package com.procurement.submission.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.submission.domain.model.bid.BidId

data class OpenBidDocsRequest(
    @field:JsonProperty("bidId") @param:JsonProperty("bidId") val bidId: BidId
)
