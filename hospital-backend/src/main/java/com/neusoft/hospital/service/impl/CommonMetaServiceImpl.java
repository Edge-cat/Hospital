package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.config.CommonMetaProvider;
import com.neusoft.hospital.entity.SysDict;
import com.neusoft.hospital.mapper.SysDictMapper;
import com.neusoft.hospital.service.CommonMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommonMetaServiceImpl implements CommonMetaService {

    private static final Map<String, String> OPTION_DICT_TYPES = Map.of(
            "gender", "gender",
            "registerTypes", "register_type",
            "payMethods", "pay_method",
            "bedTypes", "bed_type",
            "noticeTypes", "notice_type"
    );

    private final SysDictMapper sysDictMapper;

    @Override
    @Cacheable(cacheNames = "commonMeta")
    public Map<String, Object> meta() {
        Map<String, Object> base = new LinkedHashMap<>(CommonMetaProvider.meta());
        @SuppressWarnings("unchecked")
        Map<String, Object> options = (Map<String, Object>) base.get("options");
        if (options != null) {
            Map<String, Object> merged = new LinkedHashMap<>(options);
            OPTION_DICT_TYPES.forEach((optionKey, dictType) -> {
                List<Map<String, Object>> dictOptions = loadDictOptions(dictType);
                if (!dictOptions.isEmpty()) {
                    merged.put(optionKey, dictOptions);
                }
            });
            base.put("options", merged);
        }
        return base;
    }

    @Override
    @Cacheable(cacheNames = "commonOptions", key = "#key")
    public List<Map<String, Object>> options(String key) {
        String dictType = OPTION_DICT_TYPES.get(key);
        if (dictType != null) {
            List<Map<String, Object>> dictOptions = loadDictOptions(dictType);
            if (!dictOptions.isEmpty()) {
                return dictOptions;
            }
        }
        return CommonMetaProvider.options(key);
    }

    @Override
    @Cacheable(cacheNames = "commonEnums", key = "#key")
    public Map<String, Object> enums(String key) {
        return CommonMetaProvider.enums(key);
    }

    private List<Map<String, Object>> loadDictOptions(String dictType) {
        List<SysDict> dicts = sysDictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, dictType)
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getSort)
                .orderByAsc(SysDict::getId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysDict dict : dicts) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("label", dict.getDictLabel());
            Object value = parseDictValue(dict.getDictValue());
            row.put("value", value);
            result.add(row);
        }
        return result;
    }

    private Object parseDictValue(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return raw;
        }
    }
}
