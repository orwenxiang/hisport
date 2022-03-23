package com.orwen.hisport.hxhis.model.request.misc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.orwen.hisport.hxhis.model.HxHisStaffDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HxHisStaffWrapper extends AbstractHxHisWrapper<HxHisStaffDTO> {
    public static final String TYPE = "CT_CareProv";

    @JacksonXmlProperty(localName = TYPE)
    @JacksonXmlElementWrapper(localName = TYPE + "List")
    private List<HxHisStaffDTO> contents;
}