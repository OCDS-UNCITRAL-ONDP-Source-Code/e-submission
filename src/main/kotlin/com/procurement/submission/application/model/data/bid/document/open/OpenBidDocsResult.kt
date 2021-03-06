package com.procurement.submission.application.model.data.bid.document.open

import com.procurement.submission.domain.model.bid.BidId
import com.procurement.submission.domain.model.document.DocumentId
import com.procurement.submission.domain.model.enums.DocumentType
import com.procurement.submission.domain.model.lot.LotId

class OpenBidDocsResult(
    val bid: Bid
) {
    data class Bid(
        val documents: List<Document>,
        val id: BidId
    ) {
        data class Document(
            val documentType: DocumentType,
            val id: DocumentId,
            val title: String?,
            val description: String?,
            val relatedLots: List<LotId>
        )
    }
}
