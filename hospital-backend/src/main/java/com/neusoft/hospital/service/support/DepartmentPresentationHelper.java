package com.neusoft.hospital.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.entity.Appointment;
import com.neusoft.hospital.entity.Doctor;
import com.neusoft.hospital.entity.RegisterOrder;
import com.neusoft.hospital.entity.Schedule;
import com.neusoft.hospital.entity.SysDepartment;
import com.neusoft.hospital.mapper.AppointmentMapper;
import com.neusoft.hospital.mapper.DoctorMapper;
import com.neusoft.hospital.mapper.RegisterOrderMapper;
import com.neusoft.hospital.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** 患者端科室列表展示字段 enrich（号源、候诊、负责人等） */
@Component
@RequiredArgsConstructor
public class DepartmentPresentationHelper {

    private static final Map<String, Map<String, Object>> DEPT_META = new LinkedHashMap<>();

    static {
        putMeta("NK", "内科", "张主任", "010-12345601",
                "诊治心血管、呼吸、消化等内科疾病，配备完善检查与慢病管理体系。",
                List.of("高血压", "糖尿病", "冠心病"), List.of("心内", "消化", "呼吸"), true);
        putMeta("WK", "外科", "李主任", "010-12345602",
                "开展普外、微创及创伤急救等诊疗服务。",
                List.of("阑尾炎", "疝气", "外伤"), List.of("普外", "微创", "创伤"), true);
        putMeta("EK", "儿科", "王主任", "010-12345603",
                "专注儿童保健与儿科疾病诊疗。",
                List.of("发热", "咳嗽", "腹泻"), List.of("呼吸", "保健", "新生儿"), true);
        putMeta("GK", "骨科", "赵主任", "010-12345604",
                "骨骼、关节、运动损伤专业治疗。",
                List.of("骨折", "关节炎", "运动损伤"), List.of("关节", "创伤", "脊柱"), true);
        putMeta("YK", "眼科", "刘主任", "010-12345605",
                "眼部疾病检查与手术治疗。",
                List.of("白内障", "近视", "干眼症"), List.of("白内障", "屈光", "眼底"), true);
        putMeta("PFK", "皮肤科", "陈主任", "010-12345606",
                "诊治湿疹、痤疮、皮炎等常见皮肤病。",
                List.of("湿疹", "痤疮", "皮炎"), List.of("皮肤镜", "激光", "过敏"), true);
        putMeta("KQK", "口腔科", "周主任", "010-12345607",
                "口腔内科、修复、正畸及洁牙保健。",
                List.of("龋齿", "牙周炎", "口腔溃疡"), List.of("修复", "正畸", "洁牙"), true);
        putMeta("XXK", "信息科", "赵主任", "010-12345608",
                "医院信息系统运维与技术支持，不对外提供门诊挂号服务。",
                List.of(), List.of("系统运维", "网络安全"), false);
    }

    private final DoctorMapper doctorMapper;
    private final ScheduleMapper scheduleMapper;
    private final AppointmentMapper appointmentMapper;
    private final RegisterOrderMapper registerOrderMapper;
    private final RegisterSlotHelper registerSlotHelper;

    public List<Map<String, Object>> enrichForPatient(List<SysDepartment> departments) {
        List<Doctor> doctors = doctorMapper.selectList(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getStatus, 1));
        Map<String, List<Doctor>> doctorsByDept = doctors.stream()
                .filter(d -> StringUtils.hasText(d.getDepartment()))
                .collect(Collectors.groupingBy(Doctor::getDepartment));
        Map<String, Long> waitingByDept = countWaitingToday();

        List<Map<String, Object>> result = new ArrayList<>();
        for (SysDepartment dept : departments) {
            List<Doctor> deptDoctors = doctorsByDept.getOrDefault(dept.getName(), List.of());
            long waiting = waitingByDept.getOrDefault(dept.getName(), 0L);
            result.add(toPatientView(dept, deptDoctors, waiting));
        }
        return result;
    }

    private Map<String, Object> toPatientView(SysDepartment dept, List<Doctor> deptDoctors, long waitingCount) {
        Map<String, Object> meta = DEPT_META.getOrDefault(dept.getName(), Map.of());
        boolean outpatient = meta.get("outpatient") != Boolean.FALSE;

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", dept.getId());
        row.put("name", dept.getName());
        row.put("code", resolveCode(dept, meta));
        row.put("parentId", dept.getParentId() != null ? dept.getParentId() : 0L);
        row.put("sort", dept.getSort());
        row.put("status", dept.getStatus());
        row.put("description", dept.getDescription());
        row.put("desc", meta.containsKey("desc") ? meta.get("desc") : dept.getDescription());
        row.put("leader", meta.getOrDefault("leader", resolveLeader(deptDoctors)));
        row.put("phone", meta.getOrDefault("phone", ""));
        row.put("specialties", meta.getOrDefault("specialties", List.of()));
        row.put("commonDiseases", meta.getOrDefault("commonDiseases", List.of()));
        row.put("outpatient", outpatient);
        row.put("todaySlots", outpatient ? sumTodayRemaining(deptDoctors) : 0);
        row.put("waitingCount", outpatient ? waitingCount : 0L);
        row.put("avgWaitMinutes", outpatient ? estimateWaitMinutes(waitingCount) : 0);
        return row;
    }

    private int sumTodayRemaining(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        int sum = 0;
        for (Doctor doctor : doctors) {
            if (doctor.getId() == null) {
                continue;
            }
            sum += todayRemainingForDoctor(doctor.getId(), today);
        }
        return sum;
    }

    private int todayRemainingForDoctor(long doctorId, LocalDate today) {
        Set<LocalDate> scheduledDates = scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getDoctorId, doctorId)
                        .eq(Schedule::getStatus, 1)
                        .eq(Schedule::getShiftDate, today))
                .stream()
                .map(Schedule::getShiftDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Long> bookedCounts = new HashMap<>();
        for (Appointment appointment : appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDoctorId, doctorId)
                .eq(Appointment::getAppointmentDate, today)
                .in(Appointment::getStatus, 0, 1))) {
            if (!StringUtils.hasText(appointment.getTimeSlot())) {
                continue;
            }
            String key = today + "|" + appointment.getTimeSlot();
            bookedCounts.merge(key, 1L, Long::sum);
        }
        registerSlotHelper.applyPaidRegisterBookings(doctorId, today, today, bookedCounts);

        List<Map<String, Object>> dates = AppointmentScheduleHelper.buildDoctorSchedule(
                doctorId, 1, scheduledDates, bookedCounts);
        if (dates.isEmpty()) {
            return 0;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> slots = (List<Map<String, Object>>) dates.get(0).get("slots");
        if (slots == null) {
            return 0;
        }
        return slots.stream()
                .mapToInt(slot -> {
                    Object remaining = slot.get("remaining");
                    return remaining instanceof Number n ? n.intValue() : 0;
                })
                .sum();
    }

    private Map<String, Long> countWaitingToday() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        Map<String, Long> counts = new HashMap<>();
        for (RegisterOrder order : registerOrderMapper.selectList(new LambdaQueryWrapper<RegisterOrder>()
                .ge(RegisterOrder::getRegisterTime, dayStart)
                .lt(RegisterOrder::getRegisterTime, dayEnd)
                .in(RegisterOrder::getStatus, 0, 1))) {
            if (!StringUtils.hasText(order.getDepartment())) {
                continue;
            }
            counts.merge(order.getDepartment(), 1L, Long::sum);
        }
        return counts;
    }

    private int estimateWaitMinutes(long waitingCount) {
        if (waitingCount <= 0) {
            return 0;
        }
        return (int) Math.min(45, 8 + waitingCount * 3);
    }

    private String resolveLeader(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            return "";
        }
        return doctors.stream()
                .filter(d -> d.getTitle() != null && d.getTitle().contains("主任"))
                .map(Doctor::getName)
                .findFirst()
                .orElse(doctors.get(0).getName());
    }

    private static void putMeta(String code, String name, String leader, String phone, String desc,
                                List<String> diseases, List<String> specialties, boolean outpatient) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("code", code);
        meta.put("leader", leader);
        meta.put("phone", phone);
        meta.put("desc", desc);
        meta.put("commonDiseases", diseases);
        meta.put("specialties", specialties);
        meta.put("outpatient", outpatient);
        DEPT_META.put(name, meta);
    }

    private String resolveCode(SysDepartment dept, Map<String, Object> meta) {
        if (StringUtils.hasText(dept.getCode())) {
            return dept.getCode();
        }
        Object code = meta.get("code");
        return code != null ? code.toString() : "";
    }
}
