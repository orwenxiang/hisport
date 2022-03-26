package com.orwen.hisport.utils;

import lombok.SneakyThrows;
import org.springframework.lang.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static final ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    @SneakyThrows
    public static Date parseDate(String date) {
        return DATE_FORMAT_THREAD_LOCAL.get().parse(date);
    }

    public static boolean isBetween(@Nullable Date from, @Nullable Date to, Date check) {
        return isBetween(from == null ? Long.MIN_VALUE : from.getTime(),
                to == null ? Long.MAX_VALUE : to.getTime(), check.getTime());
    }

    private static boolean isBetween(long from, long to, long check) {
        return from <= check && check <= to;
    }
}
