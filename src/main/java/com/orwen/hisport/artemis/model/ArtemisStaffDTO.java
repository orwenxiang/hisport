package com.orwen.hisport.artemis.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.orwen.hisport.artemis.enums.ArtemisRole;
import com.orwen.hisport.common.enums.HisPortGender;
import com.orwen.hisport.utils.EnumStrTyped;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class ArtemisStaffDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("personId")
    private String id;
    @JsonProperty("personName")
    private String name;
    @JsonProperty("orgId")
    private String departId;
    private String certNum;

    private String phone;

    @JsonSerialize(using = EnumStrTyped.Serializer.class)
    @JsonDeserialize(using = HisPortGender.Deserializer.class)
    private HisPortGender gender;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = EnumStrTyped.Serializer.class)
    @JsonDeserialize(using = ArtemisRole.Deserializer.class)
    private ArtemisRole role;
}
