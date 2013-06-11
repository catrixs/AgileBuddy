package com.feiyang.agilebuddy.client.transport;

import com.feiyang.agilebuddy.common.MessageDefinition;

/**
 * Created by chenfei on 13-6-8.
 */
public interface MessageHandler {
    void handle(MessageDefinition message);
}
