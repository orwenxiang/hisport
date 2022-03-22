package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orwen.hisport.dispatcher.HisPortDispatcher;
import com.orwen.hisport.hxhis.HxHisRecordService;
import com.orwen.hisport.hxhis.dbaccess.HxHisRecordPO;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.time.Duration;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractHxHisPatientPuller implements Runnable {
    private final PullRange pullRange;

    @Autowired
    protected HisPortDispatcher dispatcher;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected HxHisRecordService recordService;

    public final void run() {
        log.debug("Doing {} with range {}", getClass(), pullRange);
        pull(pullRange);
        log.debug("Done {} with range {}", getClass(), pullRange);
    }

    protected abstract void pull(PullRange pullRange);

    protected <T> List<T> retrievePatientContent(String methodCode, PullRange pullRange, TypeReference<T> typeReference) {
        return Collections.emptyList();
    }

    @SneakyThrows
    protected <T> void storeRecord(T content, Boolean dispatched, Date pullAt) {
        HxHisRecordPO hisRecordPO = new HxHisRecordPO();
        hisRecordPO.setType(content.getClass().getName());
        hisRecordPO.setDispatched(dispatched);
        hisRecordPO.setPullAt(pullAt);
        hisRecordPO.setContent(objectMapper.writeValueAsString(content));
        recordService.storeRecord(hisRecordPO);
    }

    public static Stream<PullRange> streamPullRange(PullRange startAt, Duration duration) {
        return Stream.iterate(startAt, current -> !current.isBiggerThanNow(), current -> current.nextDuration(duration));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString(of = {"startDate", "endDate"})
    public static class PullRange implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date startDate;
        @JsonFormat(pattern = "HH:mm:ss")
        private Date startTime;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date endDate;
        @JsonFormat(pattern = "HH:mm:ss")
        private Date endTime;

        public PullRange(Date startAt, Duration duration) {
            Calendar start = Calendar.getInstance();
            start.setTime(startAt);
            start.add(Calendar.SECOND, -1);

            this.startDate = start.getTime();
            this.startTime = this.startDate;

            this.endDate = new Date(startDate.getTime() + duration.toMillis());
            this.endTime = this.endDate;
        }

        @JsonIgnore
        public boolean isBiggerThanNow() {
            return startDate.after(new Date()) || endDate.after(new Date());
        }

        @JsonIgnore
        public PullRange nextDuration(Duration duration) {
            return new PullRange(endDate, duration);
        }
    }
}
