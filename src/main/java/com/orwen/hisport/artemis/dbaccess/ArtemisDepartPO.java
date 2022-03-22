package com.orwen.hisport.artemis.dbaccess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.orwen.hisport.common.dbaccess.AbstractPersistable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@ToString
@Table(name = "hk_artemis_departs", indexes = {@Index(columnList = "depart_id", unique = true)})
@EqualsAndHashCode(of = {"departId", "name", "parentId", "enabled"}, callSuper = false)
public class ArtemisDepartPO extends AbstractPersistable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("orgIndexCode")
    @Column(name = "depart_id", length = 48)
    private String departId;

    @JsonProperty("orgName")
    @Column(name = "name", length = 64)
    private String name;

    @JsonProperty("parentIndexCode")
    @Column(name = "parent_id", length = 48)
    private String parentId;

    @JsonIgnore
    @Column(name = "enabled")
    public Boolean enabled;
}
