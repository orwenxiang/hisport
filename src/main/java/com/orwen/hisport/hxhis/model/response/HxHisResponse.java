package com.orwen.hisport.hxhis.model.response;

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
@JacksonXmlRootElement(localName = "Response")
public class HxHisResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    @JacksonXmlProperty(localName = "Header")
    private HxHisHeader header;

    @JacksonXmlProperty(localName = "Body")
    private HxHisRespBody body;

    public static HxHisResponse success(HxHisHeader header) {
        HxHisResponse response = new HxHisResponse();
        response.setHeader(header);
        response.setBody(HxHisRespBody.success());
        return response;
    }

    public static HxHisResponse failed(HxHisHeader header) {
        HxHisResponse response = new HxHisResponse();
        response.setHeader(header);
        response.setBody(HxHisRespBody.failed());
        return response;
    }
}
