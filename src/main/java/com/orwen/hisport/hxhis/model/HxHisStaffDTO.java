package com.orwen.hisport.hxhis.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.orwen.hisport.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
public class HxHisStaffDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JacksonXmlProperty(localName = "CTCP_Code")
    private String id;
    @JacksonXmlProperty(localName = "CTCP_Name")
    private String name;
    @JacksonXmlProperty(localName = "CTCP_IDCardNO")
    private String certNum;
    @JacksonXmlProperty(localName = "CTCP_WorkOrgCode")
    private String departId;

    @JacksonXmlProperty(localName = "CTCP_PositionStatus")
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

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JacksonXmlProperty(localName = "CTCP_UpdateDate")
    private LocalDate updateDate;

    @JsonFormat(pattern = "HH:mm:ss")
    @JacksonXmlProperty(localName = "CTCP_UpdateTime")
    private LocalTime updateTime;

    @JsonIgnore
    public boolean isEnabled(String statusCode) {
        return Objects.equals(status, statusCode) &&
                DateUtils.isBetween(validStart, validEnd, new Date());
    }

    @JsonIgnore
    public LocalDateTime updateAt() {
        if (updateTime == null) {
            return null;
        }
        return updateTime.atDate(updateDate);
    }
}
