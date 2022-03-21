package com.orwen.hisport.hxhis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.orwen.hisport.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
public class HxHisSexDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String ENABLE_CODE = "1";
    @JacksonXmlProperty(localName = "code")
    private String code;
    @JacksonXmlProperty(localName = "CTS_Code")
    private String name;
    @JacksonXmlProperty(localName = "CTS_Status")
    private String status;

    @JacksonXmlProperty(localName = "CTS_StartDate")
    private Date validStart;
    @JacksonXmlProperty(localName = "CTS_EndDate")
    private Date validEnd;

    @JsonIgnore
    public boolean isEnable() {
        return Objects.equals(status, ENABLE_CODE) &&
                DateUtils.isBetween(validStart, validEnd, new Date());
    }
}
