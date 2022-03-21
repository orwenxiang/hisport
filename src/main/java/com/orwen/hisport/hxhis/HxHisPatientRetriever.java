package com.orwen.hisport.hxhis;

import com.orwen.hisport.common.dbaccess.repository.KeyValueRepository;
import com.orwen.hisport.hxhis.puller.AbstractHxHisPatientPuller;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;

@Slf4j
@Component
public class HxHisPatientRetriever {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ObjectProvider<AbstractHxHisPatientPuller> patientPullers;

    @Autowired
    private ExecutorService pullHxHisExecutor;

    @Autowired
    private KeyValueRepository keyValues;

    @Scheduled(fixedRateString = "${hisport.hxHis.patientPullRate}")
    public void schedulePull() {
        Lock pullLock = redissonClient.getLock("HX_HIS_PATIENT_RETRIEVER_PULLING");
        if (!pullLock.tryLock()) {
            log.warn("The schedule pull patient running other instance");
        }
        try {


        } finally {
            pullLock.unlock();
        }
    }

    protected AbstractHxHisPatientPuller.PullRange latestPullAt() {
        return null;
    }
}
