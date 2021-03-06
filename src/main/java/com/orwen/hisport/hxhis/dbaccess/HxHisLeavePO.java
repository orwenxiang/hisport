package com.orwen.hisport.hxhis.dbaccess;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class HxHisLeavePO extends AbstractPersistable implements HxHisOccurredTimeCare {

    @JsonProperty("personId")
    @Column(name = "person_id", length = 48)
    private String personId;

    @Column(name = "leave_at")
    @JsonProperty("inChargeTime")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaveAt;

    @JsonIgnore
    @Column(name = "latest_pull_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date latestPullAt;

    @Override
    @Transient
    @org.springframework.data.annotation.Transient
    public Date getOccurredAt() {
        return leaveAt == null ? latestPullAt : leaveAt;
    }
}
