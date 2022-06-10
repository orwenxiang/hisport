package com.orwen.hisport.hxhis.dbaccess;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.orwen.hisport.common.dbaccess.AbstractPersistable;
import com.orwen.hisport.common.enums.HisPortGender;
import com.orwen.hisport.common.enums.HisPortVirusCheck;
import com.orwen.hisport.utils.BoolToIntStrSerializer;
import com.orwen.hisport.utils.EnumStrTyped;
import com.orwen.hisport.utils.IntStrToBoolDeserializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@ToString
@Table(name = "hx_his_patients", indexes = {@Index(columnList = "person_id,in_hospital", unique = true)})
public class HxHisPatientPO extends AbstractPersistable {
    @JsonProperty("personId")
    @Column(name = "person_id", length = 48)
    private String personId;

    @JsonProperty("personName")
    @Column(name = "name", length = 32)
    private String name;

    @JsonProperty("orgCode")
    @Column(name = "depart_id", length = 48)
    private String departId;

    @JsonProperty("certNum")
    @Column(name = "cert_num", length = 20)
    private String certNum;

    @JsonProperty("phone")
    @Column(name = "phone", length = 64)
    private String phone;

    @JsonProperty("gender")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    @JsonSerialize(using = EnumStrTyped.Serializer.class)
    @JsonDeserialize(using = HisPortGender.Deserializer.class)
    private HisPortGender gender;

    @JsonProperty("hospitalId")
    @Column(name = "hospital_id", length = 20)
    private String hospitalId;

    @JsonProperty("bangleCode")
    @Column(name = "bangle_code", length = 20)
    private String bangleCode;

    @JsonProperty("inHospital")
    @Column(name = "in_hospital")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date inHospital;

    @JsonProperty("virusCheck")
    @Enumerated(EnumType.STRING)
    @Column(name = "virus_check", length = 32)
    @JsonSerialize(using = EnumStrTyped.Serializer.class)
    @JsonDeserialize(using = HisPortVirusCheck.Deserializer.class)
    private HisPortVirusCheck virusCheck;

    @JsonProperty("erFlag")
    @Column(name = "emergence")
    @JsonSerialize(using = BoolToIntStrSerializer.class)
    @JsonDeserialize(using = IntStrToBoolDeserializer.class)
    private Boolean emergence;

    @JsonProperty("relationName")
    @Column(name = "relation_name", length = 32)
    private String relationName;

    @JsonProperty("relationPhone")
    @Column(name = "relation_phone", length = 20)
    private String relationPhone;

    @JsonProperty("wardNum")
    @Column(name = "ward_num", length = 32)
    private String wardNum;

    @JsonProperty("bedNum")
    @Column(name = "bed_num", length = 32)
    private String bedNum;

    @JsonIgnore
    @Column(name = "latest_pull_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date latestPullAt;
}