package com.buaa.werwertrain.client;

import com.buaa.werwertrain.DTO.TrainDTO;
import com.buaa.werwertrain.DTO.TrainOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "train-service")
public interface TrainClient {

    @GetMapping("train-service/{tid}/{date}/{userID}")
    public List<TrainOrderDTO> getTrainOrderByTrainAndIdentification(
            @PathVariable String tid,
            @PathVariable String date,
            @PathVariable String userID
    );

    @GetMapping("train-service/{tid}/{date}")
    public TrainDTO getTrainByTidAndDate(
            @PathVariable String tid,
            @PathVariable String date
    );

}
