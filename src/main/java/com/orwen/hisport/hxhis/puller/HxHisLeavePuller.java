package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisLeavePO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisLeavePO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisLeaveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class HxHisLeavePuller extends AbstractHxHisPatientPuller {
    private static final QHxHisLeavePO qLeave = QHxHisLeavePO.hxHisLeavePO;
    @Autowired
    private HxHisLeaveRepository leaves;

    public HxHisLeavePuller(PullRange pullRange) {
        super(pullRange);
    }

    @Override
    protected void pull(PullRange pullRange) {
        List<HxHisLeavePO> hisLeaves = retrievePatientContent("ZJ-GETINCHARGEINFO", pullRange, new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(hisLeaves)) {
            return;
        }
        Date latestPullAt = pullRange.getEndDate();
        hisLeaves.parallelStream().forEach(hisLeave -> leaves.findOne(qLeave.personId.eq(hisLeave.getPersonId()).and(qLeave.leaveAt.eq(hisLeave.getLeaveAt())))
                .ifPresentOrElse(leavePO -> {
                            log.debug("The patient leave with id {} and at {} is existed that pulled at {}",
                                    hisLeave.getPersonId(), hisLeave.getLeaveAt(), leavePO.getLatestPullAt());
                            storeRecord(hisLeave, false, latestPullAt);
                        },
                        () -> {
                            dispatcher.patientLeave(hisLeave);
                            hisLeave.setLatestPullAt(latestPullAt);
                            leaves.save(hisLeave);
                            storeRecord(hisLeave, true, latestPullAt);
                        }));
    }

    public static Stream<AbstractHxHisPatientPuller> stream(PullRange startAt, Duration duration) {
        return streamPullRange(startAt, duration).map(HxHisLeavePuller::new);
    }
}
