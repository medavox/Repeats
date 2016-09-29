package com.medavox.repeats.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author jamesburnstone
@date 04/05/2016
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

//----------------------------------- old methods -------------------------------------
    public static String getTimeStamp(){
        cal.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ");
        return sdf.format(cal.getTime());
    }

    public static String getModifiedTime(int mod){
        Calendar c = cal;
        c.add(Calendar.HOUR, mod);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        String formattedTime = (df.format(c.getTime())).substring(11,17);
        return formattedTime;
    }


    public static String getCurrentValue(String identifier) {

        cal.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        String formattedDate = df.format(cal.getTime());
        if(identifier.equalsIgnoreCase(DATETIME)){

            return formattedDate;

        }else if(identifier.equalsIgnoreCase(TIME)){

            return formattedDate.substring(11,19);

        }else if(identifier.equalsIgnoreCase(DATE)){

            return formattedDate.substring(0,10);

        }else{
            return null;
        }
    }

    public static int[] getTimeDifference(String dueDate, String dueTime){
        String dateStop = dueDate + " " + dueTime;
        String dateStart = getCurrentValue("datetime");

        //HH converts hour in 24 hours format (0-23), day calculation
        SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);

            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

            int diffSeconds =(int) (diff / 1000 % 60);
            int diffMinutes =(int)( diff / (60 * 1000) % 60);
            int diffHours = (int)(diff / (60 * 60 * 1000) % 24);
            int diffDays =(int) (diff / (24 * 60 * 60 * 1000));

            int[]timeDiffs = {diffDays, diffHours, diffMinutes, diffSeconds};

            return timeDiffs;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
