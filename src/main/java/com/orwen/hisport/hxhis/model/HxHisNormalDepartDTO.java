package com.orwen.hisport.hxhis.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.orwen.hisport.hxhis.model.common.HxHisCommonDepartDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HxHisNormalDepartDTO extends HxHisCommonDepartDTO {
    @JacksonXmlProperty(localName = "CTD_Alias")
    private String name;

    @JacksonXmlProperty(localName = "CTD_ParentCode")
    private String parent;
}
