package com.buaa.werwertrain.client.fallback;

import com.buaa.werwertrain.client.MessageClient;

import java.util.Map;

public class MessageClientFallback implements MessageClient {

    @Override
    public void addMessage(Map<String, Object> messageMap) {
        System.out.println("addMessage request failed, fallback method executed.");
    }
}
