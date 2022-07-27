package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisCareRepository;
import com.orwen.hisport.utils.TransactionRequiresNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Order(4)
@Component
@Deprecated
public class HxHisCarePuller extends AbstractHxHisPatientPuller {
    private static final Map<String, Date> HAS_CARE_CACHES = new ConcurrentReferenceHashMap<>();
    private static final QHxHisCarePO qCare = QHxHisCarePO.hxHisCarePO;
    @Autowired
    private HxHisCareRepository cares;
    @Autowired
    private TransactionRequiresNew transactionRequiresNew;

    @Override
    protected synchronized void doPull(PullRange pullRange) {
        List<HxHisCarePO> hisCares = retrievePatientContent(false,
                "ZJ-GETPATESCORTINFO", pullRange, new TypeReference<>() {
                });
        if (CollectionUtils.isEmpty(hisCares)) {
            return;
        }
        Date latestPullAt = pullRange.getEndDate();

        Map<String, HxHisCarePO> remoteHasCares = new HashMap<>();
        hisCares.forEach(hisCare -> remoteHasCares.put(hisCare.getCertCard(), hisCare));

        Set<String> remoteHasCareCertNums = remoteHasCares.keySet();
        List<String> localHasCareCertNums = localHasCareCertCards();

        notIncludedIn(remoteHasCareCertNums, localHasCareCertNums).map(remoteHasCares::get).filter(Objects::nonNull)
                .forEach(hisCare -> transactionRequiresNew.executeWithoutResult(status ->
                        processHisCareWithNewTransaction(hisCare, latestPullAt)));

        transactionRequiresNew.executeWithoutResult(status ->
                notIncludedIn(localHasCareCertNums, remoteHasCareCertNums)
                        .forEach(certNum -> {
                            cares.update().set(qCare.available, false)
                                    .where(qCare.certCard.eq(certNum)).execute();
                            HAS_CARE_CACHES.remove(certNum);
                        }));
    }

    protected void processHisCareWithNewTransaction(HxHisCarePO hisCare, Date latestPullAt) {
        String cacheKey = generateCacheKey(hisCare);
        if (HAS_CARE_CACHES.containsKey(cacheKey)) {
            reportExisted(hisCare, HAS_CARE_CACHES.get(cacheKey));
            return;
        }
        cares.findOne(qCare.certCard.eq(hisCare.getCertCard()).and(qCare.available.isTrue()))
                .ifPresentOrElse(carePO -> {
                    reportExisted(hisCare, carePO.getLatestPullAt());
                    storeRecord(hisCare, false, latestPullAt);
                    HAS_CARE_CACHES.put(cacheKey, carePO.getLatestPullAt());
                }, () -> {
                    try {
                        HxHisCarePO usingHisCard = cares.findOne(qCare.certCard.eq(hisCare.getCertCard())).orElse(hisCare);
                        BeanUtils.copyProperties(hisCare, usingHisCard, "id", "version");

                        usingHisCard.setAvailable(true);
                        usingHisCard.setLatestPullAt(latestPullAt);

                        dispatcher.patientCare(usingHisCard);
                        cares.save(usingHisCard);
                        HAS_CARE_CACHES.put(cacheKey, latestPullAt);
                    } catch (Throwable e) {
                        log.warn("Ignore sync wrong", e);
                    }
                });

    }

    protected List<String> localHasCareCertCards() {
        return cares.select(qCare.certCard).where(qCare.available.isTrue())
                .fetch();
    }

    private String generateCacheKey(HxHisCarePO hisCare) {
        return hisCare.getCertCard();
    }

    private void reportExisted(HxHisCarePO hisCare, Date latestPullAt) {
        log.debug("The patient care with cert num {} and name {} is existed that pulled at {}",
                hisCare.getCertCard(), hisCare.getName(), latestPullAt);
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
