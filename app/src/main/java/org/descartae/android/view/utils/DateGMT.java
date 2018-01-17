package org.descartae.android.view.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by lucasmontano on 11/02/17.
 */

public class DateGMT {

    public static String dateToGMT(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
        format.setCalendar(cal);
        Date gmt = format.parse(date);
        return gmt.toString();
    }

    public static Calendar getDate(int year, int month, int day) {
        final Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.HOUR, date.getMinimum(Calendar.HOUR));
        date.set(Calendar.HOUR_OF_DAY, date.getMinimum(Calendar.HOUR_OF_DAY));
        date.set(Calendar.MINUTE, date.getMinimum(Calendar.MINUTE));
        date.set(Calendar.SECOND, date.getMinimum(Calendar.SECOND));
        date.set(Calendar.MILLISECOND, date.getMinimum(Calendar.MILLISECOND));
        return (Calendar) date.clone();
    }

    public static String dateDisplay(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date.getTime());
    }

    public static String dateDisplay(Calendar date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date.getTime());
    }

    public static String dateDisplay(long date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(date));
    }

    public static int days(Calendar start, Calendar end) {

        if (end.getTimeInMillis() - start.getTimeInMillis() < 0) {
            return 0;
        }

        return Math.round(TimeUnit.MILLISECONDS.toDays(end.getTimeInMillis() - start.getTimeInMillis()));
    }

    public static Calendar stringToCalendar(String date) {

        Calendar cDate = (Calendar) Calendar.getInstance().clone();

        if (date == null) return cDate;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            cDate.setTime(simpleDateFormat.parse(date));
        } catch (ParseException e) {}

        return cDate;
    }
}