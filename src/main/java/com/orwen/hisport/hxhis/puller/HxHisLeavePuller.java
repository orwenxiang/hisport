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

import java.util.Date;
import java.util.List;

@Slf4j
@Order(2)
@Component
public class HxHisLeavePuller extends AbstractHxHisPatientPuller {
    private static final QHxHisLeavePO qLeave = QHxHisLeavePO.hxHisLeavePO;
    @Autowired
    private HxHisLeaveRepository leaves;
    @Autowired
    private TransactionRequiresNew transactionRequiresNew;

    @Override
    protected void doPull(PullRange pullRange) {
        List<HxHisLeavePO> hisLeaves = retrievePatientContent(true, "ZJ-GETINCHARGEINFO", pullRange, new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(hisLeaves)) {
            return;
        }
        Date latestPullAt = pullRange.getEndDate();
        hisLeaves.forEach(hisLeave -> transactionRequiresNew.
                executeWithoutResult(status -> processHisLeave(hisLeave, latestPullAt)));
    }

    public void processHisLeave(HxHisLeavePO hisLeave, Date latestPullAt) {
        leaves.findOne(qLeave.personId.eq(hisLeave.getPersonId())
                .and(qLeave.leaveAt.eq(hisLeave.getLeaveAt()))).ifPresentOrElse(leavePO -> {
                    log.debug("The patient leave with id {} and at {} is existed that pulled at {}",
                            hisLeave.getPersonId(), hisLeave.getLeaveAt(), leavePO.getLatestPullAt());
                    storeRecord(hisLeave, false, latestPullAt);
                },
                () -> {
                    dispatcher.patientLeave(hisLeave);
                    hisLeave.setLatestPullAt(latestPullAt);
                    leaves.save(hisLeave);
                    storeRecord(hisLeave, true, latestPullAt);
                });
    }

}
