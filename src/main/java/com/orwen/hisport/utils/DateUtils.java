package com.orwen.hisport.utils;

import lombok.SneakyThrows;
import org.springframework.lang.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private static final ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    @SneakyThrows
    public static Date parseStartAt(String date) {
        Calendar startAt = Calendar.getInstance();
        startAt.setTime(DATE_FORMAT_THREAD_LOCAL.get().parse(date));
        return startOf(startAt);
    }

    @SneakyThrows
    public static Date parseEndAt(String date) {
        Calendar endAt = Calendar.getInstance();
        endAt.setTime(DATE_FORMAT_THREAD_LOCAL.get().parse(date));
        return endOf(endAt);
    }

    public static Date startOf(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, 1);
        return calendar.getTime();
    }

    public static Date endOf(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getMaximum(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    public static boolean isBetween(@Nullable Date from, @Nullable Date to, Date check) {
        long usingFromTime = Long.MIN_VALUE;
        if (from != null && from.getTime() > 0) {
            usingFromTime = from.getTime();
        }
        long usingToTime = Long.MAX_VALUE;
        if (to != null && to.getTime() > 0) {
            usingToTime = to.getTime();
        }
        return isBetween(usingFromTime, usingToTime, check.getTime());
    }

    private static boolean isBetween(long from, long to, long check) {
        return from <= check && check <= to;
    }
}
