package com.procurement.submission.infrastructure.web.response.parser

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.submission.application.service.Transform
import com.procurement.submission.domain.extension.tryUUID
import com.procurement.submission.domain.functional.Result
import com.procurement.submission.domain.functional.bind
import com.procurement.submission.infrastructure.web.api.version.ApiVersion2
import com.procurement.submission.infrastructure.enums.Command2Type
import com.procurement.submission.infrastructure.web.extension.tryGetAttributeAsEnum
import com.procurement.submission.infrastructure.web.extension.tryGetTextAttribute
import com.procurement.submission.domain.fail.Fail
import com.procurement.submission.domain.fail.error.BadRequest
import com.procurement.submission.domain.fail.error.DataErrors
import com.procurement.submission.infrastructure.web.extension.tryGetAttribute
import java.util.*

fun JsonNode.tryGetVersion(): Result<ApiVersion2, DataErrors> {
    val name = "version"
    return tryGetTextAttribute(name).bind {
        when (val result = ApiVersion2.tryValueOf(it)) {
            is Result.Success -> result
            is Result.Failure -> Result.failure(
                DataErrors.Validation.DataFormatMismatch(
                    name = name,
                    expectedFormat = "00.00.00",
                    actualValue = it
                )
            )
        }
    }
}

fun JsonNode.tryGetAction(): Result<Command2Type, DataErrors> =
    tryGetAttributeAsEnum("action", Command2Type)

fun <T : Any> JsonNode.tryGetParams(target: Class<T>, transform: Transform): Result<T, Fail.Error> {
    val name = "params"
    return tryGetAttribute(name).bind {
        when (val result = transform.tryMapping(it, target)) {
            is Result.Success -> result
            is Result.Failure -> Result.failure(
                BadRequest("Error parsing '$name'")
            )
        }
    }
}

fun <T : Any> JsonNode.tryGetData(target: Class<T>, transform: Transform): Result<T, Fail.Error> =
    when (val result = transform.tryMapping(this, target)) {
        is Result.Success -> result
        is Result.Failure -> Result.failure(
            BadRequest("Error parsing 'data'")
        )
    }

fun JsonNode.tryGetId(): Result<UUID, DataErrors> {
    val name = "id"
    return tryGetTextAttribute(name)
        .bind {
            when (val result = it.tryUUID()) {
                is Result.Success -> result
                is Result.Failure -> Result.failure(
                    DataErrors.Validation.DataFormatMismatch(
                        name = name,
                        actualValue = it,
                        expectedFormat = "uuid"
                    )
                )
            }
        }
}

fun String.tryGetNode(transform: Transform): Result<JsonNode, BadRequest> =
    when (val result = transform.tryParse(this)) {
        is Result.Success -> result
        is Result.Failure -> Result.failure(BadRequest())
    }

val NaN: UUID get() = UUID(0, 0)