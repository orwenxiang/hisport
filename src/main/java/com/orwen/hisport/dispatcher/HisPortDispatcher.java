package com.orwen.hisport.dispatcher;

import com.orwen.hisport.artemis.ArtemisClient;
import com.orwen.hisport.artemis.enums.ArtemisRole;
import com.orwen.hisport.artemis.model.*;
import com.orwen.hisport.autoconfigure.HisPortProperties;
import com.orwen.hisport.common.enums.HisPortGender;
import com.orwen.hisport.defs.HxPortDefs;
import com.orwen.hisport.hxhis.dbaccess.HxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.HxHisLeavePO;
import com.orwen.hisport.hxhis.dbaccess.HxHisPatientPO;
import com.orwen.hisport.hxhis.dbaccess.HxHisTransferPO;
import com.orwen.hisport.hxhis.model.HxHisStaffDTO;
import com.orwen.hisport.hxhis.model.common.HxHisCommonDepartDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HisPortDispatcher {
    @Autowired(required = false)
    private ArtemisClient artemisClient;

    @Autowired
    private HisPortProperties properties;

    private Map<ArtemisRole, List<String>> roleCodes;

    @PostConstruct
    void init() {
        roleCodes = properties.getArtemis().getRoleCodes();
    }

    public void departChanged(HxHisCommonDepartDTO departDTO) {
        ArtemisDepartDTO artemisDepart = new ArtemisDepartDTO();
        artemisDepart.setDepartId(departDTO.getId());
        artemisDepart.setParentId(StringUtils.hasText(departDTO.getParent()) ? departDTO.getParent() : "-1");
        artemisDepart.setName(departDTO.getName());
        artemisDepart.setEnabled(departDTO.isEnable());

        artemisClient.departChanged(artemisDepart);
    }

    public void staffChanged(HxHisStaffDTO staffDTO) {
        if (staffDTO.isEnabled(properties.getOnJobCode())) {
            ArtemisStaffDTO artemisStaffDTO = new ArtemisStaffDTO();
            artemisStaffDTO.setId(staffDTO.getId());
            artemisStaffDTO.setName(staffDTO.getName());
            artemisStaffDTO.setCertNum(staffDTO.getCertNum());
            artemisStaffDTO.setDepartId(staffDTO.departId());
            artemisStaffDTO.setGender(HisPortGender.ofHxHisCode(staffDTO.getSexCode()));
            artemisStaffDTO.setPhone(staffDTO.getPhone());
            artemisStaffDTO.setRole(guessStaffRole(staffDTO.getPositionCode()));

            artemisClient.staffJoin(artemisStaffDTO);
        } else {
            ArtemisLeaveDTO leaveDTO = new ArtemisLeaveDTO();
            leaveDTO.setPersonId(staffDTO.getId());
            leaveDTO.setLeaveAt(staffDTO.getRetireDate() == null ? new Date()
                    : Date.from(staffDTO.getRetireDate().atStartOfDay()
                    .toInstant(HxPortDefs.DEFAULT_ZONE_OFFSET)));

            artemisClient.staffLeave(leaveDTO);
        }
    }

    protected ArtemisRole guessStaffRole(String positionCode) {
        if (!StringUtils.hasText(positionCode)) {
            return ArtemisRole.OTHER;
        }
        return roleCodes.entrySet().stream().filter(item -> !CollectionUtils.isEmpty(item.getValue()))
                .filter(entry -> entry.getValue().contains(positionCode))
                .findFirst().map(Map.Entry::getKey).orElse(ArtemisRole.OTHER);
    }

    public void patientEntry(HxHisPatientPO patientPO) {
        ArtemisPatientDTO artemisPatientDTO = new ArtemisPatientDTO();
        BeanUtils.copyProperties(patientPO, artemisPatientDTO, "id");
        artemisPatientDTO.setId(patientPO.getPersonId());

        artemisClient.patientJoin(artemisPatientDTO);
    }

    public void patientLeave(HxHisLeavePO leavePO) {
        ArtemisLeaveDTO artemisLeaveDTO = new ArtemisLeaveDTO();
        BeanUtils.copyProperties(leavePO, artemisLeaveDTO);

        artemisClient.patientLeave(artemisLeaveDTO);
    }

    public void patientTransfer(HxHisTransferPO transferPO) {
        ArtemisTransferDTO transferDTO = new ArtemisTransferDTO();
        BeanUtils.copyProperties(transferPO, transferDTO, "latestPullAt");

        artemisClient.patientTransfer(transferDTO);
    }

    public void patientCare(HxHisCarePO carePO) {
        ArtemisCareDTO artemisCareDTO = new ArtemisCareDTO();
        BeanUtils.copyProperties(carePO, artemisCareDTO, "id", "latestPullAt", "mpNat");
        artemisCareDTO.setMpNat(Boolean.parseBoolean(carePO.getMpNat()));

        artemisClient.careJoin(artemisCareDTO);
    }
}
