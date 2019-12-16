package com.procurement.submission.application.service

import com.procurement.submission.domain.model.ProcurementMethod

data class GetBidsAuctionContext(
    val cpid: String,
    val stage: String,
    val pmd: ProcurementMethod,
    val country: String
)
