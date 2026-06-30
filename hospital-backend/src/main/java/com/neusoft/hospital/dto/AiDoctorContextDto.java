package com.neusoft.hospital.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiDoctorContextDto {

    private String patientName;
    private String patientNo;
    private Integer gender;
    private Integer age;
    private String department;
    private String chiefComplaint;
    private String presentIllness;
    private String diagnosis;
    private List<String> exams = new ArrayList<>();
    private String examNote;
    private List<AiDoctorDrugDto> drugs = new ArrayList<>();
}
