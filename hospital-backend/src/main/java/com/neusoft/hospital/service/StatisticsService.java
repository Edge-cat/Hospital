package com.neusoft.hospital.service;

import java.util.Map;

public interface StatisticsService {

    Map<String, Object> overview();

    Map<String, Object> homeOverview();

    Map<String, Object> analysis();

    Map<String, Object> reports();

    Map<String, Object> decision();
}
