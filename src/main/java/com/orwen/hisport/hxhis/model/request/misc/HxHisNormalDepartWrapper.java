package com.orwen.hisport.hxhis.model.request.misc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.orwen.hisport.hxhis.model.HxHisNormalDepartDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HxHisNormalDepartWrapper extends AbstractHxHisWrapper<HxHisNormalDepartDTO> {
    private static final long serialVersionUID = 1L;
    public static final String TYPE = "CT_DeptHierarchy";

    @JacksonXmlProperty(localName = TYPE)
    @JacksonXmlElementWrapper(localName = TYPE + "List")
    private List<HxHisNormalDepartDTO> contents;
}
