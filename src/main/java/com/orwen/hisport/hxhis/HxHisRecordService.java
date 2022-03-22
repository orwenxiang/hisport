package com.orwen.hisport.hxhis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orwen.hisport.hxhis.dbaccess.HxHisRecordPO;
import com.orwen.hisport.hxhis.dbaccess.QHxHisRecordPO;
import com.orwen.hisport.hxhis.dbaccess.repository.HxHisRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public
class HxHisRecordService {
    private static final QHxHisRecordPO qRecord = QHxHisRecordPO.hxHisRecordPO;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HxHisRecordRepository records;

    public void storeRecord(HxHisRecordPO hisRecordPO) {
        records.findOne(qRecord.type.eq(hisRecordPO.getType()).and(qRecord.pullAt.eq(hisRecordPO.getPullAt())))
                .ifPresentOrElse(hisRecord -> log.debug("The hx his record type {} and pull at {}is existed ",
                                hisRecordPO.getType(), hisRecordPO.getPullAt()),
                        () -> records.save(hisRecordPO));
    }
}
