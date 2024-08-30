package com.buaa.werwertrain.client.fallback;

import com.buaa.werwertrain.client.MessageClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.Map;

@CircuitBreaker(name = "messageClient")
public class MessageClientFallback implements MessageClient {

    @Override
    public void addMessage(Map<String, Object> messageMap) {
        System.out.println("addMessage request failed, fallback method executed.");
    }
}
