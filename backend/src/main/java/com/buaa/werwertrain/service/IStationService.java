package com.buaa.werwertrain.service;

import com.buaa.werwertrain.entity.Station;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface IStationService {
    public List<Station> inquireAllStations();

}
