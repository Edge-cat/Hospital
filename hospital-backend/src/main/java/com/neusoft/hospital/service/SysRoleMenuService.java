package com.neusoft.hospital.service;

import java.util.List;

public interface SysRoleMenuService {

    List<Long> listMenuIdsByRole(String roleCode);
}
