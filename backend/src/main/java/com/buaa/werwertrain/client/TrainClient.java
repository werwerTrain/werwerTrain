package com.buaa.werwertrain.client;

import com.buaa.werwertrain.DTO.PassengerDTO;
import com.buaa.werwertrain.DTO.TrainDTO;
import com.buaa.werwertrain.DTO.TrainOrderDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "train-service", path = "/api/trains")
public interface TrainClient {

    @GetMapping("train/{tid}/{date}/{userID}")
    public List<TrainOrderDTO> getTrainOrderByTrainAndIdentification(
            @PathVariable String tid,
            @PathVariable String date,
            @PathVariable String userID
    );

    @GetMapping("train/{tid}/{date}")
    public TrainDTO getTrainByTidAndDate(
            @PathVariable String tid,
            @PathVariable String date
    );

    @PostMapping("/insertPassengers/{id}")
    Integer insertPassenger(@PathVariable("id") String id, @RequestBody Map<String,String> requestMap);

    @GetMapping("/getTrainIdAndDate/{orderId}")
    public List<Map<String, Object>> getTrainIdAndDate(@PathVariable String orderId);

    @GetMapping("/getStartTime/{trainId}/{trainDate}")
    public Map<String,Object> getStartTime(@PathVariable String trainId, @PathVariable String trainDate);

    @GetMapping("/getTrainState/{trainId}/{trainDate}")
    public Boolean getTrainState(@PathVariable String trainId,@PathVariable String trainDate);
}
