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

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ExecutorService;
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

    private Duration patientPullDuration;

    @PostConstruct
    void init() {
        patientPullDuration = properties.getHxHis().getPatientPullRate();
    }

    @Scheduled(fixedRateString = "${hisport.hxHis.patientPullRate}")
    public void schedulePull() {
        Lock pullLock = redissonClient.getLock("HX_HIS_PATIENT_RETRIEVER_PULLING");
        if (!pullLock.tryLock()) {
            log.warn("The schedule pull patient running other instance");
        }
        try {
            AbstractHxHisPatientPuller.PullRange latestPullAt = latestPullAt();

            HxHisPatientPuller.stream(latestPullAt, patientPullDuration).forEach(pullHxHisExecutor::submit);
            HxHisLeavePuller.stream(latestPullAt, patientPullDuration).forEach(pullHxHisExecutor::submit);
            HxHisTransferPuller.stream(latestPullAt, patientPullDuration).forEach(pullHxHisExecutor::submit);
            HxHisCarePuller.stream(latestPullAt, patientPullDuration).forEach(pullHxHisExecutor::submit);

            AbstractHxHisPatientPuller.streamPullRange(latestPullAt, patientPullDuration)
                    .max(Comparator.comparing(AbstractHxHisPatientPuller.PullRange::getEndDate))
                    .map(AbstractHxHisPatientPuller.PullRange::getEndDate)
                    .ifPresent(latestPullDate -> {
                        KeyValuePO keyValue = keyValues.findOne(qKeyValue.key.eq(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT)).orElseGet(() -> {
                            KeyValuePO keyValuePO = new KeyValuePO();
                            keyValuePO.setKey(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT);
                            return keyValuePO;
                        });
                        keyValue.setValue(String.valueOf(latestPullDate));
                        log.info("Save latest pull at {}", latestPullDate);
                        keyValues.save(keyValue);
                    });
        } finally {
            pullLock.unlock();
        }
    }

    protected AbstractHxHisPatientPuller.PullRange latestPullAt() {
        return keyValues.findOne(qKeyValue.key.eq(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT)).map(KeyValuePO::getValue)
                .map(Long::valueOf).map(Date::new)
                .map(date -> new AbstractHxHisPatientPuller.PullRange(date, patientPullDuration))
                .orElse(new AbstractHxHisPatientPuller.PullRange(properties.getHxHis().getLatestPullAt(), patientPullDuration));
    }
}
