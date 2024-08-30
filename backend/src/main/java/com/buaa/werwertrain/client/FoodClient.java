package com.buaa.werwertrain.client;

import com.buaa.werwertrain.DTO.FoodOrderDTO;
import com.buaa.werwertrain.DTO.OrderDTO;
import com.buaa.werwertrain.client.fallback.FoodClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "food-service", contextId = "foodClient"
        , path = "/api/foods"
        , fallback = FoodClientFallback.class)
public interface FoodClient {
    @GetMapping("food/{oid}")
    public List<FoodOrderDTO> getFoodOrders(
            @PathVariable String oid
    );
}
