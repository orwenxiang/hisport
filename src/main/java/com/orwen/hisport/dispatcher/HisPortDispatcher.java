package com.orwen.hisport.dispatcher;

import com.orwen.hisport.artemis.dbaccess.ArtemisDepartPO;
import com.orwen.hisport.artemis.enums.ArtemisRole;
import com.orwen.hisport.artemis.model.*;
import com.orwen.hisport.common.enums.HisPortGender;
import com.orwen.hisport.defs.HxPortDefs;
import com.orwen.hisport.hxhis.dbaccess.HxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.HxHisLeavePO;
import com.orwen.hisport.hxhis.dbaccess.HxHisPatientPO;
import com.orwen.hisport.hxhis.dbaccess.HxHisTransferPO;
import com.orwen.hisport.hxhis.model.HxHisStaffDTO;
import com.orwen.hisport.hxhis.model.common.HxHisCommonDepartDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
@Component
public class HisPortDispatcher {
    private static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.ofTotalSeconds(TimeZone.getTimeZone("GMT+8").getRawOffset() / 1000);
    
    @Autowired
    private RabbitOperations rabbitOperation;

    public void departChanged(HxHisCommonDepartDTO departDTO) {
        ArtemisDepartPO departPO = new ArtemisDepartPO();
        departPO.setDepartId(departDTO.getId());
        departPO.setParentId(StringUtils.hasText(departDTO.getParent()) ? departDTO.getParent() : "-1");
        departPO.setName(departDTO.getName());
        departPO.setEnabled(departDTO.isEnable());
        rabbitOperation.convertAndSend(HxPortDefs.DEPART_CHANGED_QUEUE, new ArtemisDepartPO());
    }

    public void staffChanged(HxHisStaffDTO staffDTO) {
        if (staffDTO.isEnable()) {
            ArtemisStaffDTO artemisStaffDTO = new ArtemisStaffDTO();
            artemisStaffDTO.setId(staffDTO.getId());
            artemisStaffDTO.setName(staffDTO.getName());
            artemisStaffDTO.setCertNum(staffDTO.getCertNum());
            artemisStaffDTO.setDepartId(staffDTO.getDepartId());
            artemisStaffDTO.setGender(HisPortGender.UNKNOWN);//TODO
            artemisStaffDTO.setPhone(staffDTO.getPhone());
            artemisStaffDTO.setRole(ArtemisRole.OTHER);//TODO
            rabbitOperation.convertAndSend(HxPortDefs.STAFF_JOINED_QUEUE, artemisStaffDTO);
        } else {
            ArtemisLeaveDTO leaveDTO = new ArtemisLeaveDTO();
            leaveDTO.setPersonId(staffDTO.getId());
            leaveDTO.setLeaveAt(Date.from(staffDTO.updateAt().toInstant(DEFAULT_ZONE_OFFSET)));
            rabbitOperation.convertAndSend(HxPortDefs.STAFF_LEAVED_QUEUE, leaveDTO);
        }
    }

    public void patientEntry(HxHisPatientPO patientPO) {
        ArtemisPatientDTO artemisPatientDTO = new ArtemisPatientDTO();
        BeanUtils.copyProperties(patientPO, artemisPatientDTO, "id");
        artemisPatientDTO.setId(patientPO.getPersonId());
        rabbitOperation.convertAndSend(HxPortDefs.PATIENT_JOINED_QUEUE, artemisPatientDTO);
    }

    public void patientLeave(HxHisLeavePO leavePO) {
        ArtemisLeaveDTO artemisLeaveDTO = new ArtemisLeaveDTO();
        artemisLeaveDTO.setPersonId(leavePO.getPersonId());
        artemisLeaveDTO.setLeaveAt(leavePO.getLeaveAt());
        rabbitOperation.convertAndSend(HxPortDefs.PATIENT_LEAVED_QUEUE, artemisLeaveDTO);
    }

    public void patientTransfer(HxHisTransferPO transferPO) {
        ArtemisTransferDTO transferDTO = new ArtemisTransferDTO();
        BeanUtils.copyProperties(transferPO, transferDTO, "latestPullAt");
        rabbitOperation.convertAndSend(HxPortDefs.PATIENT_TRANSFER_QUEUE, transferDTO);
    }

    public void patientCare(HxHisCarePO carePO) {
        ArtemisCareDTO artemisCareDTO = new ArtemisCareDTO();
        BeanUtils.copyProperties(carePO, artemisCareDTO, "latestPullAt");
        rabbitOperation.convertAndSend(HxPortDefs.CARE_JOINED_QUEUE, artemisCareDTO);
    }
}
