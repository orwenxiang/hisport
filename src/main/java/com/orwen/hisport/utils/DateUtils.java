package com.orwen.hisport.utils;

import org.springframework.lang.Nullable;

import java.util.Date;

public class DateUtils {
    public static boolean isBetween(@Nullable Date from, @Nullable Date to, Date check) {
        return isBetween(from == null ? Long.MIN_VALUE : from.getTime(),
                to == null ? Long.MAX_VALUE : to.getTime(), check.getTime());
    }

    private static boolean isBetween(long from, long to, long check) {
        return from <= check && check <= to;
    }
}
