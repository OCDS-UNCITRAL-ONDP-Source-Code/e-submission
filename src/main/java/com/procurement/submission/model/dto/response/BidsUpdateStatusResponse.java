package com.procurement.submission.model.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.submission.databinding.LocalDateTimeSerializer;
import com.procurement.submission.model.ocds.OrganizationReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
    "tenderPeriod",
    "tenderers",
    "bids"
})
public class BidsUpdateStatusResponse {

    @JsonProperty("tenderPeriod")
    private TenderPeriod tenderPeriod;

    @JsonProperty("tenderers")
    private Set<OrganizationReference> tenderers;

    @JsonProperty("bids")
    private List<BidUpdate> bids;

    @JsonCreator
    public BidsUpdateStatusResponse(@JsonProperty("tenderPeriod") @NotNull final TenderPeriod tenderPeriod,
                                    @JsonProperty("tenderers") @NotEmpty final Set<OrganizationReference> tenderers,
                                    @JsonProperty("bids") @NotEmpty final List<BidUpdate> bids) {
        this.tenderPeriod = tenderPeriod;
        this.tenderers = tenderers;
        this.bids = bids;
    }

    @Getter
    public static class TenderPeriod {

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime startDate;
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime endDate;

        @JsonCreator
        public TenderPeriod(@JsonProperty("startDate") final LocalDateTime startDate,
                            @JsonProperty("endDate") final LocalDateTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
