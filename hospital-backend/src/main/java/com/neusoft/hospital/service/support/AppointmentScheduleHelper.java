package com.neusoft.hospital.service.support;



import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



public final class AppointmentScheduleHelper {



    private static final String[] SLOTS = {

            "08:00-09:00", "09:00-10:00", "10:00-11:00",

            "14:00-15:00", "15:00-16:00", "16:00-17:00"

    };

    private static final int DEFAULT_QUOTA = 4;



    private AppointmentScheduleHelper() {

    }



    public static List<Map<String, Object>> buildDoctorSchedule(long doctorId, int days) {

        return buildDoctorSchedule(doctorId, days, Set.of(), Map.of());

    }



    public static List<Map<String, Object>> buildDoctorSchedule(long doctorId, int days,

                                                                Set<LocalDate> scheduledDates,

                                                                Map<String, Long> bookedCounts) {

        List<Map<String, Object>> dates = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 0; i < days; i++) {

            LocalDate d = today.plusDays(i);

            String dateStr = d.toString();

            boolean hasSchedule = scheduledDates == null || scheduledDates.isEmpty() || scheduledDates.contains(d);

            List<Map<String, Object>> slots = new ArrayList<>();

            for (String slot : SLOTS) {

                long booked = bookedCounts != null

                        ? bookedCounts.getOrDefault(dateStr + "|" + slot, 0L)

                        : 0L;

                int remaining = hasSchedule ? (int) Math.max(0, DEFAULT_QUOTA - booked) : 0;

                Map<String, Object> slotMap = new LinkedHashMap<>();

                slotMap.put("timeSlot", slot);

                slotMap.put("remaining", remaining);

                slotMap.put("available", remaining > 0);

                slots.add(slotMap);

            }

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("date", dateStr);

            item.put("weekday", weekdayName(d.getDayOfWeek()));

            item.put("shortDate", dateStr.substring(5));

            item.put("label", i == 0 ? "今日" : i == 1 ? "明天" : weekdayName(d.getDayOfWeek()));

            item.put("isToday", i == 0);

            item.put("slots", slots);

            dates.add(item);

        }

        return dates;

    }



    private static String weekdayName(DayOfWeek day) {
        if (day == null) {
            return "";
        }
        if (day == DayOfWeek.MONDAY) {
            return "周一";
        }
        if (day == DayOfWeek.TUESDAY) {
            return "周二";
        }
        if (day == DayOfWeek.WEDNESDAY) {
            return "周三";
        }
        if (day == DayOfWeek.THURSDAY) {
            return "周四";
        }
        if (day == DayOfWeek.FRIDAY) {
            return "周五";
        }
        if (day == DayOfWeek.SATURDAY) {
            return "周六";
        }
        return "周日";
    }

}


