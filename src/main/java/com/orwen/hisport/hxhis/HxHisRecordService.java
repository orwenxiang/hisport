package com.orwen.hisport.hxhis;

import com.orwen.hisport.autoconfigure.HisPortProperties;
import com.orwen.hisport.hxhis.dbaccess.HxHisRecordPO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisRecordPO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HxHisRecordService {
    private static final QHxHisRecordPO qRecord = QHxHisRecordPO.hxHisRecordPO;
    @Autowired
    private HxHisRecordRepository records;
    @Autowired
    private HisPortProperties properties;

    public void storeRecord(HxHisRecordPO hisRecordPO) {
        if (!properties.isStoreRecords()) {
            log.debug("Not store record {}", hisRecordPO);
            return;
        }
        records.findOne(qRecord.type.eq(hisRecordPO.getType()).and(qRecord.pullAt.eq(hisRecordPO.getPullAt())))
                .ifPresentOrElse(hisRecord -> log.debug("The hx his record type {} and pull at {}is existed ",
                                hisRecordPO.getType(), hisRecordPO.getPullAt()),
                        () -> records.save(hisRecordPO));
    }
}
