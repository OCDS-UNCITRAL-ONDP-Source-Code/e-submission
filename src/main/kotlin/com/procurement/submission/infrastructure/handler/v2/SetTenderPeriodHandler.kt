package com.procurement.submission.infrastructure.handler.v2

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.submission.application.service.Logger
import com.procurement.submission.application.service.PeriodService
import com.procurement.submission.application.service.Transform
import com.procurement.submission.domain.fail.Fail
import com.procurement.submission.infrastructure.api.tryGetParams
import com.procurement.submission.infrastructure.api.v2.CommandTypeV2
import com.procurement.submission.infrastructure.handler.HistoryRepository
import com.procurement.submission.infrastructure.handler.v2.base.AbstractHistoricalHandlerV2
import com.procurement.submission.infrastructure.handler.v2.converter.convert
import com.procurement.submission.infrastructure.handler.v2.model.request.SetTenderPeriodRequest
import com.procurement.submission.infrastructure.handler.v2.model.response.SetTenderPeriodResult
import com.procurement.submission.lib.functional.Result
import org.springframework.stereotype.Component

@Component
class SetTenderPeriodHandler(
    logger: Logger,
    historyRepository: HistoryRepository,
    transform: Transform,
    private val periodService: PeriodService
) : AbstractHistoricalHandlerV2<CommandTypeV2, SetTenderPeriodResult>(
    logger = logger,
    historyRepository = historyRepository,
    target = SetTenderPeriodResult::class.java,
    transform = transform
) {
    override val action: CommandTypeV2 = CommandTypeV2.SET_TENDER_PERIOD

    override fun execute(node: JsonNode): Result<SetTenderPeriodResult, Fail> {
        val params = node.tryGetParams(SetTenderPeriodRequest::class.java, transform = transform)
            .onFailure { return it }
            .convert()
            .onFailure { return it }

        return periodService.setTenderPeriod(params)
    }
}