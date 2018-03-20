package com.procurement.submission.model.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.submission.databinding.LocalDateTimeDeserializer;
import com.procurement.submission.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "date",
    "status",
    "statusDetails",
    "tenderers",
    "value",
    "documents",
    "relatedLots"
})
public class Bid {
    @JsonProperty("id")
    private String id;

    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime date;

    @JsonProperty("status")
    private Status status;

    @JsonProperty("statusDetails")
    private StatusDetails statusDetails;

    @Valid
    @JsonProperty("tenderers")
    private List<OrganizationReference> tenderers;

    @Valid
    @JsonProperty("value")
    private Value value;

    @Valid
    @JsonProperty("documents")
    private List<Document> documents;

    @JsonProperty("relatedLots")
    private List<String> relatedLots;

    @JsonCreator
    public Bid(@JsonProperty("id") final String id,
               @JsonProperty("date") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime date,
               @JsonProperty("status") final Status status,
               @JsonProperty("statusDetails") final StatusDetails statusDetails,
               @JsonProperty("tenderers") final List<OrganizationReference> tenderers,
               @JsonProperty("value") final Value value,
               @JsonProperty("documents") final List<Document> documents,
               @JsonProperty("relatedLots") final List<String> relatedLots) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.statusDetails = statusDetails;
        this.tenderers = tenderers;
        this.value = value;
        this.documents = documents;
        this.relatedLots = relatedLots;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(date)
                                    .append(status)
                                    .append(tenderers)
                                    .append(value)
                                    .append(documents)
                                    .append(relatedLots)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Bid)) {
            return false;
        }
        final Bid bqd = (Bid) obj;
        return new EqualsBuilder().append(id, bqd.id)
                                  .append(date, bqd.date)
                                  .append(status, bqd.status)
                                  .append(tenderers, bqd.tenderers)
                                  .append(value, bqd.value)
                                  .append(documents, bqd.documents)
                                  .append(relatedLots, bqd.relatedLots)
                                  .isEquals();
    }

    public enum Status {
        INVITED("invited"),
        PENDING("pending"),
        VALID("valid"),
        DISQUALIFIED("disqualified"),
        WITHDRAWN("withdrawn");

        private static final Map<String, Status> CONSTANTS = new HashMap<String, Status>();
        private final String value;

        static {
            for (final Status c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Status(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Status fromValue(final String value) {
            final Status constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(
                        "Unknown enum type " + value + ", Allowed values are " + Arrays.toString(values()));
            }
            return constant;
        }
    }

    public enum StatusDetails {
        DISQUALIFIED("disqualified"),
        VALID("valid"),
        EMPTY("empty");

        private static final Map<String, StatusDetails> CONSTANTS = new HashMap<String, StatusDetails>();
        private final String value;

        static {
            for (final StatusDetails c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        StatusDetails(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static StatusDetails fromValue(final String value) {
            final StatusDetails constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(
                        "Unknown enum type " + value + ", Allowed values are " + Arrays.toString(values()));
            }
            return constant;
        }
    }
}