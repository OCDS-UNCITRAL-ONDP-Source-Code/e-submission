package com.procurement.submission.application.service

import com.procurement.submission.application.exception.ErrorException
import com.procurement.submission.application.exception.ErrorType
import com.procurement.submission.application.model.data.bid.auction.get.BidsAuctionRequestData
import com.procurement.submission.application.model.data.bid.auction.get.BidsAuctionResponseData
import com.procurement.submission.application.model.data.bid.auction.get.GetBidsAuctionContext
import com.procurement.submission.domain.extension.toDate
import com.procurement.submission.domain.extension.toLocal
import com.procurement.submission.domain.model.bid.BidId
import com.procurement.submission.domain.model.enums.AwardCriteria
import com.procurement.submission.domain.model.enums.AwardStatusDetails
import com.procurement.submission.domain.model.enums.ProcurementMethod
import com.procurement.submission.domain.model.enums.Status
import com.procurement.submission.domain.model.enums.StatusDetails
import com.procurement.submission.infrastructure.converter.BidData
import com.procurement.submission.infrastructure.converter.convert
import com.procurement.submission.infrastructure.dao.BidDao
import com.procurement.submission.model.dto.bpe.CommandMessage
import com.procurement.submission.model.dto.bpe.ResponseDto
import com.procurement.submission.model.dto.bpe.cpid
import com.procurement.submission.model.dto.bpe.ctxId
import com.procurement.submission.model.dto.bpe.ocid
import com.procurement.submission.model.dto.bpe.owner
import com.procurement.submission.model.dto.bpe.pmd
import com.procurement.submission.model.dto.bpe.stage
import com.procurement.submission.model.dto.bpe.startDate
import com.procurement.submission.model.dto.bpe.token
import com.procurement.submission.model.dto.ocds.Bid
import com.procurement.submission.model.dto.request.ConsideredBid
import com.procurement.submission.model.dto.request.GetDocsOfConsideredBidRq
import com.procurement.submission.model.dto.request.GetDocsOfConsideredBidRs
import com.procurement.submission.model.dto.request.RelatedBidRq
import com.procurement.submission.model.dto.request.UpdateBidsByAwardStatusRq
import com.procurement.submission.model.dto.response.BidRs
import com.procurement.submission.model.entity.BidEntity
import com.procurement.submission.utils.containsAny
import com.procurement.submission.utils.toJson
import com.procurement.submission.utils.toObject
import org.springframework.stereotype.Service
import java.util.*

@Service
class StatusService(private val rulesService: RulesService,
                    private val periodService: PeriodService,
                    private val bidDao: BidDao
) {

    fun getBidsAuction(requestData: BidsAuctionRequestData, context: GetBidsAuctionContext): BidsAuctionResponseData {
        fun determineOwner(bid: Bid, bidsRecordsByIds: Map<BidId, BidEntity>): BidData {
            val bidId = BidId.fromString(bid.id)
            val bidOwner = UUID.fromString(bidsRecordsByIds[bidId]!!.owner)
            return BidData(bidOwner, bid)
        }

        fun setPendingDate(
            bidsByOwner: Map.Entry<BidId, List<BidData>>,
            bidsRecordsByIds: Map<BidId, BidEntity>
        ) : BidsAuctionResponseData.BidsData  {
            val owner = bidsByOwner.key
            val bidsWithPendingData = bidsByOwner.value.map { bidData ->
                val pendingDate = bidsRecordsByIds[BidId.fromString(bidData.bid.id)]?.pendingDate?.toLocal()
                    ?: throw ErrorException(
                        error = ErrorType.BID_NOT_FOUND,
                        message = "Cannot find bid with ${bidData.bid.id}. Bids records: ${bidsRecordsByIds.keys}."
                    )
                bidData.bid.convert(pendingDate)
            }
            return BidsAuctionResponseData.BidsData(owner = owner, bids = bidsWithPendingData)
        }

        val bidsRecords = bidDao.findAllByCpIdAndStage(context.cpid.toString(), context.stage)
        val bidsRecordsByIds = bidsRecords.associateBy { it.bidId }
        val bidsDb = bidsRecordsByIds.values.map { bidRecord -> toObject(Bid::class.java, bidRecord.jsonData) }

        val bidsByRelatedLot:Map<String, List<Bid>> = bidsDb.asSequence()
            .flatMap {bid ->
                bid.relatedLots.asSequence()
                    .map {lotId ->
                        lotId to bid
                    }
            }
            .groupBy (keySelector = {it.first}, valueTransform = {it.second})

        val relatedLots = bidsByRelatedLot.keys

        // FReq-1.4.1.10
        val ignoredInRequestBids = bidsDb.filter { it.status == Status.PENDING && !it.relatedLots.containsAny(relatedLots) }

        // FReq-1.4.1.2
        val notEnoughForOpeningBids = mutableSetOf<Bid>()
        val minNumberOfBids = rulesService.getRulesMinBids(context.country, context.pmd)
        val bidsForResponse = requestData.lots
            .asSequence()
            .flatMap { lot ->
                val bids = bidsByRelatedLot[lot.id.toString()]
                    ?.filter { bid -> bid.status == Status.PENDING }
                    ?: emptyList()
                if (bids.size >= minNumberOfBids) {
                    bids.asSequence()
                } else {
                    notEnoughForOpeningBids.addAll(bids)
                    emptySequence()
                }
            }
            .map { bid -> determineOwner(bid, bidsRecordsByIds) }
            .groupBy { it.owner }
            .map { bidsByOwner -> setPendingDate(bidsByOwner, bidsRecordsByIds) }
            .convert()

        (ignoredInRequestBids + notEnoughForOpeningBids).asSequence()
            .map { ignoredBid -> ignoredBid.copy(statusDetails = StatusDetails.ARCHIVED) }
            .map { archivedBid -> updateBidRecord(archivedBid, bidsRecordsByIds) }
            .let { updatedRecord -> bidDao.saveAll(updatedRecord.toList()) }

        return bidsForResponse
    }

    fun updateBidsByAwardStatus(cm: CommandMessage): ResponseDto {
        val cpid = cm.cpid
        val stage = cm.stage
        val dto = toObject(UpdateBidsByAwardStatusRq::class.java, cm.data)

        val bidId = dto.bidId
        val awardStatusDetails = AwardStatusDetails.creator(dto.awardStatusDetails)

        val entity = bidDao.findByCpIdAndStageAndBidId(cpid.toString(), stage, UUID.fromString(bidId))
        val bid = toObject(Bid::class.java, entity.jsonData)
        when (awardStatusDetails) {
            AwardStatusDetails.EMPTY -> bid.statusDetails = StatusDetails.EMPTY
            AwardStatusDetails.ACTIVE -> bid.statusDetails = StatusDetails.VALID
            AwardStatusDetails.UNSUCCESSFUL -> bid.statusDetails = StatusDetails.DISQUALIFIED

            AwardStatusDetails.PENDING,
            AwardStatusDetails.CONSIDERATION,
            AwardStatusDetails.AWAITING,
            AwardStatusDetails.NO_OFFERS_RECEIVED,
            AwardStatusDetails.LOT_CANCELLED -> throw ErrorException(
                error = ErrorType.INVALID_STATUS_DETAILS,
                message = "Current status details: '$awardStatusDetails'. Expected status details: [${AwardStatusDetails.ACTIVE}, ${AwardStatusDetails.UNSUCCESSFUL}]"
            )
        }
        bidDao.save(
            getEntity(
                bid = bid,
                cpId = cpid.toString(),
                stage = entity.stage,
                owner = entity.owner,
                token = entity.token,
                createdDate = entity.createdDate,
                pendingDate = entity.pendingDate
            )
        )
        return ResponseDto(data = BidRs(null, null, bid))
    }

    fun bidWithdrawn(cm: CommandMessage): ResponseDto {
        val cpid = cm.cpid
        val ocid = cm.ocid
        val stage = cm.stage
        val owner = cm.owner
        val token = cm.token
        val bidId = cm.ctxId
        val dateTime = cm.startDate

        periodService.checkCurrentDateInPeriod(cpid, ocid, dateTime)
        val entity = bidDao.findByCpIdAndStageAndBidId(cpid.toString(), stage, UUID.fromString(bidId))
        if (entity.token != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        val bid: Bid = toObject(Bid::class.java, entity.jsonData)
        checkStatusesBidUpdate(bid)
        bid.apply {
            date = dateTime
            status = Status.WITHDRAWN
        }
        entity.pendingDate = dateTime.toDate()
        entity.jsonData = toJson(bid)
        entity.status = bid.status.key
        bidDao.save(entity)
        return ResponseDto(data = BidRs(null, null, bid))
    }

    fun getDocsOfConsideredBid(cm: CommandMessage): ResponseDto {
        val cpid = cm.cpid
        val stage = cm.stage
        val awardCriteria = AwardCriteria.creator(
            cm.context.awardCriteria ?: throw ErrorException(ErrorType.CONTEXT)
        )
        val dto = toObject(GetDocsOfConsideredBidRq::class.java, cm.data)
        return if (awardCriteria == AwardCriteria.PRICE_ONLY && dto.consideredBidId != null) {
            val entity = bidDao.findByCpIdAndStageAndBidId(cpid.toString(), stage, UUID.fromString(dto.consideredBidId))
            val bid = toObject(Bid::class.java, entity.jsonData)
            ResponseDto(data = GetDocsOfConsideredBidRs(ConsideredBid(bid.id, bid.documents)))
        } else ResponseDto(data = "")
    }

    fun checkTokenOwner(cm: CommandMessage): ResponseDto {
        val cpid = cm.cpid
        val owner = cm.owner
        val token = cm.token
        val dto = toObject(RelatedBidRq::class.java, cm.data)
        val bidIds = dto.relatedBids

        val stage = when (cm.pmd) {
            ProcurementMethod.CD, ProcurementMethod.TEST_CD,
            ProcurementMethod.DA, ProcurementMethod.TEST_DA,
            ProcurementMethod.DC, ProcurementMethod.TEST_DC,
            ProcurementMethod.FA, ProcurementMethod.TEST_FA,
            ProcurementMethod.IP, ProcurementMethod.TEST_IP,
            ProcurementMethod.MV, ProcurementMethod.TEST_MV,
            ProcurementMethod.NP, ProcurementMethod.TEST_NP,
            ProcurementMethod.OP, ProcurementMethod.TEST_OP,
            ProcurementMethod.OT, ProcurementMethod.TEST_OT,
            ProcurementMethod.SV, ProcurementMethod.TEST_SV -> "EV"

            ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
            ProcurementMethod.RT, ProcurementMethod.TEST_RT -> "TP"

            ProcurementMethod.OF, ProcurementMethod.TEST_OF,
            ProcurementMethod.CF, ProcurementMethod.TEST_CF -> throw ErrorException(ErrorType.INVALID_PMD)
        }

        val bidEntities = bidDao.findAllByCpIdAndStage(cpid.toString(), stage)
        if (bidEntities.isEmpty()) throw ErrorException(ErrorType.BID_NOT_FOUND)
        val tokens = bidEntities.asSequence()
            .filter { bidIds.contains(it.bidId.toString()) }
            .map { it.token }.toSet()
        if (!tokens.contains(token)) throw ErrorException(ErrorType.INVALID_TOKEN)
        if (bidEntities[0].owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        return ResponseDto(data = "ok")
    }

    private fun checkStatusesBidUpdate(bid: Bid) {
        if (bid.status != Status.PENDING && bid.status != Status.INVITED) throw ErrorException(
            ErrorType.INVALID_STATUSES_FOR_UPDATE
        )
        if (bid.statusDetails != StatusDetails.EMPTY) throw ErrorException(
            ErrorType.INVALID_STATUSES_FOR_UPDATE
        )
    }

    private fun updateBidRecord(
        updatedBid: Bid,
        bidsRecordsByIds: Map<UUID, BidEntity>
    ): BidEntity {
        val bidId = UUID.fromString(updatedBid.id)
        val oldRecord = bidsRecordsByIds.get(bidId) ?: throw ErrorException(
            error = ErrorType.BID_NOT_FOUND,
            message = "Cannot find bid with id ${bidId}. Available bids : ${bidsRecordsByIds.keys}"
        )
        return oldRecord.copy(jsonData = toJson(updatedBid))
    }

    private fun getEntity(bid: Bid,
                          cpId: String,
                          stage: String,
                          owner: String,
                          token: UUID,
                          createdDate: Date,
                          pendingDate: Date?): BidEntity {
        return BidEntity(
                cpId = cpId,
                stage = stage,
                owner = owner,
                status = bid.status.key,
                bidId = UUID.fromString(bid.id),
                token = token,
                createdDate = createdDate,
                pendingDate = pendingDate,
                jsonData = toJson(bid)
        )
    }
}
