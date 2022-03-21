package com.orwen.hisport.hxhis.puller;

import com.orwen.hisport.hxhis.dbaccess.repository.HxHisLeaveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.stream.Stream;

@Slf4j
public class HxHisLeavePuller extends AbstractHxHisPatientPuller {
    @Autowired
    private HxHisLeaveRepository leaves;

    public HxHisLeavePuller(PullRange pullRange) {
        super(pullRange);
    }

    @Override
    protected void pull(PullRange pullRange) {

    }

    public static Stream<AbstractHxHisPatientPuller> stream(PullRange startAt, Duration duration) {
        return streamPullRange(startAt, duration).map(HxHisLeavePuller::new);
    }
}
