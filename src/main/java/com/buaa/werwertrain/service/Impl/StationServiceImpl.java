package com.buaa.werwertrain.service.Impl;

import com.buaa.werwertrain.entity.Station;
import com.buaa.werwertrain.mapper.IStationMapper;
import com.buaa.werwertrain.service.IStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("stationService")
public class StationServiceImpl implements IStationService {
    @Autowired
    private IStationMapper stationMapper;

    @Override
    public List<Station> inquireAllStations() {
        return stationMapper.inquireAllStations();
    }
}
