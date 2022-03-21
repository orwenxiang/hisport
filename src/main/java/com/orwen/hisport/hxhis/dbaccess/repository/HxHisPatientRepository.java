package com.orwen.hisport.hxhis.dbaccess.repository;

import com.orwen.hisport.common.dbaccess.repository.DBAccessRepository;
import com.orwen.hisport.hxhis.dbaccess.HxHisCarePO;
import com.orwen.hisport.hxhis.dbaccess.HxHisPatientPO;
import org.springframework.stereotype.Repository;

@Repository
public interface HxHisPatientRepository extends DBAccessRepository<HxHisPatientPO> {
}
