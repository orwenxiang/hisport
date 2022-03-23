package com.orwen.hisport.hxhis.model.request.misc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.orwen.hisport.hxhis.model.HxHisSexDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HxHisSexWrapper extends AbstractHxHisWrapper<HxHisSexDTO> {
    public static final String TYPE = "CT_Sex";

    @JacksonXmlProperty(localName = TYPE)
    @JacksonXmlElementWrapper(localName = TYPE + "List")
    private List<HxHisSexDTO> contents;
}