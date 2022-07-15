package com.orwen.hisport.hxhis.puller;

import com.orwen.hisport.common.dbaccess.KeyValuePO;
import com.orwen.hisport.common.dbaccess.QKeyValuePO;
import com.orwen.hisport.common.dbaccess.repository.KeyValueRepository;
import com.orwen.hisport.common.enums.HisPortKey;
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
    private static final QKeyValuePO qKeyValue = QKeyValuePO.keyValuePO;

    @Autowired
    private HxHisLeavePuller leavePuller;

    @Autowired
    private HxHisPatientPuller patientPuller;

    @Autowired
    private HxHisTransferPuller transferPuller;

    @Autowired
    private TransactionRequiresNew transactionRequiresNew;

    @Autowired
    private KeyValueRepository keyValues;

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
        
        persistLatestPullAt(latestPullAt);
    }

    public void persistLatestPullAt(Date latestPullAt) {
        try {
            KeyValuePO keyValue = keyValues.findOne(qKeyValue.key.eq(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT))
                    .orElseGet(() -> {
                        KeyValuePO keyValuePO = new KeyValuePO();
                        keyValuePO.setKey(HisPortKey.HX_HIS_LATEST_PULL_PATIENT_AT);
                        return keyValuePO;
                    });
            keyValue.setValue(String.valueOf(latestPullAt.getTime()));
            log.info("Save latest pull at {}", latestPullAt);
            keyValues.save(keyValue);
        } catch (Throwable e) {
            log.warn("Ignore persist latest pull at wrong", e);
        }
    }

    private void ignoreException(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.warn("Failed to process {}", runnable, e);
        }
    }


}
