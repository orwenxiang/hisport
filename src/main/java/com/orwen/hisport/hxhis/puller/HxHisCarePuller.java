package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisCareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class HxHisCarePuller extends AbstractHxHisPatientPuller {
    private static final QHxHisCarePO qCare = QHxHisCarePO.hxHisCarePO;
    @Autowired
    private HxHisCareRepository cares;

    public HxHisCarePuller(PullRange pullRange) {
        super(pullRange);
    }

    @Override
    protected void pull(PullRange pullRange) {
        List<HxHisCarePO> hisCares = retrievePatientContent("ZJ-GETPATESCORTINFO", pullRange, new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(hisCares)) {
            return;
        }
        Date latestPullAt = pullRange.getEndDate();
        hisCares.parallelStream().forEach(hisCare -> cares.findOne(qCare.certNum.eq(hisCare.getCertNum()).and(qCare.latestPullAt.eq(latestPullAt)))
                .ifPresentOrElse(carePO -> {
                            log.debug("The patient care with cert num {} and name {} is existed that pulled at {}",
                                    hisCare.getCertNum(), hisCare.getName(), latestPullAt);
                            storeRecord(hisCare, false, latestPullAt);
                        },
                        () -> {
                            dispatcher.patientCare(hisCare);
                            hisCare.setLatestPullAt(latestPullAt);
                            cares.save(hisCare);
                            storeRecord(hisCare, true, latestPullAt);
                        }));
    }

    public static Stream<AbstractHxHisPatientPuller> stream(PullRange startAt, Duration duration) {
        return streamPullRange(startAt, duration).map(HxHisCarePuller::new);
    }
}
