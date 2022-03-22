package com.orwen.hisport.hxhis.dbaccess;

import com.orwen.hisport.common.dbaccess.AbstractPersistable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@ToString
@Table(name = "hx_his_sexes")
public class HxHisSexPO extends AbstractPersistable {
    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "name", length = 32)
    private String name;
}
