package com.orwen.hisport.common.dbaccess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@MappedSuperclass
@EqualsAndHashCode(of = "id")
public abstract class AbstractPersistable implements Persistable<Long>, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Version
    @Column(name = "version")
    @JsonIgnore
    private Long version;

    @Transient
    public boolean isNew() {
        return null == this.getId();
    }
}