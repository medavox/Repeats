package com.medavox.repeats.datamodels;

import android.content.Context;
import android.content.SharedPreferences;

import com.medavox.repeats.R;
import com.medavox.repeats.application.Application;
import com.medavox.repeats.utility.DateTime;
//import com.google.gson.annotations.SerializedName;

/**
 * @author 1
@date 2
 */
/**An object representing a dose that has either been taken or missed.*/
public class CompletedDose {

    public final static String DOSE_TAKEN = "dose taken";
    public final static String DOSE_MISSED = "dose missed";
    public final static String DEVICE_ERROR = "device error";

    //@SerializedName("dose_id")
    private int dose_id;

    /**The time that this event occurred. Namely, the time the dose became missed (if missed),
     * or the time the dose was taken (if taken).*/
    //@SerializedName("effective_time")
    private long effectiveDate;

    //@SerializedName("quantity")
    private int quantity;

    //@SerializedName("status")
    private String status;//todo: replace this with enum, to allow for translations later

    //@SerializedName("error")
    private String error;


    //@SerializedName("user_id")
    private String userID;

    //@SerializedName("device_id")
    private String deviceID;

    public CompletedDose(int dose_id, long effectiveDate, int quantity, String status, String error) {
        init(dose_id, quantity, status);
        this.effectiveDate = effectiveDate;
        this.error = error;
    }

    public CompletedDose(int dose_id, int quantity, String status) {
        init(dose_id, quantity, status);
        this.effectiveDate = System.currentTimeMillis();
        this.error = null;
    }

    public CompletedDose(IntendedDose iDose, String status, String message) {
        init(iDose.getDoseID(), iDose.getQuantity(), status);
        this.effectiveDate = System.currentTimeMillis();
        this.error = message;
    }

    public CompletedDose(IntendedDose iDose, String status) {
        init(iDose.getDoseID(), iDose.getQuantity(), status);
        this.effectiveDate = System.currentTimeMillis();
    }

    private void init(int dose_id, int quantity, String status) {
        this.dose_id = dose_id;
        this.quantity = quantity;
        this.status = status;
        Context c = Application.getContext();
        SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_prefs_tag), 0);
        userID = sp.getString(c.getString(R.string.user_id), c.getString(R.string.default_user_id_value));
        deviceID = sp.getString(c.getString(R.string.device_id), c.getString(R.string.default_device_id_value));
    }

    public int getDoseID() {
        return dose_id;
    }

    public long getEffectiveDate() {
        return effectiveDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**Get the ID of the patient. Unique and unchanging for each phone/tablet.*/
    public String getStatus() {
        return status;
    }

    /**Get an error message, if any. May return null if there is no error.*/
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String toString() {
        return "CompletedDose [ ID: "+dose_id
                +"; time: "+DateTime.getNiceFormat(effectiveDate)
                +"; status: "+status
                + (error == null ? "" : "; error message: "+error)
                + "; quantity: "+quantity+"]";
    }
}
