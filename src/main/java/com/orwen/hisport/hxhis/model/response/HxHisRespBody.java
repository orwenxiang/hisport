package com.orwen.hisport.hxhis.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HxHisRespBody implements Serializable {
    private static final long serialVersionUID = 1L;
    @JacksonXmlProperty(localName = "ResultCode")
    private int code = 0;

    @JacksonXmlProperty(localName = "ResultContent")
    private String content = "成功";

    public static HxHisRespBody success() {
        return new HxHisRespBody(0, "成功");
    }

    public static HxHisRespBody failed() {
        return new HxHisRespBody(-1, "失败");
    }
}
