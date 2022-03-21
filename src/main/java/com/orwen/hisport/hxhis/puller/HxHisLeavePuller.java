package com.orwen.hisport.hxhis.puller;

import com.orwen.hisport.hxhis.dbaccess.repository.HxHisLeaveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(2)
@Component
public class HxHisLeavePuller extends AbstractHxHisPatientPuller {
    @Autowired
    private HxHisLeaveRepository leaves;


    @Override
    public void pull(PullRange pullRange) {

    }
}
