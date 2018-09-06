package com.drom.analyzer.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 03.09.2018
 */
public class DateUtils {

    public static Date addSeconds(Date date, int seconds) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    public static Date addMinutes(Date date, int minutes) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    public static Date addHours(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, amount);
        return calendar.getTime();
    }

    public static Date addDays(Date date, int interval) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, interval);
        return calendar.getTime();
    }
}
