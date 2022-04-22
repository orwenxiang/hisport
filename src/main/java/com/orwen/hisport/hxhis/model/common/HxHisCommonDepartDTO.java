package com.orwen.hisport.hxhis.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public abstract class HxHisCommonDepartDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String ENABLE_CODE = "1";

    @JacksonXmlProperty(localName = "CTD_Code")
    private String id;

    @JacksonXmlProperty(localName = "CTD_Desc")
    private String name;

    @JacksonXmlProperty(localName = "CTD_Status")
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JacksonXmlProperty(localName = "CTD_StartDate")
    private Date validStart;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JacksonXmlProperty(localName = "CTD_EndDate")
    private Date validEnd;

    @JsonIgnore
    public boolean isEnable() {
        return Objects.equals(status, ENABLE_CODE) &&
                DateUtils.isBetween(validStart, validEnd, new Date());
    }

    public abstract void setParent(String parent);

    public abstract String getParent();
}
