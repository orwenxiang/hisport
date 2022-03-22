package com.orwen.hisport.hxhis.dbaccess;

import com.orwen.hisport.common.dbaccess.AbstractPersistable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@ToString
@Table(name = "hx_his_records", indexes = @Index(columnList = "type , pull_at desc", unique = true))
public class HxHisRecordPO extends AbstractPersistable {
    @Column(name = "type", length = 64)
    private String type;

    @Column(name = "dispatched")
    private Boolean dispatched;

    @Column(name = "pull_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date pullAt;

    @Type(type = "text")
    @Column(name = "content")
    private String content;
}
