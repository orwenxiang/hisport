package com.orwen.hisport.hxhis.dbaccess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.orwen.hisport.common.dbaccess.AbstractPersistable;
import com.orwen.hisport.common.enums.HisPortGender;
import com.orwen.hisport.utils.EnumIntTyped;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@ToString
@Table(name = "hx_his_cares", indexes = {@Index(columnList = "cert_num,latest_pull_at", unique = true)})
public class HxHisCarePO extends AbstractPersistable {

    @JsonProperty("relatedPersonId")
    @Column(name = "patient_id", length = 48)
    private String patientId;

    @JsonProperty("certCard")
    @Column(name = "cert_num", length = 20)
    private String certNum;

    @JsonProperty("gender")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 32)
    @JsonSerialize(using = EnumIntTyped.Serializer.class)
    private HisPortGender gender;

    @JsonProperty("name")
    @Column(name = "name", length = 32)
    private String name;

    @JsonProperty("phone")
    @Column(name = "phone", length = 20)
    private String phone;

    @JsonProperty("bangleCode")
    @Column(name = "bangle_code", length = 20)
    private String bangleCode;

    @JsonProperty("mpNat")
    @Column(name = "mp_nat")
    private Boolean mpNat;

    @JsonIgnore
    @Column(name = "latest_pull_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date latestPullAt;
}
