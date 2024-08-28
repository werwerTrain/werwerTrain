package com.buaa.werwertrain.client;

import com.buaa.werwertrain.client.fallback.MessageClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "user-service",contextId = "messageClient", path = "/api/users",
fallback = MessageClientFallback.class)
public interface MessageClient {
    @PostMapping("/addMessage")
    void addMessage(@RequestBody Map<String, Object> messageMap);
}
