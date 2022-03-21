package com.orwen.hisport.hxhis.puller;

import com.orwen.hisport.hxhis.dbaccess.repository.HxHisPatientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(1)
@Component
public class HxHisPatientPuller extends AbstractHxHisPatientPuller {

    @Autowired
    private HxHisPatientRepository patients;


    @Override
    public void pull(PullRange pullRange) {

    }
}
