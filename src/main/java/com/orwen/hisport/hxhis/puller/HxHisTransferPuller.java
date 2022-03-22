package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisTransferPO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisTransferPO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisTransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class HxHisTransferPuller extends AbstractHxHisPatientPuller {
    private static final QHxHisTransferPO qTransfer = QHxHisTransferPO.hxHisTransferPO;
    @Autowired
    private HxHisTransferRepository transfers;

    public HxHisTransferPuller(PullRange pullRange) {
        super(pullRange);
    }

    @Override
    protected void pull(PullRange pullRange) {
        List<HxHisTransferPO> hisTransfers = retrievePatientContent("ZJ-GETPATTRANSFERINFO", pullRange, new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(hisTransfers)) {
            return;
        }
        Date latestPullAt = pullRange.getEndDate();
        hisTransfers.parallelStream().forEach(hisTransfer -> transfers.findOne(qTransfer.personId.eq(hisTransfer.getPersonId()).and(qTransfer.transferAt.eq(hisTransfer.getTransferAt())))
                .ifPresentOrElse(transferPO -> {
                            log.debug("The patient transfer with id {} and at {} is existed that pulled at {}",
                                    hisTransfer.getPersonId(), hisTransfer.getTransferAt(), transferPO.getLatestPullAt());
                            storeRecord(hisTransfer, false, latestPullAt);
                        },
                        () -> {
                            dispatcher.patientTransfer(hisTransfer);
                            hisTransfer.setLatestPullAt(latestPullAt);
                            transfers.save(hisTransfer);
                            storeRecord(hisTransfer, true, latestPullAt);
                        }));
    }

    public static Stream<AbstractHxHisPatientPuller> stream(PullRange startAt, Duration duration) {
        return streamPullRange(startAt, duration).map(HxHisTransferPuller::new);
    }
}
