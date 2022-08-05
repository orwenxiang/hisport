package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.hxhis.dbaccess.HxHisPatientPO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisPatientPO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisPatientRepository;
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
@Order(2)
@Component
public class HxHisPatientPuller extends AbstractHxHisPatientPuller {
    private static final Map<String, Date> HAS_PATIENT_CACHES = new ConcurrentReferenceHashMap<>();
    private static final QHxHisPatientPO qPatient = QHxHisPatientPO.hxHisPatientPO;
    @Autowired
    private HxHisPatientRepository patients;
    @Autowired
    private TransactionRequiresNew transactionRequiresNew;

    @Override
    protected void doPull(PullRange pullRange) {
        List<HxHisPatientPO> hisPatients = patientPull(pullRange);

        if (CollectionUtils.isEmpty(hisPatients)) {
            return;
        }
        Date latestPullAt = pullRange.getEndDate();
        hisPatients.forEach(hisPatient -> transactionRequiresNew.
                executeWithoutResult(status -> processPatientEntry(hisPatient, latestPullAt)));
    }

    List<HxHisPatientPO> patientPull(PullRange pullRange) {
        return retrievePatientContent(false, "ZJ-GETINPATINFO", pullRange, new TypeReference<>() {
        });
    }


    protected void processPatientEntry(HxHisPatientPO hisPatient, Date latestPullAt) {
        String cacheKey = generateCacheKey(hisPatient);
        if (HAS_PATIENT_CACHES.containsKey(cacheKey)) {
            reportExisted(hisPatient, HAS_PATIENT_CACHES.get(cacheKey));
            return;
        }
        patients.findOne(qPatient.personId.eq(hisPatient.getPersonId())
                .and(qPatient.inHospital.eq(hisPatient.getInHospital()))).ifPresentOrElse(patientPO -> {
                    reportExisted(hisPatient, patientPO.getLatestPullAt());
                    storeRecord(hisPatient, false, latestPullAt);
                    HAS_PATIENT_CACHES.put(cacheKey, patientPO.getLatestPullAt());
                },
                () -> {
                    dispatcher.patientEntry(hisPatient);
                    reportDispatched(hisPatient, latestPullAt);
                    hisPatient.setLatestPullAt(latestPullAt);
                    patients.save(hisPatient);
                    storeRecord(hisPatient, true, latestPullAt);
                    HAS_PATIENT_CACHES.put(cacheKey, latestPullAt);
                });

    }

    private String generateCacheKey(HxHisPatientPO patientPO) {
        return patientPO.getPersonId() + (patientPO.getInHospital()
                == null ? "" : patientPO.getInHospital().getTime());
    }

    private void reportExisted(HxHisPatientPO hisPatient, Date latestPullAt) {
        log.debug("The patient entry with person id {},name {} and at {} is existed that pulled at {}",
                hisPatient.getPersonId(), hisPatient.getName(), hisPatient.getInHospital(), latestPullAt);
    }

    private void reportDispatched(HxHisPatientPO hisPatient, Date latestPullAt) {
        log.debug("The patient entry with person id {},name {} and at {} is dispatched that pulled at {}",
                hisPatient.getPersonId(), hisPatient.getName(), hisPatient.getInHospital(), latestPullAt);
    }
}
