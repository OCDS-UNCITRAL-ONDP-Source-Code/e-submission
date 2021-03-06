package com.procurement.submission.application.model.data.bid.create

import com.procurement.submission.domain.model.Cpid
import com.procurement.submission.domain.model.Ocid
import com.procurement.submission.domain.model.Owner
import com.procurement.submission.domain.model.enums.ProcurementMethod
import java.time.LocalDateTime

data class BidCreateContext(
    val cpid: Cpid,
    val ocid: Ocid,
    val owner: Owner,
    val startDate: LocalDateTime,
    val pmd: ProcurementMethod
)
