package com.medavox.repeats.datamodels;

import com.medavox.repeats.utility.DateTime;
//import com.google.gson.annotations.SerializedName;

/**
 * @author 1
@date 2
 */
public class IntendedDose {

//    @SerializedName("dose_id")
    private int dose_id;
  //  @SerializedName("time_start")
    private long time_start;
    //@SerializedName("time_end")
    private long time_end;
    //@SerializedName("time_due")
    private long time_due;
    //@SerializedName("quantity")
    private int quantity;

    private static int demoDoseIDCounter = 1;
    private static final int demoQuantity = 2;
    private static long defaultDemoDoseDuration = 300000;//5 minutes
    //private static long defaultDemoDoseDuration = 60000;//1 minute
    //private static long defaultDemoDoseDuration = 20000;//20 seconds
    //private static long defaultDemoDoseDuration = 6000;//6 seconds

    public IntendedDose(int dose_id, long time_start, long time_end, long time_due, int quantity) {
        this.time_start = time_start;
        this.time_end = time_end;
        this.time_due = time_due;
        this.quantity = quantity;
        this.dose_id = dose_id;
    }

    public IntendedDose(int dose_id, long time_start, long time_end, int quantity) {
        this.time_start = time_start;
        this.time_end = time_end;
        this.time_due = -1;
        this.quantity = quantity;
        this.dose_id = dose_id;
    }

    public static IntendedDose createDemoDoseDueIn(long milliseconds) {
        return createDemoDoseDueIn(milliseconds, defaultDemoDoseDuration);
    }
    public static IntendedDose createDemoDoseDueIn(long milliseconds, long demoDoseDuration) {
        long startTime = System.currentTimeMillis()+milliseconds;
        long endTime = startTime+demoDoseDuration;

        return new IntendedDose(demoDoseIDCounter++,  startTime,  endTime,  demoQuantity);
    }

    public long getTimeStart() {
        return time_start;
    }

    public long getTimeEnd() {
        return time_end;
    }

    public long getTimeDue() {
        return time_due;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getDoseID() {
        return dose_id;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof IntendedDose && o != null) {
            IntendedDose iDose = (IntendedDose)o;
            return (dose_id == iDose.getDoseID()
            && time_start == iDose.getTimeStart()
            && time_end == iDose.getTimeEnd()
            && time_due == iDose.getTimeDue()
            && quantity == iDose.getQuantity()
            );
        }
        else {
            return false;
        }
    }

    public String toString() {
        return "IntendedDose [ ID: "+dose_id
                +"; time start: "+ DateTime.getNiceFormat(time_start)
                +"; time end: "+ DateTime.getNiceFormat(time_end)
                +"; time due: "+ DateTime.getNiceFormat(time_due)
                + "; quantity: "+quantity+"]";
    }
}
