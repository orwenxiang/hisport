package com.orwen.hisport.hxhis;

import com.orwen.hisport.autoconfigure.HisPortProperties;
import com.orwen.hisport.common.dbaccess.KeyValuePO;
import com.orwen.hisport.common.dbaccess.QKeyValuePO;
import com.orwen.hisport.common.dbaccess.repository.KeyValueRepository;
import com.orwen.hisport.common.enums.HisPortKey;
import com.orwen.hisport.hxhis.puller.AbstractHxHisPatientPuller;
import com.orwen.hisport.hxhis.puller.HxHisCarePuller;
import com.orwen.hisport.utils.DateUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "hisport.pull", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HxHisPatientRetriever {
    private static final String RETRIEVE_PULLER_LOCK = "HX_HIS_PATIENT_RETRIEVER_PULLING";
    private static final QKeyValuePO qKeyValue = QKeyValuePO.keyValuePO;

    @Autowired
    private HisPortProperties properties;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ExecutorService patientPullExecutor;

    @Autowired
    private KeyValueRepository keyValues;

    @Autowired
    private ObjectProvider<AbstractHxHisPatientPuller> hxHisPatientPullers;

    @Autowired
    private HxHisCarePuller carePuller;

    private Duration patientPullRange;

    private Duration patientPullExtendIn;

    @PostConstruct
    void init() {
        patientPullRange = properties.getPull().getRange();
        patientPullExtendIn = properties.getPull().getExtendIn();
    }


    @Scheduled(fixedRateString = "${hisport.pull.range}")
    public void scheduleSelectDoPullInstance() {
        log.debug("Do patient pull");

        Lock pullLock = redissonClient.getLock(RETRIEVE_PULLER_LOCK);
        pullLock.lock();
        try {
            AbstractHxHisPatientPuller.PullRange latestPullAt = latestPullAt();
            List<AbstractHxHisPatientPuller.PullRange> pullRanges = calculatePullRanges(latestPullAt, patientPullRange);
            if (CollectionUtils.isEmpty(pullRanges)) {
                log.warn("No pull range define with latest pull at {} with duration {}", latestPullAt, patientPullRange);
                return;
            } else {
                log.debug("Do patient pull with ranges {}", pullRanges);
            }
            hxHisPatientPullers.orderedStream().filter(item -> !(item instanceof HxHisCarePuller))
                    .forEach(patientPuller -> doPullerWithRanges(patientPuller, pullRanges));

            carePuller.pull(HxHisCarePuller.currentPullRange());

            Date latestPullDate = pullRanges.get(pullRanges.size() - 1).getEndDate();
            KeyValuePO keyValue = keyValues.findOne(qKeyValue.key.eq(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT))
                    .orElseGet(() -> {
                        KeyValuePO keyValuePO = new KeyValuePO();
                        keyValuePO.setKey(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT);
                        return keyValuePO;
                    });
            keyValue.setValue(String.valueOf(latestPullDate.getTime()));
            log.info("Save latest pull at {}", latestPullDate);
            keyValues.save(keyValue);
        } finally {
            pullLock.unlock();
        }
    }

    @SneakyThrows
    protected void doPullerWithRanges(
            AbstractHxHisPatientPuller patientPuller, List<AbstractHxHisPatientPuller.PullRange> pullRanges) {
        if (pullRanges.size() < 2) {
            patientPuller.pull(pullRanges.get(0));
            return;
        }
        CountDownLatch pullCount = new CountDownLatch(pullRanges.size());
        pullRanges.forEach(pullRange -> patientPullExecutor.submit(() -> {
            patientPuller.pull(pullRange.extendIn(patientPullExtendIn));
            pullCount.countDown();
        }));
        pullCount.await();
    }

    protected AbstractHxHisPatientPuller.PullRange latestPullAt() {
        return keyValues.findOne(qKeyValue.key.eq(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT)).map(KeyValuePO::getValue)
                .map(Long::valueOf).map(Date::new)
                .map(date -> new AbstractHxHisPatientPuller.PullRange(date, patientPullRange))
                .orElse(new AbstractHxHisPatientPuller.PullRange(DateUtils
                        .parseStartAt(properties.getPull().getLatestAt()), patientPullRange));
    }

    private List<AbstractHxHisPatientPuller.PullRange> calculatePullRanges(
            AbstractHxHisPatientPuller.PullRange startAt, Duration duration) {

        Date maxPullAt = StringUtils.hasText(properties.getPull().getMaxPullAt()) ?
                DateUtils.parseEndAt(properties.getPull().getMaxPullAt()) : new Date();

        return Stream.iterate(startAt, current -> !current.isBiggerThan(maxPullAt),
                current -> current.nextDuration(duration)).toList();
    }
}
