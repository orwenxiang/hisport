package com.orwen.hisport.hxhis.dbaccess;

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
@Table(name = "hx_his_transfers", indexes = {@Index(columnList = "person_id,transfer_at", unique = true)})
public class HxHisTransferPO extends AbstractPersistable {

    @JsonProperty("personId")
    @Column(name = "person_id", length = 48)
    private String personId;

    @JsonProperty("originalOrgCode")
    @Column(name = "from_depart_id", length = 48)
    private String fromDepartId;

    @JsonProperty("nowOrgCode")
    @Column(name = "to_depart_id", length = 48)
    private String toDepartId;

    @JsonProperty("transferTime")
    @Column(name = "transfer_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transferAt;
}
