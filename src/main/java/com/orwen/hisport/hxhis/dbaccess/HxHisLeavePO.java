package com.orwen.hisport.hxhis.dbaccess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.orwen.hisport.common.dbaccess.AbstractPersistable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@ToString
@Table(name = "hx_his_leaves", indexes = {@Index(columnList = "person_id,leave_at", unique = true)})
public class HxHisLeavePO extends AbstractPersistable {

    @JsonProperty("personId")
    @Column(name = "person_id", length = 48)
    private String personId;

    @Column(name = "leave_at")
    @JsonProperty("inChargeTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date leaveAt;

    @JsonIgnore
    @Column(name = "latest_pull_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date latestPullAt;
}
