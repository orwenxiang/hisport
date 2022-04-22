package com.orwen.hisport.hxhis.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.orwen.hisport.hxhis.model.common.HxHisCommonDepartDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HxHisLevel7DepartDTO extends HxHisCommonDepartDTO {
    
    @JacksonXmlProperty(localName = "CTD_Admin3Code")
    private String parent;

    @JacksonXmlProperty(localName = "CTD_Admin3Desc")
    private String parentName;

    @JacksonXmlProperty(localName = "CTD_Admin2Code")
    private String fiveCode;

    @JacksonXmlProperty(localName = "CTD_Admin2Desc")
    private String fiveName;

    @JacksonXmlProperty(localName = "CTD_Admin1Code")
    private String fourCode;

    @JacksonXmlProperty(localName = "CTD_Admin1Desc")
    private String fourName;
}
