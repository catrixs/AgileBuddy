package com.feiyang.agilebuddy.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.feiyang.agilebuddy.common.MessageDefinition;

public class UdpServer {
    private static final int MAX_PLAYERS_COUNT = 3;

    private Set<InetAddress> clients = new HashSet<InetAddress>();

    private DatagramSocket serverSocket = null;

    public UdpServer() {
        try {
            serverSocket = new DatagramSocket(MessageDefinition.SERVER_PORT);
            System.out.println("server starting at " + serverSocket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        byte[] receiveData = new byte[512];
        while (true) {
            // wait for ever
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
                System.out.println("recv:" + receivePacket.getAddress() + ":" + Arrays.toString(receivePacket.getData()));
                InetAddress clientAddress = receivePacket.getAddress();
                boolean isNew = clients.add(clientAddress);
                if (isNew) {
                    sendIpList(clientAddress);
                } else {
                    handleMessage(receivePacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(DatagramPacket receivePacket) {
        byte[] data = receivePacket.getData();
        if (data == null || data.length <= 0) {
            return;
        }

        int length = MessageDefinition.getMessageLength(data);
        switch (data[0]) {
            case MessageDefinition.INTENT_READY:
                sendIpList(receivePacket.getAddress());
                break;
            default:
                // default behavior: broadcast received message to others.
                broadcastMessage(data, length, receivePacket.getAddress());
                break;
        }
    }

    /*
     *  将信息分发送至 所有socket列表
	 */
    private void broadcastMessage(byte[] sendMessage, int length, InetAddress exclude) {
        for (InetAddress client : clients) {
            if (exclude != null && exclude.equals(client)) {
                continue;
            }

            DatagramPacket sendPacket = new DatagramPacket(sendMessage, length, client, MessageDefinition.CLIENT_PORT);
            try {
                System.out.println("send:" + sendPacket.getAddress() + ":" + Arrays.toString(sendPacket.getData()));
                serverSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendIpList(InetAddress newClient) {
        byte[] result = new byte[clients.size() * 4];

        int count = 0;
        for (InetAddress address : clients) {
            System.arraycopy(address.getAddress(), 0, result, count * 4, 4);
            count++;
        }
        MessageDefinition msg = new MessageDefinition(MessageDefinition.INTENT_READY, result);
        byte[] message = msg.toMessage();
        broadcastMessage(message, message.length, null);
    }

    public static void main(String[] args) {
        UdpServer server = new UdpServer();
        server.start();
    }
}
