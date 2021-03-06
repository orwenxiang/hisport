package com.orwen.hisport.hxhis.model.request.misc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.orwen.hisport.hxhis.model.HxHisLevel7DepartDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HxHisLevel7DepartWrapper extends AbstractHxHisWrapper<HxHisLevel7DepartDTO> {
    public static final String TYPE = "CT_DeptLevel7";

    @JacksonXmlProperty(localName = TYPE)
    @JacksonXmlElementWrapper(localName = TYPE + "List")
    private List<HxHisLevel7DepartDTO> contents;
}