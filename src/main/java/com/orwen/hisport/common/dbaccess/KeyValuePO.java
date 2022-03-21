package com.orwen.hisport.common.dbaccess;

import com.orwen.hisport.common.enums.HisPortKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@Entity
@ToString
@Table(name = "key_values", indexes = @Index(columnList = "name", unique = true))
public class KeyValuePO extends AbstractPersistable {
    @Enumerated(EnumType.STRING)
    @Column(name = "key", length = 32)
    private HisPortKey key;

    @Column(name = "value", length = 128)
    private String value;
}
