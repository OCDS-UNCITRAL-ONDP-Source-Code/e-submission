package com.procurement.submission.infrastructure.converter

import com.procurement.submission.application.params.ValidateTenderPeriodParams
import com.procurement.submission.infrastructure.dto.tender.period.ValidateTenderPeriodRequest

fun ValidateTenderPeriodRequest.convert() = ValidateTenderPeriodParams.tryCreate(
    cpid = cpid,
    ocid = ocid,
    operationType = operationType,
    pmd = pmd,
    country = country,
    date = date,
    tender = ValidateTenderPeriodParams.Tender(
        tenderPeriod = tender.tenderPeriod.convert()
            .orForwardFail { fail -> return fail }
    )
)

fun ValidateTenderPeriodRequest.Tender.TenderPeriod.convert() =
    ValidateTenderPeriodParams.Tender.TenderPeriod.tryCreate(endDate = endDate)
