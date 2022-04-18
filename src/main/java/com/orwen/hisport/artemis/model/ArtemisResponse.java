package com.orwen.hisport.artemis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
public class ArtemisResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SUCCESS_CODE = "0";

    private String code;

    @JsonProperty("msg")
    private String message;

    @JsonProperty("data")
    private T content;

    @JsonIgnore
    public boolean isSuccess() {
        return Objects.equals(code, SUCCESS_CODE);
    }
}
