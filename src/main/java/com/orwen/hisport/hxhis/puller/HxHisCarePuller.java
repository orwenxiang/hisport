package com.orwen.hisport.hxhis.puller;

import com.orwen.hisport.hxhis.dbaccess.repository.HxHisCareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(4)
@Component
public class HxHisCarePuller extends AbstractHxHisPatientPuller {
    @Autowired
    private HxHisCareRepository cares;
    
    @Override
    public void pull(PullRange pullRange) {

    }
}
