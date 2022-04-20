package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisCareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        Map<String, HxHisCarePO> remoteHasCares = new HashMap<>();
        hisCares.forEach(hisCare -> remoteHasCares.put(hisCare.getCertNum(), hisCare));

        Set<String> remoteHasCareCertNums = remoteHasCares.keySet();
        Set<String> localHasCareCertNums = localHasCareCertNums();

        notIncludedIn(remoteHasCareCertNums, localHasCareCertNums).map(remoteHasCares::get).filter(Objects::nonNull)
                .forEach(hisCare -> cares.findOne(qCare.certNum.eq(hisCare.getCertNum()).and(qCare.available.isTrue()))
                        .ifPresentOrElse(carePO -> {
                            log.debug("The patient care with cert num {} and name {} is existed that pulled at {}",
                                    hisCare.getCertNum(), hisCare.getName(), latestPullAt);
                            storeRecord(hisCare, false, latestPullAt);
                        }, () -> {
                            HxHisCarePO usingHisCard = cares.findOne(qCare.certNum.eq(hisCare.getCertNum())).orElse(hisCare);
                            BeanUtils.copyProperties(hisCare, usingHisCard, "id", "version");

                            usingHisCard.setAvailable(true);
                            usingHisCard.setLatestPullAt(latestPullAt);

                            dispatcher.patientCare(usingHisCard);
                            cares.save(usingHisCard);
                            storeRecord(usingHisCard, true, latestPullAt);
                        }));

        notIncludedIn(localHasCareCertNums, remoteHasCareCertNums)
                .forEach(certNum -> cares.update().set(qCare.available, false)
                        .where(qCare.certNum.eq(certNum)).execute());
    }

    protected Set<String> localHasCareCertNums() {
        return cares.select(qCare.certNum).where(qCare.available.isTrue())
                .stream().collect(Collectors.toSet());
    }

    private static <T> Stream<T> notIncludedIn(Collection<T> checking, Collection<T> in) {
        if (CollectionUtils.isEmpty(checking) || isEqual(checking, in)) {
            return Stream.empty();
        }
        if (CollectionUtils.isEmpty(in)) {
            return checking.stream();
        }
        return checking.stream().filter(item -> !in.contains(item));
    }

    private static <T> boolean isEqual(Collection<T> one, Collection<T> two) {
        if (CollectionUtils.isEmpty(one) && CollectionUtils.isEmpty(two)) {
            return true;
        }
        if (CollectionUtils.isEmpty(one) && !CollectionUtils.isEmpty(two)
                || !CollectionUtils.isEmpty(one) && CollectionUtils.isEmpty(two)) {
            return false;
        }
        if (one.size() != two.size()) {
            return false;
        }
        return one.containsAll(two);
    }
}
