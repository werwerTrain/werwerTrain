package com.buaa.werwertrain.controller;

import com.buaa.werwertrain.service.IStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = "*",allowCredentials="true",allowedHeaders = "*",methods = {POST,GET})
@RestController
public class StationController {

    @Autowired
    private IStationService stationService;

    @GetMapping("/trains/station")
    public Map<String, Object> inquireAllStations() {
//        response.setHeader("Access-Control-Allow-Origin", "http://49.232.244.162");

        List<Object> list = new ArrayList<>();
        stationService.inquireAllStations().forEach(e -> {
            list.add(new HashMap<>() {{
                put("value", e.getStationName());
            }});
        });
        return new HashMap<>() {{
            put("station", list);
        }};
    }
}
