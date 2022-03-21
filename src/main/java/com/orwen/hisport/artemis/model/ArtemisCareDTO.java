package com.orwen.hisport.artemis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.orwen.hisport.common.enums.HisPortGender;
import com.orwen.hisport.utils.EnumIntTyped;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class ArtemisCareDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("relatedPersonId")
    private String patientId;

    private String certNum;

    @JsonSerialize(using = EnumIntTyped.Serializer.class)
    private HisPortGender gender;

    private String name;

    private String phone;

    private String bangleCode;

    private Boolean mpNat;
}
