package com.procurement.submission.model.dto.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.submission.application.model.data.RequirementRsValue
import com.procurement.submission.domain.model.Money
import com.procurement.submission.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.submission.domain.model.enums.BusinessFunctionType
import com.procurement.submission.domain.model.enums.DocumentType
import com.procurement.submission.infrastructure.bind.criteria.RequirementValueDeserializer
import com.procurement.submission.infrastructure.bind.criteria.RequirementValueSerializer
import com.procurement.submission.infrastructure.bind.money.MoneyDeserializer
import com.procurement.submission.infrastructure.bind.money.MoneySerializer
import com.procurement.submission.model.dto.databinding.JsonDateDeserializer
import com.procurement.submission.model.dto.databinding.JsonDateSerializer
import java.time.LocalDateTime
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class BidUpdateRequest(
    @field:JsonProperty("bid") @param:JsonProperty("bid") val bid: Bid
) {
    data class Bid(
        @field:JsonProperty("tenderers") @param:JsonProperty("tenderers") val tenderers: List<Tenderer>,

        @JsonDeserialize(using = MoneyDeserializer::class)
        @JsonSerialize(using = MoneySerializer::class)
        @field:JsonProperty("value") @param:JsonProperty("value") val value: Money,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<Document>?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("requirementResponses") @param:JsonProperty("requirementResponses") val requirementResponses: List<RequirementResponse>?,

        @field:JsonProperty("relatedLots") @param:JsonProperty("relatedLots") val relatedLots: List<String>
    ) {
        class Tenderer (
            @field:JsonProperty("id") @param:JsonProperty("id") val id: UUID,

            @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("additionalIdentifiers") @param:JsonProperty("additionalIdentifiers") val additionalIdentifiers: List<AdditionalIdentifier>?,

            @field:JsonProperty("details") @param:JsonProperty("details") val details: Details,

            @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("persones") @param:JsonProperty("persones") val persones: List<Persone>?

        ) {
            data class AdditionalIdentifier(
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("legalName") @param:JsonProperty("legalName") val legalName: String,

                @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
            )

            data class Details(
                @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
                @field:JsonProperty("permits") @param:JsonProperty("permits") val permits: List<Permit>?,

                @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
                @field:JsonProperty("bankAccounts") @param:JsonProperty("bankAccounts") val bankAccounts: List<BankAccount>?,

                @field:JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("legalForm") @param:JsonProperty("legalForm") val legalForm: LegalForm?
            ) {
                data class LegalForm(
                    @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                    @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

                    @field:JsonInclude(JsonInclude.Include.NON_NULL)
                    @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
                )

                data class BankAccount(
                    @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                    @field:JsonProperty("bankName") @param:JsonProperty("bankName") val bankName: String,
                    @field:JsonProperty("address") @param:JsonProperty("address") val address: Address,
                    @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier,
                    @field:JsonProperty("accountIdentification") @param:JsonProperty("accountIdentification") val accountIdentification: AccountIdentification,

                    @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
                    @field:JsonProperty("additionalAccountIdentifiers") @param:JsonProperty("additionalAccountIdentifiers") val additionalAccountIdentifiers: List<AdditionalAccountIdentifier>?
                ) {
                    data class Identifier(
                        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String
                    )

                    data class AdditionalAccountIdentifier(
                        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String
                    )

                    data class Address(
                        @field:JsonProperty("streetAddress") @param:JsonProperty("streetAddress") val streetAddress: String,

                        @field:JsonInclude(JsonInclude.Include.NON_NULL)
                        @field:JsonProperty("postalCode") @param:JsonProperty("postalCode") val postalCode: String?,

                        @field:JsonProperty("addressDetails") @param:JsonProperty("addressDetails") val addressDetails: AddressDetails
                    ) {
                        data class AddressDetails(
                            @field:JsonProperty("country") @param:JsonProperty("country") val country: Country,
                            @field:JsonProperty("region") @param:JsonProperty("region") val region: Region,
                            @field:JsonProperty("locality") @param:JsonProperty("locality") val locality: Locality
                        ) {
                            data class Locality(
                                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

                                @field:JsonInclude(JsonInclude.Include.NON_NULL)
                                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
                            )

                            data class Region(
                                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
                            )

                            data class Country(
                                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
                            )
                        }
                    }

                    data class AccountIdentification(
                        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String
                    )
                }

                data class Permit(
                    @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                    @field:JsonInclude(JsonInclude.Include.NON_NULL)
                    @field:JsonProperty("url") @param:JsonProperty("url") val url: String?,

                    @field:JsonProperty("permitDetails") @param:JsonProperty("permitDetails") val permitDetails: PermitDetails
                ) {
                    data class PermitDetails(
                        @field:JsonProperty("issuedBy") @param:JsonProperty("issuedBy") val issuedBy: IssuedBy,
                        @field:JsonProperty("issuedThought") @param:JsonProperty("issuedThought") val issuedThought: IssuedThought,
                        @field:JsonProperty("validityPeriod") @param:JsonProperty("validityPeriod") val validityPeriod: ValidityPeriod
                    ) {
                        data class IssuedBy(
                            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                            @field:JsonProperty("name") @param:JsonProperty("name") val name: String
                        )

                        data class IssuedThought(
                            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                            @field:JsonProperty("name") @param:JsonProperty("name") val name: String
                        )

                        data class ValidityPeriod(
                            @JsonDeserialize(using = JsonDateDeserializer::class)
                            @JsonSerialize(using = JsonDateSerializer::class)
                            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

                            @JsonDeserialize(using = JsonDateDeserializer::class)
                            @JsonSerialize(using = JsonDateSerializer::class)
                            @field:JsonInclude(JsonInclude.Include.NON_NULL)
                            @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime?
                        )
                    }
                }
            }

            data class Persone(
                @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
                @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
                @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier,
                @field:JsonProperty("businessFunctions") @param:JsonProperty("businessFunctions") val businessFunctions: List<BusinessFunction>
            ) {
                data class BusinessFunction(
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                    @field:JsonProperty("type") @param:JsonProperty("type") val type: BusinessFunctionType,
                    @field:JsonProperty("jobTitle") @param:JsonProperty("jobTitle") val jobTitle: String,
                    @field:JsonProperty("period") @param:JsonProperty("period") val period: Period,

                    @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
                    @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<Document>?
                ) {
                    data class Period(
                        @JsonDeserialize(using = JsonDateDeserializer::class)
                        @JsonSerialize(using = JsonDateSerializer::class)
                        @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
                    )

                    data class Document(
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                        @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: BusinessFunctionDocumentType,
                        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

                        @field:JsonInclude(JsonInclude.Include.NON_NULL)
                        @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
                    )
                }

                data class Identifier(
                    @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                    @field:JsonInclude(JsonInclude.Include.NON_NULL)
                    @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
                )
            }

        }

        data class Document(
            @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: DocumentType,
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
            @field:JsonProperty("relatedLots") @param:JsonProperty("relatedLots") val relatedLots: List<String>
        )

        data class RequirementResponse(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

            @JsonDeserialize(using = RequirementValueDeserializer::class)
            @JsonSerialize(using = RequirementValueSerializer::class)
            @field:JsonProperty("value") @param:JsonProperty("value") val value: RequirementRsValue,

            @field:JsonProperty("requirement") @param:JsonProperty("requirement") val requirement: Requirement,
            @field:JsonProperty("period") @param:JsonProperty("period") val period: Period
        ) {
            data class Period(
                @JsonDeserialize(using = JsonDateDeserializer::class)
                @JsonSerialize(using = JsonDateSerializer::class)
                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

                @JsonDeserialize(using = JsonDateDeserializer::class)
                @JsonSerialize(using = JsonDateSerializer::class)
                @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
            )

            data class Requirement(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String
            )
        }
    }
}

