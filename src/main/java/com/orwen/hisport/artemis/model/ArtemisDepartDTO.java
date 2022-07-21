package com.orwen.hisport.artemis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Getter
@Setter
@ToString
public class ArtemisDepartDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("orgIndexCode")
    private String departId;

    @JsonProperty("orgName")
    private String name;

    @JsonProperty("parentIndexCode")
    private String parentId;
    
    @JsonProperty("statue")
    private String status;

    @JsonIgnore
    private Boolean enabled;
}
