package com.buaa.werwertrain.client.fallback;

import com.buaa.werwertrain.DTO.FoodOrderDTO;
import com.buaa.werwertrain.client.FoodClient;

import java.util.ArrayList;
import java.util.List;

public class FoodClientFallback implements FoodClient {
    @Override
    public List<FoodOrderDTO> getFoodOrders(String oid) {
        return new ArrayList<>() {{
            add(new FoodOrderDTO());
        }};
    }
}
