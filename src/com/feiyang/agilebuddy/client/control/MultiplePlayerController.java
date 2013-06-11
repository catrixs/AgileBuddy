package com.feiyang.agilebuddy.client.control;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.feiyang.agilebuddy.client.transport.MessageHandler;
import com.feiyang.agilebuddy.client.transport.UdpClient;
import com.feiyang.agilebuddy.common.MessageDefinition;
import com.feiyang.agilebuddy.common.util.InetAddressUtil;

import org.void1898.www.agilebuddy.AgileBuddyActivity;
import org.void1898.www.agilebuddy.MultiplePlayerActivity;
import org.void1898.www.agilebuddy.material.UIModel;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenfei on 13-6-7.
 */
public class MultiplePlayerController extends Handler implements
        MessageHandler {
    private static final Random rnd = new Random();
    private static float value;
    private static final MultiplePlayerController controller = new MultiplePlayerController();
    private static final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();

    private int me;
    private int current;
    private List<Integer> players = new ArrayList<Integer>();

    public AgileBuddyActivity activity;
    private MultiplePlayerActivity multiplePlayerActivity;
    private boolean running;
    private UdpClient client;

    private ScheduledFuture moveFuture;

    public static MultiplePlayerController getInstance() {
        return controller;
    }

    public MultiplePlayerController() {
        init();
    }

    public void searching() {
        InetAddress address = InetAddressUtil.getLocalIpAddress();
        MessageDefinition message = new MessageDefinition(MessageDefinition.INTENT_READY, address.getAddress());
        client.sendMessage(message.toMessage());
    }

    private void handleSearch(MessageDefinition message) {
        byte[] address = new byte[4];
        Set<Integer> ips = new HashSet<Integer>();
        while(message.hasNext()) {
            ips.add(message.getInt());
        }
        players = new ArrayList<Integer>(ips);
        Collections.sort(players);
    }

    public void move(int velocityX, int velocityY) {
        MessageDefinition message = new MessageDefinition(MessageDefinition.INTENT_OTHER_MOVE);
        message.append(velocityX);
        message.append(velocityY);
        client.sendMessage(message.toMessage());
    }

    public void handleMove(MessageDefinition message) {
        int velocityX = message.getInt();
        int velocityY = message.getInt();
        activity.mAgileBuddyView.mUIThread.handleOtherMoveEvent(velocityX);
    }

    public void gameStart() {
        current = me;
        running = true;
        invokeTimer();
//        scheduleMoveTimerTask();

        MessageDefinition message = new MessageDefinition(MessageDefinition.INTENT_START);
        message.append(current);
        client.sendMessage(message.toMessage());
    }

    public void handleGameStart(MessageDefinition message) {
        current = message.getInt();
        this.sendMessage(this.obtainMessage(MessageDefinition.INTENT_START));
    }

    public void gameover() {
        running = false;
        if(moveFuture != null) {
            moveFuture.cancel(true);
            moveFuture = null;
        }

        MessageDefinition msg = new MessageDefinition(MessageDefinition.INTENT_GAME_OVER);
        client.sendMessage(msg.toMessage());
    }

    public void handleGameover() {
        this.running = false;
        this.sendMessage(this.obtainMessage(MessageDefinition.INTENT_GAME_OVER));
    }

    public void changeTurn() {
        int index = Collections.binarySearch(players, current);
        index = (index >= players.size() - 1) || index < 0 ? index = 0 : index + 1;
        int nextPlayer = players.get(index);
        current = nextPlayer;

        MessageDefinition message = new MessageDefinition(MessageDefinition.INTENT_CHANGE_TURN);
        message.append(current);
        client.sendMessage(message.toMessage());
    }

    public void handleChangeTurn(MessageDefinition message) {
        current = message.getInt();
        if (isMyTurn()) {
            invokeTimer();
        }
    }

    public void init() {
        me = InetAddressUtil.pack(InetAddressUtil.getLocalIpAddress()
                .getAddress());
        client = new UdpClient(this);
        client.start();
        running = true;
    }

    public void stop() {
        running = false;
        if(moveFuture != null) {
            moveFuture.cancel(true);
            moveFuture = null;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Intent i = null;
        switch (msg.what) {
            case MessageDefinition.INTENT_CHANGE_TURN:
                Toast.makeText(activity, "»¹ÓÐ3Ãë", 3).show();
                break;
            case MessageDefinition.INTENT_OTHER_MOVE:
                if (activity != null) {
                    UIModel model = activity.mAgileBuddyView.mUIThread.mUIModel;
                    if(model != null) {
                        model.mRoleVelocityX = msg.arg1;
                        model.mRoleVelocityY = msg.arg2;
                    }
                }
                break;
            case MessageDefinition.INTENT_START:
                i = new Intent(multiplePlayerActivity, AgileBuddyActivity.class);
                multiplePlayerActivity.startActivity(i);
                break;
            case MessageDefinition.INTENT_GAME_OVER:
                if (activity != null) {
                    activity.mAgileBuddyView.mUIThread.gameover();
                }
                break;
            default:
                break;
        }
    }

    public boolean isMyTurn() {
        return running && me == current;
    }

    @Override
    public void handle(MessageDefinition message) {
        byte type = message.getType();
        if (type == MessageDefinition.INTENT_READY) {
            handleSearch(message);
        } else if (type == MessageDefinition.INTENT_GAME_OVER) {
            handleGameover();
        } else if (type == MessageDefinition.INTENT_START) {
            handleGameStart(message);
        } else if (type == MessageDefinition.INTENT_OTHER_MOVE) {
            handleMove(message);
        } else if (type == MessageDefinition.INTENT_CHANGE_TURN) {
            handleChangeTurn(message);
        }
    }

    public AgileBuddyActivity getActivity() {
        return activity;
    }

    public void setActivity(AgileBuddyActivity activity) {
        this.activity = activity;
    }

    public MultiplePlayerActivity getMultiplePlayerActivity() {
        return multiplePlayerActivity;
    }

    public void setMultiplePlayerActivity(
            MultiplePlayerActivity multiplePlayerActivity) {
        this.multiplePlayerActivity = multiplePlayerActivity;
    }

    public void invokeTimer() {
        timer.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isMyTurn()) {
                        sendMessage(obtainMessage(MessageDefinition.INTENT_CHANGE_TURN));
                    }
                } catch (Exception ex) {
                    Log.d("", "Error at 'run' method", ex);
                }
            }
        }, 1000 * 10L, TimeUnit.MILLISECONDS);
        timer.schedule(new Runnable() {
            @Override
            public void run() {
                if (isMyTurn()) {
                    changeTurn();
                }
            }
        }, 15000L, TimeUnit.MILLISECONDS);
    }

//    public void scheduleMoveTimerTask() {
//        if(moveFuture != null) {
//            moveFuture.cancel(true);
//        }
//
//        moveFuture = timer.scheduleAtFixedRate(new Runnable() {
//            private int lastX;
//            private int lastY;
//            @Override
//            public void run() {
//                if (running && activity != null && isMyTurn()) {
//                    UIModel model = activity.mAgileBuddyView.mUIThread.mUIModel;
//                    if(model != null) {
//                        int currentX = model.mRoleVelocityX;
//                        int currentY = model.mRoleVelocityY;
//                        if(currentX != lastX || currentY != lastY) {
//                            move(currentX, currentY);
//                            lastX = currentX;
//                            lastY = currentY;
//                        }
//                    }
//                }
//            }
//        }, 0, 25, TimeUnit.MILLISECONDS);
//    }
}
