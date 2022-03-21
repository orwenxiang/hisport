package com.orwen.hisport.artemis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.orwen.hisport.common.enums.HisPortVirusCheck;
import com.orwen.hisport.utils.BoolToIntSerializer;
import com.orwen.hisport.utils.EnumStrTyped;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString(callSuper = true)
public class ArtemisPatientDTO extends ArtemisStaffDTO {
    private String hospitalId;

    private String bangleCode;
    private Date inHospital;
    @JsonSerialize(using = EnumStrTyped.Serializer.class)
    private HisPortVirusCheck virusCheck;
    @JsonProperty("erFlag")
    @JsonSerialize(using = BoolToIntSerializer.class)
    private Boolean emergence;
    private String relationName;
    private String relationPhone;
    private String wardNum;
    private String bedNum;
}
