package com.procurement.submission.service;

import com.procurement.submission.model.dto.request.SubmissionPeriodDto;
import org.springframework.stereotype.Service;

@Service
public interface SubmissionPeriodService {

    void insertData(SubmissionPeriodDto data);
}
