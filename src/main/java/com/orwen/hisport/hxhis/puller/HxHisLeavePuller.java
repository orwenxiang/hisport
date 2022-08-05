package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisLeavePO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisLeavePO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisLeaveRepository;
import com.orwen.hisport.utils.TransactionRequiresNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Order(1)
@Component
public class HxHisLeavePuller extends AbstractHxHisPatientPuller {
    private static final Map<String, Date> HAS_LEAVE_CACHES = new ConcurrentReferenceHashMap<>();
    private static final QHxHisLeavePO qLeave = QHxHisLeavePO.hxHisLeavePO;
    @Autowired
    private HxHisLeaveRepository leaves;
    @Autowired
    private TransactionRequiresNew transactionRequiresNew;

    @Override
    protected void doPull(PullRange pullRange) {
        List<HxHisLeavePO> hisLeaves = leavePull(pullRange);
        if (CollectionUtils.isEmpty(hisLeaves)) {
            return;
        }
        Date latestPullAt = pullRange.getEndDate();
        hisLeaves.forEach(hisLeave -> transactionRequiresNew.
                executeWithoutResult(status -> processHisLeave(hisLeave, latestPullAt)));
    }

    List<HxHisLeavePO> leavePull(PullRange pullRange) {
        return retrievePatientContent(true, "ZJ-GETINCHARGEINFO", pullRange, new TypeReference<>() {
        });
    }

    protected void processHisLeave(HxHisLeavePO hisLeave, Date latestPullAt) {
        String cacheKey = generateCacheKey(hisLeave);
        if (HAS_LEAVE_CACHES.containsKey(cacheKey)) {
            reportExisted(hisLeave, HAS_LEAVE_CACHES.get(cacheKey));
            return;
        }
        leaves.findOne(qLeave.personId.eq(hisLeave.getPersonId())
                .and(qLeave.leaveAt.eq(hisLeave.getLeaveAt()))).ifPresentOrElse(leavePO -> {
                    reportExisted(hisLeave, leavePO.getLatestPullAt());
                    storeRecord(hisLeave, false, latestPullAt);
                    HAS_LEAVE_CACHES.put(cacheKey, leavePO.getLatestPullAt());
                },
                () -> {
                    dispatcher.patientLeave(hisLeave);
                    reportDispatched(hisLeave, latestPullAt);
                    hisLeave.setLatestPullAt(latestPullAt);
                    leaves.save(hisLeave);
                    storeRecord(hisLeave, true, latestPullAt);
                    HAS_LEAVE_CACHES.put(cacheKey, latestPullAt);
                });
    }

    private String generateCacheKey(HxHisLeavePO hisLeave) {
        return hisLeave.getPersonId() + (hisLeave.getLeaveAt()
                == null ? "" : hisLeave.getLeaveAt().getTime());
    }

    private void reportExisted(HxHisLeavePO hisLeave, Date latestPullAt) {
        log.debug("The patient leave with person id {} and at {} is existed that pulled at {}",
                hisLeave.getPersonId(), hisLeave.getLeaveAt(), latestPullAt);
    }

    private void reportDispatched(HxHisLeavePO hisLeave, Date latestPullAt) {
        log.debug("The patient leave with person id {} and at {} is dispatched that pulled at {}",
                hisLeave.getPersonId(), hisLeave.getLeaveAt(), latestPullAt);
    }
}
