package com.ghostchu.quickshop.addon.plan.util;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    @NotNull
    public static Date daysAgo(int ago) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -ago);
        return cal.getTime();
    }
}
