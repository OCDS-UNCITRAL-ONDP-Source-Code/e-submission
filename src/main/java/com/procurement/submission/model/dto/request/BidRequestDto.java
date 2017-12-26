package com.procurement.submission.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.submission.model.ocds.Bid;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
    "ocid",
    "stage",
    "bid"
})
public class BidRequestDto {

    @NotNull
    @JsonProperty("ocid")
    private String ocid;

    @NotNull
    @JsonProperty("stage")
    private String stage;

    @NotNull
    @JsonProperty("bid")
    private Bid bid;

    @JsonCreator
    public BidRequestDto(@JsonProperty("ocid") final String ocid,
                         @JsonProperty("stage") final String stage,
                         @JsonProperty("bid") final Bid bid) {
        this.ocid = ocid;
        this.stage = stage;
        this.bid = bid;
    }
}