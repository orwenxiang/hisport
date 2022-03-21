package com.orwen.hisport.hxhis.model.common;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class HxHisHeader implements Serializable {
    private static final long serialVersionUID = 1L;
    @JacksonXmlProperty(localName = "MessageID")
    private String msgId;

    @JacksonXmlProperty(localName = "SourceSystem")
    private String source;
}