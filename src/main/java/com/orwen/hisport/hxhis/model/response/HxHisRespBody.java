package com.orwen.hisport.hxhis.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class HxHisRespBody implements Serializable {
    private static final long serialVersionUID = 1L;
    @JacksonXmlProperty(localName = "ResultCode")
    private int code = 0;

    @JacksonXmlProperty(localName = "ResultContent")
    private String content = "成功";
}
