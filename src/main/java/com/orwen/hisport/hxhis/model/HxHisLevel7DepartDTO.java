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
    @JacksonXmlProperty(localName = "CTD_BelongDept")
    private String parent;
}
