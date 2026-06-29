package com.neusoft.hospital.service;

import java.util.List;
import java.util.Map;

public interface CommonMetaService {

    Map<String, Object> meta();

    List<Map<String, Object>> options(String key);

    Map<String, Object> enums(String key);
}
