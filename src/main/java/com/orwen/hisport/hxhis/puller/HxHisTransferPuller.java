package com.orwen.hisport.hxhis.puller;

import com.orwen.hisport.hxhis.dbaccess.repository.HxHisTransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(3)
@Component
public class HxHisTransferPuller extends AbstractHxHisPatientPuller {
    @Autowired
    private HxHisTransferRepository transfers;


    @Override
    public void pull(PullRange pullRange) {

    }
}
