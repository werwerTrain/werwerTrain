package com.buaa.werwertrain.client.fallback;

import com.buaa.werwertrain.client.UserClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "userClient")
public class UserClientFallback implements UserClient {

    @Override
    public String getEmail(String userId) {
        System.out.println("Get email request failed, fallback method executed.");
        // Provide fallback behavior here
        return "fallback@example.com"; // Example fallback value
    }
}