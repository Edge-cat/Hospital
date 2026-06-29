package com.neusoft.hospital.service;

import java.util.List;
import java.util.Map;

public interface SysMenuService {

    List<Map<String, Object>> tree();

    void create(Map<String, Object> body);

    void update(Long id, Map<String, Object> body);

    void delete(Long id);
}
