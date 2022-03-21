package com.orwen.hisport.artemis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class ArtemisTransferDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String personId;

    @JsonProperty("originalOrgId")
    private String fromDepartId;

    @JsonProperty("nowOrgId")
    private String toDepartId;

    @JsonProperty("transferTime")
    private Date transferAt;
}
