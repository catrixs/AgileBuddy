package com.feiyang.agilebuddy.client.transport;

import com.feiyang.agilebuddy.common.MessageDefinition;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by chenfei on 13-6-8.
 */
public class UdpClient {
    private final List<MessageHandler> handlers = new ArrayList<MessageHandler>();
    private final Lock lock = new ReentrantLock();
    private final Condition stateChangeCondition = lock.newCondition();
    private boolean running;
    private InetAddress serverIp;
    private DatagramSocket clientSocket;

    Thread receiveThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while(true) {
                    byte[] message = new byte[1024];

                    while (running) {
                        receive(clientSocket, message);
                    }

                    if (!running) {
                        lock.lock();
                        try {
                            stateChangeCondition.await(500, TimeUnit.MILLISECONDS);
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    });

    private void receive(DatagramSocket clientSocket, byte[] buffer) {
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        try {
            clientSocket.receive(receivePacket);
            for (MessageHandler handler : handlers) {
                MessageDefinition msg = new MessageDefinition(receivePacket.getData());
                handler.handle(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UdpClient(MessageHandler handler) {
        try {
            serverIp = InetAddress.getByName(MessageDefinition.HOST);
            clientSocket = new DatagramSocket(MessageDefinition.CLIENT_PORT);
            handlers.add(handler);
            receiveThread.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        running = true;
        lock.lock();
        try {
            stateChangeCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        running = false;
    }

    public void sendMessage(byte[] bytes) {
        int len = MessageDefinition.getMessageLength(bytes);
        DatagramPacket packet = new DatagramPacket(bytes, len, serverIp, MessageDefinition.SERVER_PORT);
        try {
            clientSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registry(MessageHandler handler) {
        handlers.add(handler);
    }
}
