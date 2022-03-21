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
public class HxHisStaffDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String ENABLE_CODE = "1";
    @JacksonXmlProperty(localName = "CTCP_Code")
    private String id;
    @JacksonXmlProperty(localName = "CTCP_Name")
    private String name;
    @JacksonXmlProperty(localName = "CTCP_IDCardNO")
    private String certNum;
    @JacksonXmlProperty(localName = "CTCP_Status")
    private String status;

    @JacksonXmlProperty(localName = "CTCP_CareProvTypeCode")
    private String roleCode;
    @JacksonXmlProperty(localName = "CTCP_Telephone")
    private String phone;
    @JacksonXmlProperty(localName = "CTCP_SexCode")
    private String sexCode;
    @JacksonXmlProperty(localName = "CTCP_StartDate")
    private Date validStart;
    @JacksonXmlProperty(localName = "CTCP_EndDate")
    private Date validEnd;

    @JsonIgnore
    public boolean isEnable() {
        return Objects.equals(status, ENABLE_CODE) &&
                DateUtils.isBetween(validStart, validEnd, new Date());
    }
}
