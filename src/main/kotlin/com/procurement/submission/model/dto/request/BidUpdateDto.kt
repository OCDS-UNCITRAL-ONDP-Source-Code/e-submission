package com.procurement.submission.model.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.submission.model.dto.ocds.*
import com.procurement.submission.model.dto.ocds.Bid
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class BidUpdateDto @JsonCreator constructor(

        @field:Valid @field:NotNull
        val bid: BidUpdate
)

data class BidUpdate @JsonCreator constructor(

        @field:Valid
        var value: Value?,

        @field:Valid @field:NotEmpty
        var documents: List<Document>
)