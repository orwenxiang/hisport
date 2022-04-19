package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisCareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Slf4j
@Order(4)
@Component
public class HxHisCarePuller extends AbstractHxHisPatientPuller {
    private static final QHxHisCarePO qCare = QHxHisCarePO.hxHisCarePO;
    @Autowired
    private HxHisCareRepository cares;

    @Override
    protected void doPull(PullRange pullRange) {
        List<HxHisCarePO> hisCares = retrievePatientContent(false, "ZJ-GETPATESCORTINFO", pullRange, new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(hisCares)) {
            return;
        }
        Date latestPullAt = pullRange.getEndDate();
        hisCares.forEach(hisCare -> cares.findOne(qCare.certNum.eq(hisCare.getCertNum())
                .and(qCare.latestPullAt.eq(latestPullAt))).ifPresentOrElse(carePO -> {
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
}
