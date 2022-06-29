package com.orwen.hisport.hxhis.puller;

import com.orwen.hisport.hxhis.dbaccess.HxHisLeavePO;
import com.orwen.hisport.hxhis.dbaccess.HxHisOccurredTimeCare;
import com.orwen.hisport.hxhis.dbaccess.HxHisPatientPO;
import com.orwen.hisport.hxhis.dbaccess.HxHisTransferPO;
import com.orwen.hisport.utils.TransactionRequiresNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class HxHisComposePuller extends AbstractHxHisPatientPuller {
    @Autowired
    private HxHisLeavePuller leavePuller;

    @Autowired
    private HxHisPatientPuller patientPuller;

    @Autowired
    private HxHisTransferPuller transferPuller;

    @Autowired
    private TransactionRequiresNew transactionRequiresNew;

    @Override
    protected void doPull(PullRange pullRange) {
        List<HxHisOccurredTimeCare> occurredTimeCares = new ArrayList<>();

        occurredTimeCares.addAll(leavePuller.leavePull(pullRange));
        occurredTimeCares.addAll(patientPuller.patientPull(pullRange));
        occurredTimeCares.addAll(transferPuller.transferPull(pullRange));

        if (CollectionUtils.isEmpty(occurredTimeCares)) {
            return;
        }

        occurredTimeCares.sort(Comparator.comparing(HxHisOccurredTimeCare::getOccurredAt));

        Date latestPullAt = pullRange.getEndDate();

        occurredTimeCares.forEach(occurredTimeCare -> ignoreException(() ->
                transactionRequiresNew.executeWithoutResult(status -> {
                    if (occurredTimeCare instanceof HxHisLeavePO hxHisLeavePO) {
                        leavePuller.processHisLeave(hxHisLeavePO, latestPullAt);
                    } else if (occurredTimeCare instanceof HxHisPatientPO hxHisPatientPO) {
                        patientPuller.processPatientEntry(hxHisPatientPO, latestPullAt);
                    } else if (occurredTimeCare instanceof HxHisTransferPO hxHisTransferPO) {
                        transferPuller.processTransfer(hxHisTransferPO, latestPullAt);
                    } else {
                        log.error("Not support process type {}", occurredTimeCare);
                    }
                })));
    }

    private void ignoreException(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.warn("Failed to process {}", runnable, e);
        }
    }


}
