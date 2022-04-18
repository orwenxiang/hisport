package com.orwen.hisport.hxhis;

import com.orwen.hisport.autoconfigure.HisPortProperties;
import com.orwen.hisport.common.dbaccess.KeyValuePO;
import com.orwen.hisport.common.dbaccess.QKeyValuePO;
import com.orwen.hisport.common.dbaccess.repository.KeyValueRepository;
import com.orwen.hisport.common.enums.HisPortKey;
import com.orwen.hisport.hxhis.puller.AbstractHxHisPatientPuller;
import com.orwen.hisport.utils.DateUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Component
public class HxHisPatientRetriever implements MessageListener<String> {
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
    @Qualifier("patientPullWeights")
    private RMap<String, Integer> patientPullWeights;

    @Autowired
    @Qualifier("patientPullerTopic")
    private RTopic patientPullerTopic;

    private Duration patientPullRange;

    private String instanceId;

    @PostConstruct
    void init() {
        patientPullRange = properties.getPull().getRange();
        instanceId = redissonClient.getId();
        patientPullerTopic.addListener(String.class, this);
    }

    @PreDestroy
    void shutdown() {
        patientPullerTopic.removeListener(this);
        patientPullWeights.getLock(instanceId).forceUnlock();
        patientPullWeights.remove(instanceId);
    }

    @Scheduled(fixedRateString = "${hisport.pull.range}")
    protected void scheduleSelectDoPullInstance() {
        patientPullWeights.put(instanceId, properties.getPull().getWeight());
        RLock currentInstanceLock = patientPullWeights.getLock(instanceId);
        if (!currentInstanceLock.isLocked()) {
            patientPullWeights.getLock(instanceId).lock();
        }

        Lock pullLock = redissonClient.getLock(RETRIEVE_PULLER_LOCK);
        if (!pullLock.tryLock()) {
            log.debug("Do pull instance id selector by other instance");
            return;
        }
        try {
            if (patientPullWeights.size() < 2) {
                patientPullerTopic.publishAsync(instanceId);
                return;
            }

            List<String> instances = new ArrayList<>();

            patientPullWeights.entrySet().stream().filter(instance -> patientPullWeights.getLock(instanceId).isLocked())
                    .forEach(entry -> IntStream.range(0, entry.getValue()).forEach(index -> instances.add(entry.getKey())));

            String selectedInstanceId = instances.get(ThreadLocalRandom.current()
                    .nextInt(0, instances.size()));

            patientPullerTopic.publish(selectedInstanceId);
        } finally {
            pullLock.unlock();
        }
    }

    @Override
    public void onMessage(CharSequence channel, String instanceId) {
        if (!Objects.equals(instanceId, this.instanceId)) {
            log.debug("The process patient pull instance {} is not current {}", instanceId, this.instanceId);
            return;
        }
        log.debug("Do patient pull in current instance");

        Lock pullLock = redissonClient.getLock(RETRIEVE_PULLER_LOCK);
        pullLock.lock();
        try {
            AbstractHxHisPatientPuller.PullRange latestPullAt = latestPullAt();
            List<AbstractHxHisPatientPuller.PullRange> pullRanges = calculatePullRanges(latestPullAt, patientPullRange);
            if (CollectionUtils.isEmpty(pullRanges)) {
                log.warn("No pull range define with latest pull at {} with duration {}", latestPullAt, patientPullRange);
                return;
            }
            hxHisPatientPullers.orderedStream().forEach(patientPuller -> doPullerWithRanges(patientPuller, pullRanges));
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
            patientPuller.pull(pullRange);
            pullCount.countDown();
        }));
        pullCount.await();
    }

    protected AbstractHxHisPatientPuller.PullRange latestPullAt() {
        return keyValues.findOne(qKeyValue.key.eq(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT)).map(KeyValuePO::getValue)
                .map(Long::valueOf).map(Date::new)
                .map(date -> new AbstractHxHisPatientPuller.PullRange(date, patientPullRange))
                .orElse(new AbstractHxHisPatientPuller.PullRange(DateUtils.parseDate(properties.getPull().getLatestAt()),
                        patientPullRange));
    }

    private List<AbstractHxHisPatientPuller.PullRange> calculatePullRanges(
            AbstractHxHisPatientPuller.PullRange startAt, Duration duration) {
        return Stream.iterate(startAt, current -> !current.isBiggerThanNow(), current -> current.nextDuration(duration)).toList();
    }
}
