package com.orwen.hisport.dispatcher;

import com.orwen.hisport.hxhis.dbaccess.HxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.HxHisLeavePO;
import com.orwen.hisport.hxhis.dbaccess.HxHisPatientPO;
import com.orwen.hisport.hxhis.dbaccess.HxHisTransferPO;
import com.orwen.hisport.hxhis.model.HxHisStaffDTO;
import com.orwen.hisport.hxhis.model.common.HxHisCommonDepartDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HisPortDispatcher {
    @Autowired
    private RabbitOperations rabbitOperation;

    public void departChanged(HxHisCommonDepartDTO departDTO) {

    }

    public void staffChanged(HxHisStaffDTO staffDTO) {

    }

    public void patientEntry(HxHisPatientPO patientDTO) {

    }

    public void patientLeave(HxHisLeavePO leaveHospitalDTO) {

    }

    public void patientTransfer(HxHisTransferPO transferPO) {

    }

    public void patientCare(HxHisCarePO carePO) {

    }
}
