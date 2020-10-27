package com.procurement.submission.infrastructure.converter

import com.procurement.submission.application.params.bid.CreateBidParams
import com.procurement.submission.application.params.parseAmount
import com.procurement.submission.application.params.parseBFDocumentType
import com.procurement.submission.application.params.parseBidId
import com.procurement.submission.application.params.parseBusinessFunctionType
import com.procurement.submission.application.params.parseCpid
import com.procurement.submission.application.params.parseDate
import com.procurement.submission.application.params.parseDocumentType
import com.procurement.submission.application.params.parseItemId
import com.procurement.submission.application.params.parseOcid
import com.procurement.submission.application.params.parseOwner
import com.procurement.submission.application.params.parseParsePersonId
import com.procurement.submission.application.params.parsePersonTitle
import com.procurement.submission.application.params.parseRequirementId
import com.procurement.submission.application.params.parseScale
import com.procurement.submission.application.params.parseTypeOfSupplier
import com.procurement.submission.application.params.rules.notEmptyRule
import com.procurement.submission.domain.extension.mapResult
import com.procurement.submission.domain.fail.error.DataErrors
import com.procurement.submission.domain.functional.Result
import com.procurement.submission.domain.functional.asSuccess
import com.procurement.submission.domain.functional.bind
import com.procurement.submission.domain.functional.validate
import com.procurement.submission.domain.model.Money
import com.procurement.submission.domain.model.Token
import com.procurement.submission.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.submission.domain.model.enums.BusinessFunctionType
import com.procurement.submission.domain.model.enums.DocumentType
import com.procurement.submission.domain.model.enums.PersonTitle
import com.procurement.submission.domain.model.enums.Scale
import com.procurement.submission.domain.model.enums.Status
import com.procurement.submission.domain.model.enums.StatusDetails
import com.procurement.submission.domain.model.enums.TypeOfSupplier
import com.procurement.submission.infrastructure.dto.bid.create.CreateBidRequest
import com.procurement.submission.infrastructure.dto.bid.create.CreateBidResult
import com.procurement.submission.model.dto.ocds.AccountIdentification
import com.procurement.submission.model.dto.ocds.AdditionalAccountIdentifier
import com.procurement.submission.model.dto.ocds.Address
import com.procurement.submission.model.dto.ocds.AddressDetails
import com.procurement.submission.model.dto.ocds.Amount
import com.procurement.submission.model.dto.ocds.BankAccount
import com.procurement.submission.model.dto.ocds.Bid
import com.procurement.submission.model.dto.ocds.BusinessFunction
import com.procurement.submission.model.dto.ocds.ContactPoint
import com.procurement.submission.model.dto.ocds.CountryDetails
import com.procurement.submission.model.dto.ocds.Details
import com.procurement.submission.model.dto.ocds.Document
import com.procurement.submission.model.dto.ocds.Identifier
import com.procurement.submission.model.dto.ocds.IssuedBy
import com.procurement.submission.model.dto.ocds.IssuedThought
import com.procurement.submission.model.dto.ocds.Item
import com.procurement.submission.model.dto.ocds.LegalForm
import com.procurement.submission.model.dto.ocds.LocalityDetails
import com.procurement.submission.model.dto.ocds.MainEconomicActivity
import com.procurement.submission.model.dto.ocds.OrganizationReference
import com.procurement.submission.model.dto.ocds.Period
import com.procurement.submission.model.dto.ocds.Permit
import com.procurement.submission.model.dto.ocds.PermitDetails
import com.procurement.submission.model.dto.ocds.Persone
import com.procurement.submission.model.dto.ocds.RegionDetails
import com.procurement.submission.model.dto.ocds.Requirement
import com.procurement.submission.model.dto.ocds.RequirementResponse
import com.procurement.submission.model.dto.ocds.ValidityPeriod
import com.procurement.submission.model.dto.ocds.Value
import java.time.LocalDateTime
import java.util.*

fun CreateBidRequest.convert(): Result<CreateBidParams, DataErrors> {
    val path = "bids"

    val cpid = parseCpid(cpid).orForwardFail { return it }
    val ocid = parseOcid(ocid).orForwardFail { return it }
    val date = parseDate(date, "$path.date").orForwardFail { return it }
    val owner = parseOwner(owner).orForwardFail { return it }
    val bids = bids.convert(path).orForwardFail { return it }

    return CreateBidParams(
        cpid = cpid,
        ocid = ocid,
        date = date,
        bids = bids,
        owner = owner
    ).asSuccess()
}

private fun CreateBidRequest.Bids.convert(path: String): Result<CreateBidParams.Bids, DataErrors> {
    val details = details.validate(notEmptyRule("$path.details"))
        .bind { it.mapResult { detail -> detail.convert("$path.details") } }
        .orForwardFail { return it }

    return CreateBidParams.Bids(
        details = details
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.convert(path: String): Result<CreateBidParams.Bids.Detail, DataErrors> {
    val id = parseBidId(id, "$path.id")
        .orForwardFail { return it }

    val items = items.validate(notEmptyRule("$path.items"))
        .bind { it.orEmpty().mapResult { item -> item.convert("$path.items") } }
        .orForwardFail { return it }

    val relatedLots = relatedLots.validate(notEmptyRule("$path.relatedLots"))
        .orForwardFail { return it }

    val value = value?.convert("$path.value")
        ?.orForwardFail { return it }

    val documents = documents.validate(notEmptyRule("$path.documents"))
        .bind { it.orEmpty().mapResult { document -> document.convert("$path.documents") } }
        .orForwardFail { return it }

    val tenderers = tenderers.validate(notEmptyRule("$path.tenderers"))
        .bind { it.mapResult { tenderer -> tenderer.convert("$path.tenderers") } }
        .orForwardFail { return it }

    val requirementResponses = requirementResponses.validate(notEmptyRule("$path.requirementResponses"))
        .bind {
            it.orEmpty()
                .mapResult { requirementResponse -> requirementResponse.convert("$path.requirementResponses") }
        }
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail(
        id = id,
        items = items,
        relatedLots = relatedLots,
        value = value,
        documents = documents,
        tenderers = tenderers,
        requirementResponses = requirementResponses
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.RequirementResponse.convert(path: String): Result<CreateBidParams.Bids.Detail.RequirementResponse, DataErrors> {
    val requirement = requirement.convert(path)
        .orForwardFail { return it }

    val period = period?.convert(path)
        ?.orForwardFail { return it }

    return CreateBidParams.Bids.Detail.RequirementResponse(
        id = id,
        requirement = requirement,
        period = period,
        value = value
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.RequirementResponse.Period.convert(path: String): Result<CreateBidParams.Bids.Detail.RequirementResponse.Period, DataErrors> {
    val startDate = parseDate(startDate, "$path.startDate")
        .orForwardFail { return it }

    val endDate = endDate.let { parseDate(it, "$path.endDate") }
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.RequirementResponse.Period(
        startDate = startDate,
        endDate = endDate
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.RequirementResponse.Requirement.convert(path: String): Result<CreateBidParams.Bids.Detail.RequirementResponse.Requirement, DataErrors> {
    val id = parseRequirementId(id, "$path.id")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.RequirementResponse.Requirement(id = id)
        .asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Tenderer.convert(path: String): Result<CreateBidParams.Bids.Detail.Tenderer, DataErrors> {
    val additionalIdentifiers = additionalIdentifiers.validate(notEmptyRule("$path.additionalIdentifiers"))
        .orForwardFail { return it }
        .orEmpty()
        .map { additionalIdentifier -> additionalIdentifier.convert() }

    val address = address.convert()
    val contactPoint = contactPoint.convert()
    val details = details.convert("$path.details").orForwardFail { return it }
    val identifier = identifier.convert()

    val persones = persones.validate(notEmptyRule("$path.persones"))
        .bind { it.orEmpty().mapResult { person -> person.convert("$path.persones") } }
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Tenderer(
        id = id,
        additionalIdentifiers = additionalIdentifiers,
        address = address,
        contactPoint = contactPoint,
        details = details,
        identifier = identifier,
        name = name,
        persones = persones
    ).asSuccess()
}

private val allowedPersonTitles = PersonTitle.allowedElements
    .filter {
        when (it) {
            PersonTitle.MR,
            PersonTitle.MRS,
            PersonTitle.MS -> true
        }
    }
    .toSet()

private fun CreateBidRequest.Bids.Detail.Tenderer.Persone.convert(path: String): Result<CreateBidParams.Bids.Detail.Tenderer.Persone, DataErrors> {
    val id = parseParsePersonId(id, "$path.id")
        .orForwardFail { return it }

    val title = parsePersonTitle(title, allowedPersonTitles, "$path.title")
        .orForwardFail { return it }

    val identifier = identifier.convert()

    val businessFunctions = businessFunctions.validate(notEmptyRule("$path.businessFunctions"))
        .bind { it.mapResult { businessFunction -> businessFunction.convert("$path.businessFunctions") } }
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Tenderer.Persone(
        id = id,
        title = title,
        name = name,
        identifier = identifier,
        businessFunctions = businessFunctions
    ).asSuccess()
}

private val allowedBusinessFunctionType = BusinessFunctionType.allowedElements
    .filter {
        when (it) {
            BusinessFunctionType.AUTHORITY,
            BusinessFunctionType.CONTACT_POINT -> true
        }
    }.toSet()

private fun CreateBidRequest.Bids.Detail.Tenderer.Persone.BusinessFunction.convert(path: String): Result<CreateBidParams.Bids.Detail.Tenderer.Persone.BusinessFunction, DataErrors> {
    val documents = documents.validate(notEmptyRule("$path.documents"))
        .bind { it.orEmpty().mapResult { document -> document.convert("$path.documents") } }
        .orForwardFail { return it }

    val type = parseBusinessFunctionType(type, allowedBusinessFunctionType, "$path.type")
        .orForwardFail { return it }

    val period = period.convert("$path.period").orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Tenderer.Persone.BusinessFunction(
        id = id,
        documents = documents,
        jobTitle = jobTitle,
        type = type,
        period = period
    ).asSuccess()
}

private val allowedBFDocumentType = BusinessFunctionDocumentType.allowedElements
    .filter {
        when (it) {
            BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> true
        }
    }
    .toSet()

private fun CreateBidRequest.Bids.Detail.Tenderer.Persone.BusinessFunction.Document.convert(path: String): Result<CreateBidParams.Bids.Detail.Tenderer.Persone.BusinessFunction.Document, DataErrors> {
    val documentType = parseBFDocumentType(documentType, allowedBFDocumentType, "$path.documentType")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Tenderer.Persone.BusinessFunction.Document(
        id = id,
        description = description,
        title = title,
        documentType = documentType
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Tenderer.Persone.BusinessFunction.Period.convert(path: String): Result<CreateBidParams.Bids.Detail.Tenderer.Persone.BusinessFunction.Period, DataErrors> {
    val startDate = parseDate(startDate, "$path.startDate")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Tenderer.Persone.BusinessFunction.Period(
        startDate = startDate
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Tenderer.Persone.Identifier.convert() = CreateBidParams.Bids.Detail.Tenderer.Persone.Identifier(
    id = id,
    uri = uri,
    scheme = scheme
)

private fun CreateBidRequest.Bids.Detail.Tenderer.Identifier.convert() = CreateBidParams.Bids.Detail.Tenderer.Identifier(
    id = id,
    scheme = scheme,
    uri = uri,
    legalName = legalName
)

private val allowedTypeOfSupplier = TypeOfSupplier.allowedElements
    .filter {
        when (it) {
            TypeOfSupplier.COMPANY,
            TypeOfSupplier.INDIVIDUAL -> true
        }
    }
    .toSet()

private val allowedScales = Scale.allowedElements
    .filter {
        when (it) {
            Scale.SME,
            Scale.LARGE,
            Scale.MICRO -> true
            Scale.EMPTY -> false
        }
    }
    .toSet()

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.convert(path: String): Result<CreateBidParams.Bids.Detail.Tenderer.Details, DataErrors> {

    val typeOfSupplier = typeOfSupplier?.let {
        parseTypeOfSupplier(it, allowedTypeOfSupplier, "$path.typeOfSupplier")
            .orForwardFail { return it }
    }

    val bankAccounts = bankAccounts.validate(notEmptyRule("$path.bankAccounts"))
        .bind { it.orEmpty().mapResult { bankAccount -> bankAccount.convert("$path.bankAccounts") } }
        .orForwardFail { return it }

    val legalForm = legalForm?.convert()

    val mainEconomicActivities = mainEconomicActivities.validate(notEmptyRule("$path.mainEconomicActivities"))
        .orForwardFail { return it }
        .orEmpty()
        .map { mainEconomicActivity -> mainEconomicActivity.convert() }

    val permits = permits.validate(notEmptyRule("$path.permits"))
        .bind { it.orEmpty().mapResult { permit -> permit.convert("$path.permits") } }
        .orForwardFail { return it }

    val scale = parseScale(scale, allowedScales, "$path.scale")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Tenderer.Details(
        typeOfSupplier = typeOfSupplier,
        bankAccounts = bankAccounts,
        legalForm = legalForm,
        mainEconomicActivities = mainEconomicActivities,
        permits = permits,
        scale = scale
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.Permit.convert(path: String): Result<CreateBidParams.Bids.Detail.Tenderer.Details.Permit, DataErrors> {
    val permitDetails = permitDetails.convert("$path.permitDetails")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Tenderer.Details.Permit(
        id = id,
        scheme = scheme,
        url = url,
        permitDetails = permitDetails
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.Permit.PermitDetails.convert(path: String): Result<CreateBidParams.Bids.Detail.Tenderer.Details.Permit.PermitDetails, DataErrors> {
    val issuedBy = issuedBy.convert()
    val issuedThought = issuedThought.convert()
    val validityPeriod = validityPeriod.convert("$path.validityPeriod")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Tenderer.Details.Permit.PermitDetails(
        issuedBy = issuedBy,
        issuedThought = issuedThought,
        validityPeriod = validityPeriod
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.Permit.PermitDetails.ValidityPeriod.convert(path: String): Result<CreateBidParams.Bids.Detail.Tenderer.Details.Permit.PermitDetails.ValidityPeriod, DataErrors> {
    val startDate = parseDate(value = startDate, attributeName = "$path.startDate")
        .orForwardFail { return it }

    val endDate = parseDate(value = endDate, attributeName = "$path.endDate")
            .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Tenderer.Details.Permit.PermitDetails.ValidityPeriod(
        startDate = startDate,
        endDate = endDate
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.Permit.PermitDetails.IssuedThought.convert() = CreateBidParams.Bids.Detail.Tenderer.Details.Permit.PermitDetails.IssuedThought(
    id = id,
    name = name
)

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.Permit.PermitDetails.IssuedBy.convert() = CreateBidParams.Bids.Detail.Tenderer.Details.Permit.PermitDetails.IssuedBy(
    id = id,
    name = name
)

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.BankAccount.convert(path: String): Result<CreateBidParams.Bids.Detail.Tenderer.Details.BankAccount, DataErrors> {
    val identifier = identifier.convert()
    val address = address.convert()
    val accountIdentification = accountIdentification.convert()

    val additionalAccountIdentifiers = additionalAccountIdentifiers.validate(notEmptyRule("$path.additionalAccountIdentifiers"))
        .orForwardFail { return it }
        .orEmpty()
        .map { bankAccount -> bankAccount.convert() }

    return CreateBidParams.Bids.Detail.Tenderer.Details.BankAccount(
        description = description,
        bankName = bankName,
        identifier = identifier,
        address = address,
        accountIdentification = accountIdentification,
        additionalAccountIdentifiers = additionalAccountIdentifiers
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.BankAccount.AdditionalAccountIdentifier.convert() = CreateBidParams.Bids.Detail.Tenderer.Details.BankAccount.AdditionalAccountIdentifier(
    id = id,
    scheme = scheme
)

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.BankAccount.AccountIdentification.convert() = CreateBidParams.Bids.Detail.Tenderer.Details.BankAccount.AccountIdentification(
    id = id,
    scheme = scheme
)

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.BankAccount.Address.convert() = CreateBidParams.Bids.Detail.Tenderer.Details.BankAccount.Address(
    streetAddress = streetAddress,
    postalCode = postalCode,
    addressDetails = CreateBidParams.Bids.Detail.Tenderer.Details.BankAccount.Address.AddressDetails(
        country = addressDetails.country.let { country ->
            CreateBidParams.Bids.Detail.Tenderer.Details.BankAccount.Address.AddressDetails.Country(
                id = country.id,
                description = country.description,
                scheme = country.scheme,
                uri = country.uri
            )
        },
        locality = addressDetails.locality.let { locality ->
            CreateBidParams.Bids.Detail.Tenderer.Details.BankAccount.Address.AddressDetails.Locality(
                id = locality.id,
                description = locality.description,
                scheme = locality.scheme,
                uri = locality.uri
            )
        },
        region = addressDetails.region.let { region ->
            CreateBidParams.Bids.Detail.Tenderer.Details.BankAccount.Address.AddressDetails.Region(
                id = region.id,
                description = region.description,
                scheme = region.scheme,
                uri = region.uri
            )
        }
    )
)

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.BankAccount.Identifier.convert() = CreateBidParams.Bids.Detail.Tenderer.Details.BankAccount.Identifier(
    id = id,
    scheme = scheme
)

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.MainEconomicActivity.convert() = CreateBidParams.Bids.Detail.Tenderer.Details.MainEconomicActivity(
    id = id,
    scheme = scheme,
    uri = uri,
    description = description
)

private fun CreateBidRequest.Bids.Detail.Tenderer.Details.LegalForm.convert() = CreateBidParams.Bids.Detail.Tenderer.Details.LegalForm(
    id = id,
    scheme = scheme,
    uri = uri,
    description = description
)

private fun CreateBidRequest.Bids.Detail.Tenderer.ContactPoint.convert() = CreateBidParams.Bids.Detail.Tenderer.ContactPoint(
    name = name,
    email = email,
    faxNumber = faxNumber,
    telephone = telephone,
    url = url
)

private fun CreateBidRequest.Bids.Detail.Tenderer.Address.convert() = CreateBidParams.Bids.Detail.Tenderer.Address(
    streetAddress = streetAddress,
    postalCode = postalCode,
    addressDetails = CreateBidParams.Bids.Detail.Tenderer.Address.AddressDetails(
        country = addressDetails.country.let { country ->
            CreateBidParams.Bids.Detail.Tenderer.Address.AddressDetails.Country(
                id = country.id,
                description = country.description,
                scheme = country.scheme,
                uri = country.uri
            )
        },
        locality = addressDetails.locality.let { locality ->
            CreateBidParams.Bids.Detail.Tenderer.Address.AddressDetails.Locality(
                id = locality.id,
                description = locality.description,
                scheme = locality.scheme,
                uri = locality.uri
            )
        },
        region = addressDetails.region.let { region ->
            CreateBidParams.Bids.Detail.Tenderer.Address.AddressDetails.Region(
                id = region.id,
                description = region.description,
                scheme = region.scheme,
                uri = region.uri
            )
        }
    )
)

private fun CreateBidRequest.Bids.Detail.Tenderer.AdditionalIdentifier.convert() = CreateBidParams.Bids.Detail.Tenderer.AdditionalIdentifier(
    id = id,
    scheme = scheme,
    legalName = legalName,
    uri = uri
)

private val allowedBidDocumentType = DocumentType.allowedElements
    .filter {
        when (it) {
            DocumentType.COMMERCIAL_OFFER,
            DocumentType.ELIGIBILITY_DOCUMENTS,
            DocumentType.ILLUSTRATION,
            DocumentType.QUALIFICATION_DOCUMENTS,
            DocumentType.SUBMISSION_DOCUMENTS,
            DocumentType.TECHNICAL_DOCUMENTS -> true
        }
    }.toSet()

private fun CreateBidRequest.Bids.Detail.Document.convert(path: String): Result<CreateBidParams.Bids.Detail.Document, DataErrors> {
    val relatedLots = relatedLots.validate(notEmptyRule("$path.relatedLots"))
        .orForwardFail { return it }
        .orEmpty()

    val documentType = parseDocumentType(documentType, allowedBidDocumentType, "$path.documentType")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Document(
        id = id,
        description = description,
        relatedLots = relatedLots,
        documentType = documentType,
        title = title
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Value.convert(path: String): Result<CreateBidParams.Bids.Detail.Value, DataErrors> {
    val amount = parseAmount(amount, "$path.amount")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Value(
        currency = currency,
        amount = amount
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Item.convert(path: String): Result<CreateBidParams.Bids.Detail.Item, DataErrors> {

    val id = parseItemId(id, "$path.id")
        .orForwardFail { return it }

    val unit = unit.convert("$path.unit")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Item(
        id = id,
        unit = unit
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Item.Unit.convert(path: String): Result<CreateBidParams.Bids.Detail.Item.Unit, DataErrors> {
    val value = value.convert("$path.value")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Item.Unit(
        value = value
    ).asSuccess()
}

private fun CreateBidRequest.Bids.Detail.Item.Unit.Value.convert(path: String): Result<CreateBidParams.Bids.Detail.Item.Unit.Value, DataErrors> {
    val amount = parseAmount(amount, "$path.value")
        .orForwardFail { return it }

    return CreateBidParams.Bids.Detail.Item.Unit.Value(
        amount = amount,
        currency = currency
    ).asSuccess()
}

fun CreateBidParams.Bids.Detail.convert(date: LocalDateTime) = Bid(
    id = id.toString(),
    status = Status.PENDING,
    statusDetails = StatusDetails.WITHDRAWN,
    date = date,
    value = value
        ?.let { value ->
            Money(
                amount = value.amount.value,
                currency = value.currency
            )
        },
    requirementResponses = requirementResponses
        .map { requirementResponse ->
            RequirementResponse(
                id = requirementResponse.id,
                value = requirementResponse.value,
                requirement = Requirement(requirementResponse.requirement.id.toString()),
                period = requirementResponse.period
                    ?.let { period ->
                        Period(
                            startDate = period.startDate,
                            endDate = period.endDate
                        )
                    },
                title = null,
                description = null
            )
        },
    tenderers = tenderers
        .map { tenderer ->
            OrganizationReference(
                id = tenderer.id,
                name = tenderer.name,
                identifier = tenderer.identifier
                    .let { identifier ->
                        Identifier(
                            id = identifier.id,
                            legalName = identifier.legalName,
                            scheme = identifier.scheme,
                            uri = identifier.uri
                        )
                    },
                additionalIdentifiers = tenderer.additionalIdentifiers
                    .map { additionalIdentifier ->
                        Identifier(
                            id = additionalIdentifier.id,
                            legalName = additionalIdentifier.legalName,
                            scheme = additionalIdentifier.scheme,
                            uri = additionalIdentifier.uri
                        )
                    }.toSet(),
                address = tenderer.address
                    .let { address ->
                        Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails
                                .let { addressDetails ->
                                    AddressDetails(
                                        country = addressDetails.country
                                            .let { country ->
                                                CountryDetails(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                        region = addressDetails.region
                                            .let { region ->
                                                RegionDetails(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                        locality = addressDetails.locality
                                            .let { locality ->
                                                LocalityDetails(
                                                    scheme = locality.scheme,
                                                    id = locality.id,
                                                    description = locality.description,
                                                    uri = locality.uri
                                                )
                                            }
                                    )
                                }
                        )
                    },
                contactPoint = tenderer.contactPoint
                    .let { contactPoint ->
                        ContactPoint(
                            name = contactPoint.name,
                            email = contactPoint.email,
                            telephone = contactPoint.telephone,
                            faxNumber = contactPoint.faxNumber,
                            url = contactPoint.url
                        )
                    },
                persones = tenderer.persones
                    .map { person ->
                        Persone(
                            id = person.id,
                            identifier = person.identifier
                                .let { identifier ->
                                    Persone.Identifier(
                                        scheme = identifier.scheme,
                                        id = identifier.id,
                                        uri = identifier.uri
                                    )
                                },
                            name = person.name,
                            title = person.title.toString(),
                            businessFunctions = person.businessFunctions
                                .map { businessFunction ->
                                    BusinessFunction(
                                        id = businessFunction.id,
                                        type = businessFunction.type,
                                        jobTitle = businessFunction.jobTitle,
                                        period = BusinessFunction.Period(startDate = businessFunction.period.startDate),
                                        documents = businessFunction.documents
                                            .map { document ->
                                                BusinessFunction.Document(
                                                    documentType = document.documentType,
                                                    id = document.id,
                                                    title = document.title,
                                                    description = document.description
                                                )
                                            }
                                    )
                                }
                        )
                    },
                details = tenderer.details
                    .let { detail ->
                        Details(
                            typeOfSupplier = detail.typeOfSupplier?.toString(),
                            mainEconomicActivities = detail.mainEconomicActivities
                                .map { mainEconomicActivity ->
                                    MainEconomicActivity(
                                        id = mainEconomicActivity.id,
                                        scheme = mainEconomicActivity.scheme,
                                        uri = mainEconomicActivity.uri,
                                        description = mainEconomicActivity.description
                                    )
                                },
                            scale = detail.scale.toString(),
                            permits = detail.permits.map { permit ->
                                Permit(
                                    scheme = permit.scheme,
                                    id = permit.id,
                                    url = permit.url,
                                    permitDetails = permit.permitDetails
                                        .let { permitDetails ->
                                            PermitDetails(
                                                issuedBy = permitDetails.issuedBy
                                                    .let { issuedBy ->
                                                        IssuedBy(
                                                            id = issuedBy.id,
                                                            name = issuedBy.name
                                                        )
                                                    },
                                                issuedThought = permitDetails.issuedThought
                                                    .let { issuedThought ->
                                                        IssuedThought(
                                                            id = issuedThought.id,
                                                            name = issuedThought.name
                                                        )
                                                    },
                                                validityPeriod = permitDetails.validityPeriod
                                                    .let { validityPeriod ->
                                                        ValidityPeriod(
                                                            startDate = validityPeriod.startDate,
                                                            endDate = validityPeriod.startDate
                                                        )
                                                    }
                                            )
                                        }
                                )
                            },
                            bankAccounts = detail.bankAccounts
                                .map { bankAccount ->
                                    BankAccount(
                                        description = bankAccount.description,
                                        bankName = bankAccount.bankName,
                                        address = bankAccount.address
                                            .let { address ->
                                                Address(
                                                    streetAddress = address.streetAddress,
                                                    postalCode = address.postalCode,
                                                    addressDetails = address.addressDetails
                                                        .let { addressDetails ->
                                                            AddressDetails(
                                                                country = addressDetails.country
                                                                    .let { country ->
                                                                        CountryDetails(
                                                                            scheme = country.scheme,
                                                                            id = country.id,
                                                                            description = country.description,
                                                                            uri = country.uri
                                                                        )
                                                                    },
                                                                region = addressDetails.region
                                                                    .let { region ->
                                                                        RegionDetails(
                                                                            scheme = region.scheme,
                                                                            id = region.id,
                                                                            description = region.description,
                                                                            uri = region.uri
                                                                        )
                                                                    },
                                                                locality = addressDetails.locality
                                                                    .let { locality ->
                                                                        LocalityDetails(
                                                                            scheme = locality.scheme,
                                                                            id = locality.id,
                                                                            description = locality.description,
                                                                            uri = locality.uri
                                                                        )
                                                                    }
                                                            )
                                                        }
                                                )
                                            },
                                        identifier = bankAccount.identifier
                                            .let { accountIdentifier ->
                                                BankAccount.Identifier(
                                                    scheme = accountIdentifier.scheme,
                                                    id = accountIdentifier.id
                                                )
                                            },
                                        accountIdentification = bankAccount.accountIdentification
                                            .let { accountIdentifier ->
                                                AccountIdentification(
                                                    scheme = accountIdentifier.scheme,
                                                    id = accountIdentifier.id
                                                )

                                            },
                                        additionalAccountIdentifiers = bankAccount.additionalAccountIdentifiers
                                            .map { accountIdentifier ->
                                                AdditionalAccountIdentifier(
                                                    scheme = accountIdentifier.scheme,
                                                    id = accountIdentifier.id
                                                )
                                            }
                                    )
                                },
                            legalForm = detail.legalForm
                                ?.let { legalForm ->
                                    LegalForm(
                                        scheme = legalForm.scheme,
                                        id = legalForm.id,
                                        description = legalForm.description,
                                        uri = legalForm.uri
                                    )
                                }
                        )
                    }
            )
        },
    relatedLots = relatedLots,
    documents = documents
        .map { document ->
            Document(
                documentType = document.documentType,
                id = document.id,
                title = document.title,
                description = document.description,
                relatedLots = document.relatedLots
            )
        },
    items = items
        .map { item ->
            Item(
                id = item.id,
                unit = item.unit.let { unit ->
                    Item.Unit(
                        value = unit.value
                            .let { value ->
                                Value(
                                    amount = value.amount.value,
                                    currency = value.currency
                                )
                            }
                    )
                }
            )
        }
)

fun Bid.convertToCreateBidResult(token: Token) = CreateBidResult(
    token = token,
    bids = CreateBidResult.Bids(
        details = listOf(
            CreateBidResult.Bids.Detail(
                id = UUID.fromString(id),
                status = status,
                statusDetails = statusDetails,
                date = date,
                value = value
                ?.let { value ->
                    CreateBidResult.Bids.Detail.Value(
                        amount = Amount(value.amount),
                        currency = value.currency
                    )
                },
                requirementResponses = requirementResponses
                ?.map { requirementResponse ->
                    CreateBidResult.Bids.Detail.RequirementResponse(
                        id = requirementResponse.id,
                        value = requirementResponse.value,
                        requirement = CreateBidResult.Bids.Detail.RequirementResponse.Requirement(
                            requirementResponse.requirement.id
                        ),
                        period = requirementResponse.period
                            ?.let { period ->
                                CreateBidResult.Bids.Detail.RequirementResponse.Period(
                                    startDate = period.startDate,
                                    endDate = period.endDate
                                )
                            }
                    )
                },
                tenderers = tenderers
                .map { tenderer ->
                    CreateBidResult.Bids.Detail.Tenderer(
                        id = tenderer.id!!,
                        name = tenderer.name,
                        identifier = tenderer.identifier
                            .let { identifier ->
                                CreateBidResult.Bids.Detail.Tenderer.Identifier(
                                    id = identifier.id,
                                    legalName = identifier.legalName,
                                    scheme = identifier.scheme,
                                    uri = identifier.uri
                                )
                            },
                        additionalIdentifiers = tenderer.additionalIdentifiers
                            ?.map { additionalIdentifier ->
                                CreateBidResult.Bids.Detail.Tenderer.AdditionalIdentifier(
                                    id = additionalIdentifier.id,
                                    legalName = additionalIdentifier.legalName,
                                    scheme = additionalIdentifier.scheme,
                                    uri = additionalIdentifier.uri
                                )
                            },
                        address = tenderer.address
                            .let { address ->
                                CreateBidResult.Bids.Detail.Tenderer.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails
                                        .let { addressDetails ->
                                            CreateBidResult.Bids.Detail.Tenderer.Address.AddressDetails(
                                                country = addressDetails.country
                                                    .let { country ->
                                                        CreateBidResult.Bids.Detail.Tenderer.Address.AddressDetails.Country(
                                                            scheme = country.scheme,
                                                            id = country.id,
                                                            description = country.description,
                                                            uri = country.uri
                                                        )
                                                    },
                                                region = addressDetails.region
                                                    .let { region ->
                                                        CreateBidResult.Bids.Detail.Tenderer.Address.AddressDetails.Region(
                                                            scheme = region.scheme,
                                                            id = region.id,
                                                            description = region.description,
                                                            uri = region.uri
                                                        )
                                                    },
                                                locality = addressDetails.locality
                                                    .let { locality ->
                                                        CreateBidResult.Bids.Detail.Tenderer.Address.AddressDetails.Locality(
                                                            scheme = locality.scheme,
                                                            id = locality.id,
                                                            description = locality.description,
                                                            uri = locality.uri
                                                        )
                                                    }
                                            )
                                        }
                                )
                            },
                        contactPoint = tenderer.contactPoint
                            .let { contactPoint ->
                                CreateBidResult.Bids.Detail.Tenderer.ContactPoint(
                                    name = contactPoint.name,
                                    email = contactPoint.email!!,
                                    telephone = contactPoint.telephone,
                                    faxNumber = contactPoint.faxNumber,
                                    url = contactPoint.url
                                )
                            },
                        persones = tenderer.persones
                            ?.map { person ->
                                CreateBidResult.Bids.Detail.Tenderer.Persone(
                                    id = person.id,
                                    identifier = person.identifier
                                        .let { identifier ->
                                            CreateBidResult.Bids.Detail.Tenderer.Persone.Identifier(
                                                scheme = identifier.scheme,
                                                id = identifier.id,
                                                uri = identifier.uri
                                            )
                                        },
                                    name = person.name,
                                    title = person.title,
                                    businessFunctions = person.businessFunctions
                                        .map { businessFunction ->
                                            CreateBidResult.Bids.Detail.Tenderer.Persone.BusinessFunction(
                                                id = businessFunction.id,
                                                type = businessFunction.type,
                                                jobTitle = businessFunction.jobTitle,
                                                period = CreateBidResult.Bids.Detail.Tenderer.Persone.BusinessFunction.Period(
                                                    startDate = businessFunction.period.startDate
                                                ),
                                                documents = businessFunction.documents
                                                    ?.map { document ->
                                                        CreateBidResult.Bids.Detail.Tenderer.Persone.BusinessFunction.Document(
                                                            documentType = document.documentType,
                                                            id = document.id,
                                                            title = document.title,
                                                            description = document.description
                                                        )
                                                    }
                                            )
                                        }
                                )
                            },
                        details = tenderer.details
                            .let { detail ->
                                CreateBidResult.Bids.Detail.Tenderer.Details(
                                    typeOfSupplier = detail.typeOfSupplier?.let { TypeOfSupplier.creator(it) },
                                    mainEconomicActivities = detail.mainEconomicActivities
                                        ?.map { mainEconomicActivity ->
                                            CreateBidResult.Bids.Detail.Tenderer.Details.MainEconomicActivity(
                                                id = mainEconomicActivity.id,
                                                scheme = mainEconomicActivity.scheme,
                                                uri = mainEconomicActivity.uri,
                                                description = mainEconomicActivity.description
                                            )
                                        },
                                    scale = Scale.creator(detail.scale),
                                    permits = detail.permits
                                        ?.map { permit ->
                                            CreateBidResult.Bids.Detail.Tenderer.Details.Permit(
                                                scheme = permit.scheme,
                                                id = permit.id,
                                                url = permit.url,
                                                permitDetails = permit.permitDetails
                                                    .let { permitDetails ->
                                                        CreateBidResult.Bids.Detail.Tenderer.Details.Permit.PermitDetails(
                                                            issuedBy = permitDetails.issuedBy
                                                                .let { issuedBy ->
                                                                    CreateBidResult.Bids.Detail.Tenderer.Details.Permit.PermitDetails.IssuedBy(
                                                                        id = issuedBy.id,
                                                                        name = issuedBy.name
                                                                    )
                                                                },
                                                            issuedThought = permitDetails.issuedThought
                                                                .let { issuedThought ->
                                                                    CreateBidResult.Bids.Detail.Tenderer.Details.Permit.PermitDetails.IssuedThought(
                                                                        id = issuedThought.id,
                                                                        name = issuedThought.name
                                                                    )
                                                                },
                                                            validityPeriod = permitDetails.validityPeriod
                                                                .let { validityPeriod ->
                                                                    CreateBidResult.Bids.Detail.Tenderer.Details.Permit.PermitDetails.ValidityPeriod(
                                                                        startDate = validityPeriod.startDate,
                                                                        endDate = validityPeriod.startDate
                                                                    )
                                                                }
                                                        )
                                                    }
                                            )
                                        },
                                    bankAccounts = detail.bankAccounts
                                        ?.map { bankAccount ->
                                            CreateBidResult.Bids.Detail.Tenderer.Details.BankAccount(
                                                description = bankAccount.description,
                                                bankName = bankAccount.bankName,
                                                address = bankAccount.address
                                                    .let { address ->
                                                        CreateBidResult.Bids.Detail.Tenderer.Details.BankAccount.Address(
                                                            streetAddress = address.streetAddress,
                                                            postalCode = address.postalCode,
                                                            addressDetails = address.addressDetails
                                                                .let { addressDetails ->
                                                                    CreateBidResult.Bids.Detail.Tenderer.Details.BankAccount.Address.AddressDetails(
                                                                        country = addressDetails.country
                                                                            .let { country ->
                                                                                CreateBidResult.Bids.Detail.Tenderer.Details.BankAccount.Address.AddressDetails.Country(
                                                                                    scheme = country.scheme,
                                                                                    id = country.id,
                                                                                    description = country.description,
                                                                                    uri = country.uri
                                                                                )
                                                                            },
                                                                        region = addressDetails.region
                                                                            .let { region ->
                                                                                CreateBidResult.Bids.Detail.Tenderer.Details.BankAccount.Address.AddressDetails.Region(
                                                                                    scheme = region.scheme,
                                                                                    id = region.id,
                                                                                    description = region.description,
                                                                                    uri = region.uri
                                                                                )
                                                                            },
                                                                        locality = addressDetails.locality
                                                                            .let { locality ->
                                                                                CreateBidResult.Bids.Detail.Tenderer.Details.BankAccount.Address.AddressDetails.Locality(
                                                                                    scheme = locality.scheme,
                                                                                    id = locality.id,
                                                                                    description = locality.description,
                                                                                    uri = locality.uri
                                                                                )
                                                                            }
                                                                    )
                                                                }
                                                        )
                                                    },
                                                identifier = bankAccount.identifier
                                                    .let { accountIdentifier ->
                                                        CreateBidResult.Bids.Detail.Tenderer.Details.BankAccount.Identifier(
                                                            scheme = accountIdentifier.scheme,
                                                            id = accountIdentifier.id
                                                        )
                                                    },
                                                accountIdentification = bankAccount.accountIdentification
                                                    .let { accountIdentifier ->
                                                        CreateBidResult.Bids.Detail.Tenderer.Details.BankAccount.AccountIdentification(
                                                            scheme = accountIdentifier.scheme,
                                                            id = accountIdentifier.id
                                                        )

                                                    },
                                                additionalAccountIdentifiers = bankAccount.additionalAccountIdentifiers
                                                    ?.map { accountIdentifier ->
                                                        CreateBidResult.Bids.Detail.Tenderer.Details.BankAccount.AdditionalAccountIdentifier(
                                                            scheme = accountIdentifier.scheme,
                                                            id = accountIdentifier.id
                                                        )
                                                    }
                                            )
                                        },
                                    legalForm = detail.legalForm
                                        ?.let { legalForm ->
                                            CreateBidResult.Bids.Detail.Tenderer.Details.LegalForm(
                                                scheme = legalForm.scheme,
                                                id = legalForm.id,
                                                description = legalForm.description,
                                                uri = legalForm.uri
                                            )
                                        }
                                )
                            }
                    )
                },
                relatedLots = relatedLots.map { UUID.fromString(it) },
                documents = documents
                ?.map { document ->
                    CreateBidResult.Bids.Detail.Document(
                        documentType = document.documentType,
                        id = document.id,
                        title = document.title!!,
                        description = document.description,
                        relatedLots = document.relatedLots?.map { UUID.fromString(it) }
                    )
                },
                items = items
                ?.map { item ->
                    CreateBidResult.Bids.Detail.Item(
                        id = item.id,
                        unit = item.unit.let { unit ->
                            CreateBidResult.Bids.Detail.Item.Unit(
                                value = unit.value
                                    .let { value ->
                                        CreateBidResult.Bids.Detail.Item.Unit.Value(
                                            amount = Amount(value.amount),
                                            currency = value.currency
                                        )
                                    }
                            )
                        }
                    )
                }
        )
        )
    )
)

