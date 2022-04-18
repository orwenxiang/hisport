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
        startAt.set(Calendar.HOUR_OF_DAY, startAt.getMinimum(Calendar.HOUR_OF_DAY));
        startAt.set(Calendar.MINUTE, startAt.getMinimum(Calendar.MINUTE));
        startAt.set(Calendar.SECOND, startAt.getMinimum(Calendar.SECOND));
        startAt.set(Calendar.MILLISECOND, 1);
        return startAt.getTime();
    }

    @SneakyThrows
    public static Date parseEndAt(String date) {
        Calendar startAt = Calendar.getInstance();
        startAt.setTime(DATE_FORMAT_THREAD_LOCAL.get().parse(date));
        startAt.set(Calendar.HOUR_OF_DAY, startAt.getMaximum(Calendar.HOUR_OF_DAY));
        startAt.set(Calendar.MINUTE, startAt.getMaximum(Calendar.MINUTE));
        startAt.set(Calendar.SECOND, startAt.getMaximum(Calendar.SECOND));
        startAt.set(Calendar.MILLISECOND, startAt.getMaximum(Calendar.MILLISECOND));
        return startAt.getTime();
    }

    public static boolean isBetween(@Nullable Date from, @Nullable Date to, Date check) {
        return isBetween(from == null ? Long.MIN_VALUE : from.getTime(),
                to == null ? Long.MAX_VALUE : to.getTime(), check.getTime());
    }

    private static boolean isBetween(long from, long to, long check) {
        return from <= check && check <= to;
    }
}
