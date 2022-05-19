package com.orwen.hisport.hxhis.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.orwen.hisport.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
public class HxHisStaffDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @JacksonXmlProperty(localName = "CTCP_JobNumber")
    private String id;
    @JacksonXmlProperty(localName = "CTCP_Name")
    private String name;
    @JacksonXmlProperty(localName = "CTCP_IDCardNO")
    private String certNum;
    @JacksonXmlProperty(localName = "CTCP_DeptHierarchyCode")
    private String adminDepartId;
    @JacksonXmlProperty(localName = "CTCP_WorkOrgCode")
    private String workDepartId;

    @JacksonXmlProperty(localName = "CTCP_PositionStatus")
    private String status;

    @JacksonXmlProperty(localName = "CTCP_PositionSeqCode")
    private String positionCode;

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

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JacksonXmlProperty(localName = "CTCP_RetireDate")
    private LocalDate retireDate;

    @JsonIgnore
    public boolean isEnabled(String statusCode) {
        return Objects.equals(status, statusCode)
                && DateUtils.isBetween(validStart, validEnd, new Date())
                && retireDate == null;
    }

    @JsonIgnore
    public String departId() {
        if (StringUtils.hasText(workDepartId)) {
            return workDepartId;
        }
        return adminDepartId;
    }
}
