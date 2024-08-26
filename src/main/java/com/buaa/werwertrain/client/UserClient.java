package com.buaa.werwertrain.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service",contextId = "userClient")
public interface UserClient {
    @GetMapping("/getEmail")
    public String getEmail(String userId);
}
