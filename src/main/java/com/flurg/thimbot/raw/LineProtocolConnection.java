/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flurg.thimbot.raw;

import com.flurg.thimbot.Priority;
import com.flurg.thimbot.ThimBot;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class LineProtocolConnection {

    static final Charset UTF_8 = Charset.forName("utf-8");
    private static final long PING_TIME = 24000000000L;

    final ThimBot context;
    final LineListener lineListener;
    final Socket socket;
    final int bufSize;

    boolean shutdown;
    int windowSize = 8;
    long seq, ack;

    final Object lock = new Object();
    final ArrayDeque<LineOutputCallback> lowQueue = new ArrayDeque<>();
    final ArrayDeque<LineOutputCallback> medQueue = new ArrayDeque<>();
    final ArrayDeque<LineOutputCallback> highQueue = new ArrayDeque<>();

    private final Thread readThread = new Thread(new Runnable() {

        public void run() {
            try {
                final InputStream inputStream = socket.getInputStream();
                final byte[] bytes = new byte[bufSize];
                final LineListener listener = lineListener;
                int res;
                int lim = 0, pos = 0;
                for (;;) {
                    res = inputStream.read(bytes, lim, bufSize - lim);

                    if (res > 0) {
                        lim += res;
                    }

                    for (int i = pos; i < lim; i ++) {
                        if (bytes[i] == 13 && i < lim - 1 && bytes[i + 1] == 10) {
                            System.out.printf(">>> %s%n", new String(bytes, pos, i - pos, StandardCharsets.US_ASCII));
                            listener.handleLine(context, LineProtocolConnection.this, bytes, pos, i - pos);
                            pos = i + 2;
                        }
                    }

                    if (pos > 0) {
                        System.arraycopy(bytes, pos, bytes, 0, lim - pos);
                        lim -= pos;
                        pos = 0;
                    }

                    if (res == -1) {
                        if (lim > 0) {
                            System.out.printf(">>> %s%n", new String(bytes, 0, lim, StandardCharsets.US_ASCII));
                            listener.handleLine(context, LineProtocolConnection.this, bytes, 0, lim);
                        }
                        signalShutdown();
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.printf("Read exception: %s%n", e);
                signalShutdown();
            }
        }
    }, "IRC Read Thread");

    private final Thread writeThread = new Thread(new Runnable() {
        public void run() {
            try {
                final OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream(), 16384);
                final Object lock = LineProtocolConnection.this.lock;
                LineOutputCallback callback;
                boolean shutdown = false;
                long lastPing = System.nanoTime();
                long now;

                for (;;) {
                    synchronized (lock) {
                        if (LineProtocolConnection.this.shutdown) return;
                        if ((now = System.nanoTime()) - lastPing > PING_TIME || seq - ack == (long)windowSize) {
                            outputStream.write('P');
                            outputStream.write('I');
                            outputStream.write('N');
                            outputStream.write('G');
                            outputStream.write(' ');
                            outputStream.write('Q');
                            outputStream.write(Long.toString(seq++).getBytes(StandardCharsets.US_ASCII));
                            outputStream.write(13);
                            outputStream.write(10);
                            outputStream.flush();
                            lastPing = now;
                        }
                        while (seq - ack > (long)windowSize) try {
                            lock.wait();
                            if (LineProtocolConnection.this.shutdown) return;
                        } catch (InterruptedException e) {
                        }
                        do {
                            shutdown = LineProtocolConnection.this.shutdown;
                            callback = highQueue.poll();
                            if (callback == null) {
                                callback = medQueue.poll();
                                if (callback == null) {
                                    callback = lowQueue.poll();
                                    if (callback == null) {
                                        if (shutdown) {
                                            return;
                                        }
                                        try {
                                            lock.wait(PING_TIME / 1000000L + 50L, (int) (PING_TIME % 1000000L));
                                            if (LineProtocolConnection.this.shutdown) return;
                                        } catch (InterruptedException e) {
                                        }
                                        continue;
                                    }
                                }
                            }
                        } while (callback == null);
                    }
                    if (callback != null) try {
                        final ByteArrayOutput byteOutput = new ByteArrayOutput();
                        callback.writeLine(context, byteOutput, seq);
                        if (byteOutput.isWritten()) {
                            System.out.printf("<<< %s%n", byteOutput.toString("UTF-8"));
                            byteOutput.writeTo(outputStream);
                            outputStream.write(13);
                            outputStream.write(10);
                            outputStream.flush();
                            seq++;
                        }
                    } catch (IOException e) {
                        System.out.printf("Write exception: %s%n", e);
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.printf("Write exception: %s%n", e);
                return;
            } finally {
                safeClose(socket);
                final LineListener listener = lineListener;
                try {
                    listener.terminated(context, LineProtocolConnection.this);
                } catch (Throwable ignored) {}
            }
        }
    }, "IRC Write Thread");

    public LineProtocolConnection(final ThimBot context, final LineListener lineListener, final Socket socket, final int bufSize) {
        this.context = context;
        this.lineListener = lineListener;
        this.socket = socket;
        this.bufSize = bufSize;
    }

    public void start() {
        readThread.start();
        writeThread.start();
    }

    private void enqueue(ArrayDeque<LineOutputCallback> queue, LineOutputCallback callback) {
        synchronized (lock) {
            if (shutdown) {
                return;
            }
            queue.add(callback);
            if (queue.size() == 1) {
                lock.notify();
            }
        }
    }

    public void queueMessage(Priority priority, LineOutputCallback callback) {
        if (priority == Priority.HIGH) {
            enqueue(highQueue, callback);
        } else if (priority == Priority.NORMAL) {
            enqueue(medQueue, callback);
        } else if (priority == Priority.LOW) {
            enqueue(lowQueue, callback);
        } else {
            throw new IllegalArgumentException("Invalid priority value");
        }
    }

    public void setWindowSize(int size) {
        synchronized (lock) {
            windowSize = size;
            lock.notify();
        }
    }

    public void acknowledge(long ackSeq) {
        synchronized (lock) {
            if (ack < ackSeq) {
                ack = ackSeq;
                lock.notify();
            }
        }
    }

    public void terminate() {
        synchronized (lock) {
            shutdown = true;
            lock.notify();
        }
    }

    public void detach() {
        synchronized (lock) {

        }
    }

    public boolean isTerminated() {
        synchronized (lock) {
            return shutdown;
        }
    }

    static void safeClose(final Closeable closeable) {
        if (closeable != null) try {
            closeable.close();
        } catch (Throwable ignored) {}
    }

    void signalShutdown() {
        synchronized (lock) {
            shutdown = true;
            lock.notify();
        }
    }

    public String getServerName() {
        SocketAddress address = socket.getRemoteSocketAddress();
        if (address instanceof InetSocketAddress) {
            return ((InetSocketAddress) address).getHostString();
        } else {
            return "server";
        }
    }

    static final class ByteArrayOutput extends ByteArrayOutputStream implements IRCOutput {

        ByteArrayOutput() {
        }

        public void write(final Emittable emitter) throws IOException {
            emitter.emit((ByteArrayOutputStream) this);
        }

        public void write(final StringEmitter emitter) throws IOException {
            emitter.emit((ByteArrayOutputStream) this);
        }

        public void write(final String string, final Charset charset) throws IOException {
            write(string.getBytes(charset));
        }

        public void write(final String string) throws IOException {
            write(string.getBytes(StandardCharsets.US_ASCII));
        }

        public void write(final int b) {
            super.write(b == 10 || b == 13 ? 32 : b);
        }

        public void write(final byte[] b, final int offs, final int len) {
            for (int i = 0; i < len; i ++) {
                if (b[i] == 10 || b[i] == 13) {
                    b[i] = 32;
                }
            }
            super.write(b, offs, len);
        }

        boolean isWritten() {
            return count > 0;
        }
    }
}
