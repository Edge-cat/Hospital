package com.neusoft.hospital.service;

import com.neusoft.hospital.dto.AiConsultRequest;
import com.neusoft.hospital.dto.AiConsultResponse;
import com.neusoft.hospital.dto.AiDoctorAssistRequest;

public interface AiConsultService {

    AiConsultResponse consult(AiConsultRequest request);

    AiConsultResponse doctorAssist(AiDoctorAssistRequest request);
}
