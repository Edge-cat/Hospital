package com.neusoft.hospital.service.impl;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.neusoft.hospital.entity.SysConfig;

import com.neusoft.hospital.mapper.SysConfigMapper;

import com.neusoft.hospital.service.SysConfigService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;



import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;



@Service

@RequiredArgsConstructor

public class SysConfigServiceImpl implements SysConfigService {



    private static final Map<String, String[]> DISPLAY = Map.of(

            "hospital_name", new String[]{"医院名称", "系统显示名称"},

            "register_fee", new String[]{"普通挂号费(元)", "默认挂号费用"},

            "expert_fee", new String[]{"专家挂号费(元)", "专家号费用"},

            "appointment_days", new String[]{"预约天数", "可提前预约天数"},

            "session_timeout", new String[]{"会话超时(分钟)", "登录超时时间"}

    );



    private final SysConfigMapper sysConfigMapper;



    @Override

    public List<Map<String, Object>> listForView() {

        List<SysConfig> configs = sysConfigMapper.selectList(null);

        List<Map<String, Object>> result = new ArrayList<>();

        for (SysConfig config : configs) {

            String[] meta = DISPLAY.getOrDefault(config.getConfigKey(),

                    new String[]{config.getDescription(), ""});

            Map<String, Object> row = new LinkedHashMap<>();

            row.put("id", config.getId());

            row.put("configKey", config.getConfigKey());

            row.put("configValue", config.getConfigValue());

            row.put("configName", meta[0] != null ? meta[0] : config.getConfigKey());

            row.put("remark", meta[1] != null ? meta[1] : "");

            result.add(row);

        }

        return result;

    }



    @Override

    public void saveByForm(Map<String, Object> form) {

        if (form == null) {

            return;

        }

        for (Map.Entry<String, Object> entry : form.entrySet()) {

            SysConfig existing = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()

                    .eq(SysConfig::getConfigKey, entry.getKey()));

            if (existing != null) {

                existing.setConfigValue(entry.getValue() != null ? entry.getValue().toString() : "");

                sysConfigMapper.updateById(existing);

            } else {

                SysConfig config = new SysConfig();

                config.setConfigKey(entry.getKey());

                config.setConfigValue(entry.getValue() != null ? entry.getValue().toString() : "");

                sysConfigMapper.insert(config);

            }

        }

    }

}

