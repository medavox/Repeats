package com.medavox.repeats.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Adam Howard
@date 04/07/2016
 */
public class DateTime {

    private static final String TIME = "time";
    private static final String DATE = "date";
    private static final String DATETIME = "datetime";
    private static Calendar cal = Calendar.getInstance();

    public static String getNiceDate(long timeInMillis) {
        return getNiceDate(timeInMillis, false);
    }

    public static String getNiceDate(long timeInMillis, boolean withDayOfWeek) {
        cal.setTimeInMillis(timeInMillis);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.UK);

        return (withDayOfWeek ? cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.UK)+" " : "")+day+" "+month;
    }

    public static String getNiceTime(long timeInMillis) {
        cal.setTimeInMillis(timeInMillis);
        String hour = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY));
        String minute = String.format("%02d", cal.get(Calendar.MINUTE));
        return hour+":"+minute;
    }

    public static String getNiceFormat(long timeInMillis, boolean withDayOfWeek) {
        return getNiceTime(timeInMillis)+" on "+getNiceDate(timeInMillis, withDayOfWeek);
    }

    public static String getNiceFormat(long timeInMillis) {
        return getNiceTime(timeInMillis)+" on "+getNiceDate(timeInMillis);
    }
/*
    public static String getNiceFormat(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        String hour = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY));
        String minute = String.format("%02d", cal.get(Calendar.MINUTE));
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.UK);
        return hour+":"+minute +" on "+day+" "+month;
    }*/



    /**Print the duration of something in human-readable format,
     * displaying only the 2 highest non-zero time units.*/
    public static String getDuration(long duration) {
        long dur = Math.abs(duration); //even if it's in the past, make it positive
        int[] amounts = {0, 0, 0};
        String[] unitNames = {"day", "hour", "minute"};
        amounts[0] = (int) (dur / (24 * 60 * 60 * 1000));//days
        amounts[1] = (int) ((dur / (1000*60*60)) % 24);//hours
        amounts[2] = (int) ((dur / (1000*60)) % 60);//minutes
        //amounts[3] = (int) (dur  / 1000) % 60 ;//seconds

        int rawSeconds = (int)(dur / 1000);

        //if it's less than 2 minutes, just return this as seconds
        if(rawSeconds <= 120) {
            return unitString(rawSeconds, "second");
        }

        //only display minutes or larger
        int unitsCounted = 0;
        String ret = "";
        for(int i = 0; i < amounts.length && unitsCounted < 2; i++) {
            if(amounts[i] > 0) {
                //if(i == amounts.length-1 && amounts[i-1] >= 5)//if we're dealing with >5 minutes
                ret += unitString(amounts[i], unitNames[i])+" ";
                unitsCounted++;
            }
        }
        return ret;
    }

    private static String unitString(int amount, String unit) {
        String ret = (amount> 0 ? amount+" "+unit : "");
        ret += (amount > 1 ? "s" : "");
        return ret;
    }
}
