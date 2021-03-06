package com.procurement.submission.infrastructure.dto.bid.evaluation.request

import com.procurement.submission.infrastructure.AbstractDTOTestBase
import com.procurement.submission.infrastructure.handler.v1.model.request.GetBidsForEvaluationRequest
import org.junit.jupiter.api.Test

class GetBidsForEvaluationRequestTest : AbstractDTOTestBase<GetBidsForEvaluationRequest>(GetBidsForEvaluationRequest::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/infrastructure/dto/bid/evaluation/request/request_get_bids_for_evaluation_full.json")
    }

}