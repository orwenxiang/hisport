package com.orwen.hisport.hxhis.dbaccess;

import javax.persistence.Transient;
import java.util.Date;

public interface HxHisOccurredTimeCare {
    @Transient
    @org.springframework.data.annotation.Transient
    Date getOccurredAt();
}
