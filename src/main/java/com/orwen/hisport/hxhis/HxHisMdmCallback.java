package com.orwen.hisport.hxhis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orwen.hisport.hxhis.model.request.HxHisRequest;
import com.orwen.hisport.hxhis.model.response.HxHisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/hxhis/mdm/callback")
public class HxHisMdmCallback {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HxHisRecordService recordService;

    @PostMapping("/depart")
    public HxHisResponse department(@RequestBody HxHisRequest hisRequest) {
        return null;
    }

    @PostMapping("/staff")
    public HxHisResponse staff(@RequestBody HxHisRequest hisRequest) {
        return null;
    }
}
