package com.procurement.submission.exception;

public enum ErrorType {

    BID_NOT_FOUND("00.01", "Bids not found."),
    INVALID_OWNER("00.02", "Invalid owner."),
    INVALID_ID("00.03", "Invalid bid id."),
    INVALID_RELATED_LOT("00.04", "Invalid related lots in documents."),
    PERIOD_NOT_FOUND("01.01", "Period not found."),
    INVALID_PERIOD("01.02", "Invalid period."),
    INVALID_DATE("01.03", "Date does not match the period."),
    PERIOD_NOT_EXPIRED("01.04", "Period has not yet expired."),
    INTERVAL_RULES_NOT_FOUND("02.01", "Interval rules not found."),
    BIDS_RULES_NOT_FOUND("02.02", "Bids rules not found."),
    BID_ALREADY_WITH_LOT("03.01", "We already have Bid with this Lots and Tenderers."),
    ID_NOT_NULL("03.02", "Bid id must be empty."),
    STATUS_DETAIL_IS_NULL("03.03", "Status detail must not be empty.");

    private final String code;
    private final String message;

    ErrorType(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
