package com.orwen.hisport.artemis.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.orwen.hisport.common.enums.HisPortVirusCheck;
import com.orwen.hisport.utils.BoolToIntStrSerializer;
import com.orwen.hisport.utils.EnumStrTyped;
import com.orwen.hisport.utils.IntStrToBoolDeserializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString(callSuper = true)
public class ArtemisPatientDTO extends ArtemisStaffDTO {
    private String hospitalId;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "GMT+8")
    private Date inHospital;

    @JsonProperty("erFlag")
    @JsonSerialize(using = BoolToIntStrSerializer.class)
    @JsonDeserialize(using = IntStrToBoolDeserializer.class)
    private Boolean emergence;

    @JsonSerialize(using = EnumStrTyped.Serializer.class)
    @JsonDeserialize(using = HisPortVirusCheck.Deserializer.class)
    private HisPortVirusCheck virusCheck;

    private String bangleCode;

    private String relationName;

    private String relationPhone;

    private String wardNum;

    private String bedNum;
}
