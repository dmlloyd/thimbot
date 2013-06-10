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

import com.flurg.thimbot.Charsets;
import com.flurg.thimbot.Priority;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayDeque;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class LineProtocolConnection<T> {

    static final Charset UTF_8 = Charset.forName("utf-8");

    final T context;
    final LineListener<T> lineListener;
    final Socket socket;
    final int bufSize;

    boolean shutdown;
    int windowSize = 8;
    int seq, ack;

    final Object lock = new Object();
    final ArrayDeque<LineOutputCallback<T>> lowQueue = new ArrayDeque<>();
    final ArrayDeque<LineOutputCallback<T>> medQueue = new ArrayDeque<>();
    final ArrayDeque<LineOutputCallback<T>> highQueue = new ArrayDeque<>();

    private final Thread readThread = new Thread(new Runnable() {

        public void run() {
            try {
                final InputStream inputStream = socket.getInputStream();
                final byte[] bytes = new byte[bufSize];
                final LineListener<T> listener = lineListener;
                int res;
                int lim = 0, pos = 0;
                for (;;) {
                    res = inputStream.read(bytes, lim, bufSize - lim);

                    if (res > 0) {
                        lim += res;
                    }

                    for (int i = pos; i < lim; i ++) {
                        if (bytes[i] == 13 && i < lim - 1 && bytes[i + 1] == 10) {
                            System.out.printf(">>> %s%n", new String(bytes, pos, i - pos, Charsets.US_ASCII));
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
                            System.out.printf(">>> %s%n", new String(bytes, 0, lim, Charsets.US_ASCII));
                            listener.handleLine(context, LineProtocolConnection.this, bytes, 0, lim);
                        }
                        signalShutdown();
                        return;
                    }
                }
            } catch (IOException e) {
                signalShutdown();
            }
        }
    }, "IRC Read Thread");

    private final Thread writeThread = new Thread(new Runnable() {
        public void run() {
            try {
                final OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream(), 16384);
                final Object lock = LineProtocolConnection.this.lock;
                LineOutputCallback<T> callback;
                boolean shutdown = false;

                for (;;) {
                    synchronized (lock) {
                        if (LineProtocolConnection.this.shutdown) return;
                        if (seq - ack == windowSize) {
                            outputStream.write('P');
                            outputStream.write('I');
                            outputStream.write('N');
                            outputStream.write('G');
                            outputStream.write(' ');
                            outputStream.write('Q');
                            outputStream.write(Integer.toString(seq++).getBytes(Charsets.US_ASCII));
                            outputStream.write(13);
                            outputStream.write(10);
                            outputStream.flush();
                        }
                        while (seq - ack > windowSize) try {
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
                                            lock.wait();
                                            if (LineProtocolConnection.this.shutdown) return;
                                        } catch (InterruptedException e) {
                                        }
                                        continue;
                                    }
                                }
                            }
                        } while (callback == null);
                    }
                    try {
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
                        return;
                    }
                }
            } catch (IOException e) {
                return;
            } finally {
                safeClose(socket);
                final LineListener<T> listener = lineListener;
                try {
                    listener.terminated(context, LineProtocolConnection.this);
                } catch (Throwable ignored) {}
            }
        }
    }, "IRC Write Thread");

    public LineProtocolConnection(final T context, final LineListener<T> lineListener, final Socket socket, final int bufSize) {
        this.context = context;
        this.lineListener = lineListener;
        this.socket = socket;
        this.bufSize = bufSize;
    }

    public void start() {
        readThread.start();
        writeThread.start();
    }

    private void enqueue(ArrayDeque<LineOutputCallback<T>> queue, LineOutputCallback<T> callback) {
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

    public void queueMessage(Priority priority, LineOutputCallback<T> callback) {
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

    public void acknowledge(int ackSeq) {
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

    static final class ByteArrayOutput extends ByteArrayOutputStream implements ByteOutput {

        ByteArrayOutput() {
        }

        public void write(final StringEmitter emitter) throws IOException {
            emitter.emit(this);
        }

        public void write(final String string, final Charset charset) throws IOException {
            write(string.getBytes(charset));
        }

        public void write(final String string) throws IOException {
            write(string.getBytes(Charsets.US_ASCII));
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
