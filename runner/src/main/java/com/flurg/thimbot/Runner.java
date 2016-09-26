package com.flurg.thimbot;

import com.flurg.thimbot.event.EventHandler;
import com.flurg.thimbot.event.EventHandlerContext;
import com.flurg.thimbot.event.MOTDEndEvent;
import sun.security.ssl.SSLSocketFactoryImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Runner {
    public static void main(String[] args) throws Exception {
        InetAddress addr = InetAddress.getByName("irc.freenode.org");
        int port = 6667;
        SocketAddress sockaddr = new InetSocketAddress(addr, port);
        ThimBot bot = new ThimBot(sockaddr, new SSLSocketFactoryImpl());
        bot.connect();
        bot.addEventHandler(new EventHandler(){
            @Override
            public void handleEvent(EventHandlerContext context, MOTDEndEvent event) throws Exception {
                bot.sendJoin("##java");
            }
        });
    }
}
