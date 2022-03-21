package com.orwen.hisport.hxhis.puller;

import com.orwen.hisport.hxhis.dbaccess.repository.HxHisPatientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.stream.Stream;

@Slf4j
public class HxHisPatientPuller extends AbstractHxHisPatientPuller {

    @Autowired
    private HxHisPatientRepository patients;

    public HxHisPatientPuller(PullRange pullRange) {
        super(pullRange);
    }


    @Override
    protected void pull(PullRange pullRange) {

    }

    public static Stream<AbstractHxHisPatientPuller> stream(PullRange startAt, Duration duration) {
        return streamPullRange(startAt, duration).map(HxHisPatientPuller::new);
    }
}
