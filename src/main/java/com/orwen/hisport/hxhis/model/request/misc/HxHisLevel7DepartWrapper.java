package com.orwen.hisport.hxhis.model.request.misc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.orwen.hisport.hxhis.model.HxHisNormalDepartDTO;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HxHisLevel7DepartWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    @JacksonXmlProperty(localName = "CT_DeptLevel7")
    @JacksonXmlElementWrapper(localName = "CT_DeptLevel7List")
    private List<HxHisNormalDepartDTO> departs;
}