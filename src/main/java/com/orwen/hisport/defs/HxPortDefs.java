package com.orwen.hisport.defs;

import java.time.ZoneOffset;
import java.util.TimeZone;

public interface HxPortDefs {
    String DEPART_CHANGED_QUEUE = "hisport_depart_changed";

    String STAFF_JOINED_QUEUE = "hisport_staff_joined";

    String STAFF_LEAVED_QUEUE = "hisport_staff_leaved";

    String PATIENT_JOINED_QUEUE = "hisport_patient_joined";

    String PATIENT_LEAVED_QUEUE = "hisport_patient_leaved";

    String PATIENT_TRANSFER_QUEUE = "hisport_patient_transfer";

    String CARE_JOINED_QUEUE = "hisport_care_joined";

    String PATIENT_PULLER_TOPIC = "hisport_patient_topic";
    
    ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset
            .ofTotalSeconds(TimeZone.getTimeZone("GMT+8").getRawOffset() / 1000);
}
