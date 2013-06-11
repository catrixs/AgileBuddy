package com.feiyang.agilebuddy.client.transport;

import com.feiyang.agilebuddy.common.MessageDefinition;
import com.feiyang.agilebuddy.common.util.InetAddressUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * just a simulator player.
 *
 * Created by chenfei on 13-6-10.
 */
public class MockPlayer implements MessageHandler {
    private int last;
    private int current;

    private UdpClient client;
    private List<int[]> footprint = new ArrayList<int[]>();

    public MockPlayer() {
        client = new UdpClient(this);
    }

    public void start() {
        client.start();

        MessageDefinition ready = new MessageDefinition(MessageDefinition.INTENT_READY);
        ready.append(InetAddressUtil.pack(InetAddressUtil.getLocalIpAddress().getAddress()));
        client.sendMessage(ready.toMessage());

        Scanner scanner = new Scanner(System.in);
        while(true) {
            String cmd = scanner.next();
            if("replay".equals(cmd)) {
                replayFootprint();
            }
        }
    }

    @Override
    public void handle(MessageDefinition message) {
        byte type = message.getType();
        if(type == MessageDefinition.INTENT_START) {
            last = message.getInt();
        } else if(type == MessageDefinition.INTENT_OTHER_MOVE) {
            int x = message.getInt();
            int y = message.getInt();
            footprint.add(new int[] {x, y});
        } else if(type == MessageDefinition.INTENT_CHANGE_TURN) {
            current = message.getInt();
        } else if(type == MessageDefinition.INTENT_GAME_OVER) {
            footprint.clear();
        }
    }

    private void replayFootprint() {
        // replay the mobile move.
        for (int[] move : footprint) {
            MessageDefinition message = new MessageDefinition(MessageDefinition.INTENT_OTHER_MOVE);
            message.append(move[0]);
            message.append(move[1]);
            System.out.println("replay move:" + Arrays.toString(move));
            client.sendMessage(message.toMessage());

            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        footprint.clear();

        MessageDefinition changeTurnMessage = new MessageDefinition(MessageDefinition.INTENT_START);
        changeTurnMessage.append(last);
        System.out.println("replay player:" + last);
        client.sendMessage(changeTurnMessage.toMessage());
    }

    public static void main(String[] args) {
        MockPlayer player = new MockPlayer();
        player.start();
    }
}