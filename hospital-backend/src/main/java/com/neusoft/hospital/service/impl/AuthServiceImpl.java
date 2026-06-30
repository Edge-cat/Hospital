package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.config.JwtUtil;
import com.neusoft.hospital.dto.LoginRequest;
import com.neusoft.hospital.dto.LoginResponse;
import com.neusoft.hospital.dto.RegisterRequest;
import com.neusoft.hospital.dto.WxLoginRequest;
import com.neusoft.hospital.entity.Patient;
import com.neusoft.hospital.entity.SysLoginLog;
import com.neusoft.hospital.entity.SysUser;
import com.neusoft.hospital.mapper.PatientMapper;
import com.neusoft.hospital.mapper.SysLoginLogMapper;
import com.neusoft.hospital.mapper.SysUserMapper;
import com.neusoft.hospital.service.AuthService;
import com.neusoft.hospital.service.SysRoleMenuService;
import com.neusoft.hospital.service.support.PasswordHelper;
import com.neusoft.hospital.service.support.WxAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PatientMapper patientMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final JwtUtil jwtUtil;
    private final PasswordHelper passwordHelper;
    private final SysRoleMenuService roleMenuService;
    private final WxAuthClient wxAuthClient;

    @Value("${hospital.wx.demo-phone:13800138000}")
    private String wxDemoPhone;

    @Override
    public LoginResponse login(LoginRequest request, String ip) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            Patient patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                    .eq(Patient::getPhone, request.getUsername()));
            if (patient != null && patient.getUserId() != null) {
                user = sysUserMapper.selectById(patient.getUserId());
            }
        }
        if (user == null || !passwordHelper.matches(request.getPassword(), user.getPassword())) {
            saveLoginLog(request.getUsername(), ip, 0, "用户名或密码错误");
            throw new BusinessException(401, "用户名或密码错误");
        }
        upgradePasswordIfNeeded(user, request.getPassword());
        saveLoginLog(user.getUsername(), ip, 1, "登录成功");
        return buildLoginResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request, String ip) {
        String phone = normalizePhone(request.getPhone());
        String password = request.getPassword();
        String name = request.getName() != null ? request.getName().trim() : "";

        if (!StringUtils.hasText(phone) || !phone.matches("^1\\d{10}$")) {
            throw new BusinessException(400, "请输入正确的11位手机号");
        }
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BusinessException(400, "密码至少6位");
        }
        if (!StringUtils.hasText(name) || name.length() < 2) {
            throw new BusinessException(400, "请输入真实姓名");
        }

        SysUser exists = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, phone));
        if (exists != null) {
            throw new BusinessException(400, "该手机号已注册");
        }

        Patient existingPatient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getPhone, phone));
        if (existingPatient != null && existingPatient.getUserId() != null) {
            throw new BusinessException(400, "该手机号已绑定账号");
        }

        SysUser user = new SysUser();
        user.setUsername(phone);
        user.setPassword(passwordHelper.encode(password));
        user.setName(name);
        user.setRole("patient");
        user.setRoleLabel("患者");
        user.setPhone(phone);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        sysUserMapper.insert(user);

        if (existingPatient != null) {
            existingPatient.setUserId(user.getId());
            if (!StringUtils.hasText(existingPatient.getName())) {
                existingPatient.setName(name);
            }
            if (StringUtils.hasText(request.getIdCard())) {
                existingPatient.setIdCard(request.getIdCard().trim());
            }
            if (request.getGender() != null) {
                existingPatient.setGender(request.getGender());
            }
            patientMapper.updateById(existingPatient);
        } else {
            Patient patient = new Patient();
            patient.setPatientNo("P" + System.currentTimeMillis() % 100000000L);
            patient.setName(name);
            patient.setPhone(phone);
            patient.setIdCard(StringUtils.hasText(request.getIdCard()) ? request.getIdCard().trim() : null);
            patient.setGender(request.getGender());
            patient.setCardNo(patient.getPatientNo());
            // 2=已完成/未入候诊队列；仅管理端「加入候诊」或挂号后应设为 0
            patient.setStatus(2);
            patient.setUserId(user.getId());
            patient.setCreateTime(LocalDateTime.now());
            patientMapper.insert(patient);
        }

        saveLoginLog(user.getUsername(), ip, 1, "注册成功");
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse wxLogin(WxLoginRequest request, String ip) {
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException(400, "缺少微信授权 code");
        }
        WxAuthClient.WxSession session = wxAuthClient.code2Session(request.getCode());
        Patient patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getWxOpenid, session.getOpenid()));
        if (patient == null && StringUtils.hasText(request.getPhone())) {
            patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                    .eq(Patient::getPhone, request.getPhone()));
            if (patient != null) {
                patient.setWxOpenid(session.getOpenid());
                patientMapper.updateById(patient);
            }
        }
        if (patient == null) {
            String phone = StringUtils.hasText(request.getPhone()) ? request.getPhone() : wxDemoPhone;
            patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>().eq(Patient::getPhone, phone));
            if (patient != null && !StringUtils.hasText(patient.getWxOpenid())) {
                patient.setWxOpenid(session.getOpenid());
                patientMapper.updateById(patient);
            }
        }
        if (patient == null || patient.getUserId() == null) {
            saveLoginLog(session.getOpenid(), ip, 0, "微信登录：未绑定患者");
            throw new BusinessException(404, "该微信未绑定患者账号");
        }
        SysUser user = sysUserMapper.selectById(patient.getUserId());
        if (user == null) {
            saveLoginLog(session.getOpenid(), ip, 0, "微信登录：用户不存在");
            throw new BusinessException(404, "用户不存在");
        }
        saveLoginLog(user.getUsername(), ip, 1, "微信登录成功");
        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(SysUser user) {
        LoginResponse response = new LoginResponse();
        response.setToken(jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getName()));
        LoginResponse.UserInfo info = new LoginResponse.UserInfo();
        info.setId(user.getId());
        info.setName(user.getName());
        info.setRole(user.getRole());
        info.setRoleLabel(user.getRoleLabel());
        info.setDepartment(user.getDepartment());
        List<Long> menuIds = roleMenuService.listMenuIdsByRole(user.getRole());
        info.setMenuIds(menuIds);
        response.setUser(info);
        return response;
    }

    private void upgradePasswordIfNeeded(SysUser user, String rawPassword) {
        if (!passwordHelper.isEncoded(user.getPassword())) {
            user.setPassword(passwordHelper.encode(rawPassword));
            sysUserMapper.updateById(user);
        }
    }

    private void saveLoginLog(String username, String ip, int status, String message) {
        SysLoginLog log = new SysLoginLog();
        log.setUsername(username);
        log.setIp(ip);
        log.setStatus(status);
        log.setMessage(message);
        log.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(log);
    }

    private String normalizePhone(String phone) {
        return phone != null ? phone.trim() : "";
    }
}
