package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisCareRepository;
import com.orwen.hisport.utils.DateUtils;
import com.orwen.hisport.utils.TransactionRequiresNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Order(4)
@Component
public class HxHisCarePuller extends AbstractHxHisPatientPuller {
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
                        .forEach(certNum -> cares.update().set(qCare.available, false)
                                .where(qCare.certCard.eq(certNum)).execute()));
    }

    protected void processHisCareWithNewTransaction(HxHisCarePO hisCare, Date latestPullAt) {
        cares.findOne(qCare.certCard.eq(hisCare.getCertCard()).and(qCare.available.isTrue()))
                .ifPresentOrElse(carePO -> {
                    log.debug("The patient care with cert num {} and name {} is existed that pulled at {}",
                            hisCare.getCertCard(), hisCare.getName(), latestPullAt);
                    storeRecord(hisCare, false, latestPullAt);
                }, () -> {
                    try {
                        HxHisCarePO usingHisCard = cares.findOne(qCare.certCard.eq(hisCare.getCertCard())).orElse(hisCare);
                        BeanUtils.copyProperties(hisCare, usingHisCard, "id", "version");

                        usingHisCard.setAvailable(true);
                        usingHisCard.setLatestPullAt(latestPullAt);

                        dispatcher.patientCare(usingHisCard);
                        cares.save(usingHisCard);
                    } catch (Throwable e) {
                        log.warn("Ignore sync wrong", e);
                    }
                });

    }


    public static PullRange currentPullRange() {
        PullRange pullRange = new PullRange();

        Date startAt = DateUtils.startOf(Calendar.getInstance());
        Date endAt = DateUtils.endOf(Calendar.getInstance());

        pullRange.setStartDate(startAt);
        pullRange.setStartTime(startAt);
        pullRange.setEndDate(endAt);
        pullRange.setEndTime(endAt);
        return pullRange;
    }

    protected List<String> localHasCareCertCards() {
        return cares.select(qCare.certCard).where(qCare.available.isTrue())
                .fetch();
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
