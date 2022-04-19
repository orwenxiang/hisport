package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisPatientPO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisPatientPO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisPatientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Slf4j
@Order(1)
@Component
public class HxHisPatientPuller extends AbstractHxHisPatientPuller {
    private static final QHxHisPatientPO qPatient = QHxHisPatientPO.hxHisPatientPO;
    @Autowired
    private HxHisPatientRepository patients;

    @Override
    protected void doPull(PullRange pullRange) {
        List<HxHisPatientPO> hisPatients = retrievePatientContent(true,"ZJ-GETINPATINFO", pullRange, new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(hisPatients)) {
            return;
        }
        Date latestPullAt = pullRange.getEndDate();
        hisPatients.forEach(hisPatient -> patients.findOne(qPatient.personId.eq(hisPatient.getPersonId())
                .and(qPatient.inHospital.eq(hisPatient.getInHospital()))).ifPresentOrElse(transferPO -> {
                    log.debug("The patient entry with id {} and at {} is existed that pulled at {}",
                            hisPatient.getPersonId(), hisPatient.getInHospital(), transferPO.getLatestPullAt());
                    storeRecord(hisPatient, false, latestPullAt);
                },
                () -> {
                    dispatcher.patientEntry(hisPatient);
                    hisPatient.setLatestPullAt(latestPullAt);
                    patients.save(hisPatient);
                    storeRecord(hisPatient, true, latestPullAt);
                }));
    }
}
