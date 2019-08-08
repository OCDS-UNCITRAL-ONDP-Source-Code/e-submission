package com.procurement.submission.infrastructure.dto.award

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.submission.model.dto.ocds.AwardStatusDetails
import java.util.*

data class EvaluatedAwardsRequest(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("awards") @param:JsonProperty("awards") val awards: List<Award>
) {
    data class Award(
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: AwardStatusDetails,
        @field:JsonProperty("relatedBid") @param:JsonProperty("relatedBid") val relatedBid: UUID
    )
}
