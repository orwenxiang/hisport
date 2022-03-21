package com.orwen.hisport.hxhis.model.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.orwen.hisport.hxhis.model.common.HxHisHeader;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "Request")
public class HxHisRequest<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @JacksonXmlProperty(localName = "Header")
    private HxHisHeader header;

    @JacksonXmlProperty(localName = "Body")
    private T body;
}
