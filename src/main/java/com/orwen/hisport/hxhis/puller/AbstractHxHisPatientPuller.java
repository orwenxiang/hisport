package com.orwen.hisport.hxhis.puller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.orwen.hisport.autoconfigure.HisPortProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.time.Duration;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class AbstractHxHisPatientPuller {
    @Autowired
    private HisPortProperties properties;

    public abstract void pull(PullRange pullRange);

    protected <T> List<T> retrievePatientContent(String methodCode, PullRange pullRange, TypeReference<T> typeReference) {
        return Collections.emptyList();
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
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
