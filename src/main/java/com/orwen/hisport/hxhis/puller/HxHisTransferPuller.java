package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisTransferPO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisTransferPO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisTransferRepository;
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
@Order(3)
@Component
public class HxHisTransferPuller extends AbstractHxHisPatientPuller {
    private static final Map<String, Date> HAS_TRANSFER_CACHES = new ConcurrentReferenceHashMap<>();

    private static final QHxHisTransferPO qTransfer = QHxHisTransferPO.hxHisTransferPO;
    @Autowired
    private HxHisTransferRepository transfers;
    @Autowired
    private TransactionRequiresNew transactionRequiresNew;

    @Override
    protected void doPull(PullRange pullRange) {
        List<HxHisTransferPO> hisTransfers = transferPull(pullRange);
        if (CollectionUtils.isEmpty(hisTransfers)) {
            return;
        }
        Date latestPullAt = pullRange.getEndDate();
        hisTransfers.forEach(hisTransfer -> transactionRequiresNew.executeWithoutResult(status ->
                processTransfer(hisTransfer, latestPullAt)));
    }

    List<HxHisTransferPO> transferPull(PullRange pullRange) {
        return retrievePatientContent(false, "ZJ-GETPATTRANSFERINFO",
                pullRange, new TypeReference<>() {
                });
    }

    protected void processTransfer(HxHisTransferPO hisTransfer, Date latestPullAt) {
        String cacheKey = generateCacheKey(hisTransfer);
        if (HAS_TRANSFER_CACHES.containsKey(cacheKey)) {
            reportExisted(hisTransfer, HAS_TRANSFER_CACHES.get(cacheKey));
            return;
        }
        transfers.findOne(qTransfer.personId.eq(hisTransfer.getPersonId())
                .and(qTransfer.transferAt.eq(hisTransfer.getTransferAt()))).ifPresentOrElse(transferPO -> {
                    reportExisted(hisTransfer, transferPO.getLatestPullAt());
                    storeRecord(hisTransfer, false, latestPullAt);
                    HAS_TRANSFER_CACHES.put(cacheKey, transferPO.getLatestPullAt());
                },
                () -> {
                    dispatcher.patientTransfer(hisTransfer);
                    hisTransfer.setLatestPullAt(latestPullAt);
                    transfers.save(hisTransfer);
                    storeRecord(hisTransfer, true, latestPullAt);
                    HAS_TRANSFER_CACHES.put(cacheKey, latestPullAt);
                });
    }

    private String generateCacheKey(HxHisTransferPO hisTransfer) {
        return hisTransfer.getPersonId() + (hisTransfer.getTransferAt()
                == null ? "" : hisTransfer.getTransferAt().getTime());
    }

    private void reportExisted(HxHisTransferPO transferPO, Date latestPullAt) {
        log.debug("The patient transfer with id {} and at {} is existed that pulled at {}",
                transferPO.getPersonId(), transferPO.getTransferAt(), latestPullAt);
    }
}
