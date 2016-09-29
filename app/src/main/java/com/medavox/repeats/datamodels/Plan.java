package com.medavox.repeats.datamodels;

import com.google.gson.annotations.SerializedName;

/**
 * @author adam
@date 23/09/16
 */

public class Plan {
    @SerializedName("plan_id")
    int planID;
    @SerializedName("device_id")
    String deviceID;

    public Plan(int plan_id, String device_id) {
        planID = plan_id;
        deviceID = device_id;
    }

    public String toString() {
        return "Plan [plan ID: "+planID+"; "+
                "device ID: "+deviceID+"]";
    }

    public int getPlanID() {
        return planID;
    }

    public String getDeviceID() {
        return deviceID;
    }
}
