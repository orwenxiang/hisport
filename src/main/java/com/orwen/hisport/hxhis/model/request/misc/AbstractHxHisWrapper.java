package com.orwen.hisport.hxhis.model.request.misc;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public abstract class AbstractHxHisWrapper<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract List<T> getContents();
}
