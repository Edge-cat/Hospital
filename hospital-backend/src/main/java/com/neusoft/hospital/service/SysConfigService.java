package com.neusoft.hospital.service;

import java.util.List;
import java.util.Map;

public interface SysConfigService {
    List<Map<String, Object>> listForView();

    void saveByForm(Map<String, Object> form);
}
