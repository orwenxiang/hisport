package com.orwen.hisport.hxhis;

import com.orwen.hisport.autoconfigure.HisPortProperties;
import com.orwen.hisport.common.dbaccess.KeyValuePO;
import com.orwen.hisport.common.dbaccess.QKeyValuePO;
import com.orwen.hisport.common.dbaccess.repository.KeyValueRepository;
import com.orwen.hisport.common.enums.HisPortKey;
import com.orwen.hisport.hxhis.puller.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

@Slf4j
@Component
public class HxHisPatientRetriever {
    private static final QKeyValuePO qKeyValue = QKeyValuePO.keyValuePO;
    @Autowired
    private HisPortProperties properties;

    @Autowired
    private RedissonClient redissonClient;

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
            AbstractHxHisPatientPuller.PullRange latestPullAt = latestPullAt();
            AtomicReference<AbstractHxHisPatientPuller.PullRange> latestPullRange = new AtomicReference<>();

            HxHisPatientPuller.stream(latestPullAt, properties.getHxHis().getPatientPullRate()).forEach(pullHxHisExecutor::submit);
            HxHisLeavePuller.stream(latestPullAt, properties.getHxHis().getPatientPullRate()).forEach(pullHxHisExecutor::submit);
            HxHisTransferPuller.stream(latestPullAt, properties.getHxHis().getPatientPullRate()).forEach(pullHxHisExecutor::submit);
            HxHisCarePuller.stream(latestPullAt, properties.getHxHis().getPatientPullRate())
                    .peek(puller -> latestPullRange.set(puller.getPullRange())).forEach(pullHxHisExecutor::submit);

            if (latestPullRange.get() != null) {
                KeyValuePO keyValue = keyValues.findOne(qKeyValue.key.eq(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT)).orElseGet(() -> {
                    KeyValuePO keyValuePO = new KeyValuePO();
                    keyValuePO.setKey(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT);
                    return keyValuePO;
                });
                Date latestPullTime = latestPullRange.get().getEndTime();
                keyValue.setValue(String.valueOf(latestPullRange.get().getEndDate().getTime()));
                log.info("Save latest pull at {}", latestPullTime);
                keyValues.save(keyValue);
            }
        } finally {
            pullLock.unlock();
        }
    }

    protected AbstractHxHisPatientPuller.PullRange latestPullAt() {
        return keyValues.findOne(qKeyValue.key.eq(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT)).map(KeyValuePO::getValue)
                .map(Long::valueOf).map(Date::new)
                .map(date -> new AbstractHxHisPatientPuller.PullRange(date, properties.getHxHis().getPatientPullRate()))
                .orElse(new AbstractHxHisPatientPuller.PullRange(properties.getHxHis().getLatestPullAt(), properties.getHxHis().getPatientPullRate()));
    }
}
