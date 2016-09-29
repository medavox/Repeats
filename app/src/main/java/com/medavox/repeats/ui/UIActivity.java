package com.medavox.repeats.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.medavox.repeats.R;
import com.medavox.repeats.application.Application;
import com.medavox.repeats.backend.Backend;
import com.medavox.repeats.backend.BackendHelper;
import com.medavox.repeats.background.BackgroundService;
import com.medavox.repeats.controllers.MedebottleController;
import com.medavox.repeats.datamodels.IntendedDose;
import com.medavox.repeats.events.UIMessageEvent;
import com.medavox.repeats.network.NetworkController;
import com.medavox.repeats.ui.fragments.EbottleFragment;
import com.medavox.repeats.ui.fragments.PlanFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import icepick.Icepick;

/**@author Adam Howard
@date 17/08/16*/
/**Common functionality for any Activity providing a UI for the app.
 * Handles app logic for user input, managing bluetooth support and permissions,
 * and other common housekeeping tasks.
 * UI implementations should override this class in order to manage existing fragments and app components,
 * such as bluetooth, eBottle and network communication.*/
public abstract class UIActivity extends AppCompatActivity {
//todo: handle app lifecycle methods (such as pausing) in an intelligent way, specific to us
    private static final String TAG = "UIActivity";
    protected static final int  REQUEST_ENABLE_BT = 2;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;  //Needed for android 6 new permissions
    private static AppStatus situation = AppStatus.WAITING_TO_CHECK;

    protected String deviceId;
    protected AlertDialog deviceIdAlert;
    protected Timer myTimer;

    /*package-private*/ SharedPreferences sp;

    public int doseSwallowed;//todo: replace this


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionSetUp();
        setBluetooth(true);
        askForDeviceName();  //check that device id has been set

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called");
        //set up a recurring timer to keep PlanFragment's info up-to-date while the app is open
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventBus.getDefault().post(new UIMessageEvent(this, UIMessageEvent.BroadcastMessages.UPDATE));
            }
        }, 0, 60000);

        deviceId = sp.getString(getString(R.string.device_id), getString(R.string.default_device_id_value));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop the UI update timer
        myTimer.cancel();
        myTimer = null;


    }

    @Override
    public void onStart() {
        super.onStart();
        //register this activity to listen on the EventBus
        EventBus.getDefault().register(this);
        changeAppStateTo(AppStatus.WAITING_TO_CHECK);
        sp = getSharedPreferences(getString(R.string.shared_prefs_tag), 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if(deviceIdAlert!=null) {
            deviceIdAlert.dismiss();
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    /**The finite set of states that the app can be in.*/
    public enum AppStatus {
//before medication
        //name              is UI busy (uninterruptable with new UI messages)
        WAITING_TO_CHECK    (false),
        ERROR               (false),
        NO_PLAN             (false),
        NO_NEXT_DOSE        (false),
        DOSE_DUE_FUTURE     (false),
        DOSE_DUE_NOW        (false),
//during medication
        DISPENSE_PRESSED    (true), //\\\
        CONNECTED           (true), //----TODO:roll these 3 into one?
        DISPENSING          (true), /////
        FULL_DOSE_DISPENSED (false),
        DOSE_SWALLOWED      (true);

        private boolean isBusy;

        AppStatus(boolean isBusy) {
            this.isBusy = isBusy;
        }

        public boolean isBusy() {
            return isBusy;
        }
    }
    public static AppStatus getAppState() {
        return situation;
    }

    /**Move the app into another AppStatus.
     * Called when a significant event has occurred -- eg pills have been dispensed --
     * and valid responses to user actions have changed.
     * These are events external to the core app code,
     * such as user actions, communication events and other callbacks.
     * Pass WAITING_TO_CHECK when the appropriate state is not known, and a check is needed (such as app startup).*/
    public static void changeAppStateTo(AppStatus as) {
        Context context = Application.getContext();
        Log.d(TAG, "app changing state from "+situation+" to "+as);
        //IntendedDose dose = dpe.getIntendedDoses();
        Backend helper = BackendHelper.getInstance(context);
        boolean sendUpdateBroadcast = false;
        switch(as) {//callbacks for specific states
            case DOSE_SWALLOWED:
                EventBus.getDefault().post(new UIMessageEvent(UIActivity.class,
                        EbottleFragment.UI_MESSAGE_RECIPIENT_ID,
                        EbottleFragment.EbottleFragmentTextViews.INSTRUCTIONS,
                        "Thank you. You have taken all necessary tablets at this time."
                ));//no break, because we want to now carry out the actions from WAITING_TO_CHECK

            case WAITING_TO_CHECK:
                //find out more info (using a plan) and get us out of this limbo state

                if(!helper.hasPlan()) {
                    changeAppStateTo(AppStatus.NO_PLAN);
                    return;
                }
                else if(!helper.hasNextDueDose()) {
                    changeAppStateTo(AppStatus.NO_NEXT_DOSE);
                    return;
                }
                else if(helper.hasPlan() && helper.hasNextDueDose()) {//there is a plan, and it has a next dose
                    //work out if the dose is due now, or in the future
                    final IntendedDose iDose = helper.getNextDueDose();
                    if(iDose.getTimeStart() < System.currentTimeMillis()) {
                        changeAppStateTo(AppStatus.DOSE_DUE_NOW);

                        BackgroundService.setReminders(iDose);
                        return;
                    }
                    else {
                        changeAppStateTo(AppStatus.DOSE_DUE_FUTURE);
                        return;
                    }
                }
                //break;//unreachable

            case NO_PLAN:
            case NO_NEXT_DOSE:
            case DOSE_DUE_FUTURE:
            case DOSE_DUE_NOW:
                sendUpdateBroadcast = true;
                break;
            case DISPENSE_PRESSED:
                NetworkController.getInstance().postMonitor(Monitor.createMonitor(Application.getContext(),//context
                        helper.getNextDueDose().getDoseID(),                //dose ID
                        PlatformCodes.DISPENSE_PRESSED));            //action
                break;
            case CONNECTED:
                NetworkController.getInstance().postMonitor(Monitor.createMonitor(Application.getContext(),//context
                        helper.getNextDueDose().getDoseID(),                //dose ID
                        PlatformCodes.CONNECTED_TO_MED_EBOTTLE));            //action
                break;
            case DISPENSING:
                break;
            case FULL_DOSE_DISPENSED:
                break;
        }
        situation = as;
        if(sendUpdateBroadcast) {
            //tell listening fragments to update their UIs
            EventBus.getDefault().post(new UIMessageEvent(UIActivity.class, UIMessageEvent.BroadcastMessages.UPDATE));
        }
    }


    public void askForDeviceName() {
        askForDeviceName(false);
    }

    /**Asks the user to select the device ID from a pre-defined list.
     * Won't ask if the value is set is sharedPreferences and parameter checkAnyway is false.
     * @param checkAnyway whether to ask the user, even if the sharedPreferences value is set.*/
    public String askForDeviceName(boolean checkAnyway) {
        final SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs_tag), 0);
        final String[] IDs = getResources().getStringArray(R.array.device_ids);
        if(!prefs.contains(getString(R.string.device_id)) || checkAnyway) {
            //no device id, or we have to check again anyway
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please choose your Device ID : ")
                .setItems(R.array.device_ids, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //DatabaseHelper.getInstance(getApplicationContext()).insertInfo("deviceId", "prototype");
                        //set deviceID preference upon user choice

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(getString(R.string.device_id), IDs[which]);
                        editor.apply();
                        deviceId = IDs[which];
                        EventBus.getDefault().post(new UIMessageEvent(this, PlanFragment.UI_MESSAGE_RECIPIENT_ID,
                                PlanFragment.PlanFragmentTextViews.DEVICE_ID_TEXT, deviceId));
                    }
                });
            deviceIdAlert = builder.create();
            deviceIdAlert.show();

        }else{//there is a deviceID set in sharedPreferences, so get that value
            deviceId =  prefs.getString(getString(R.string.device_id), IDs[0]);//default to the first deviceID,
            // if there is a problem reading from SharedPreferences
        }
        return deviceId;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @TargetApi(23)
    private void permissionSetUp(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth permissions");
                builder.setMessage("Allow this app to use bluetooth?");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover ble when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
            }
        }
    }

    /**
     * Switches on device bluetooth programmatically if not already on
     * @param enable
     * Boolean flag to either switch bluetooth on (true) or off (false)
     * @return
     * Returns true
     */
    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            // return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    @Subscribe (threadMode = ThreadMode.BACKGROUND)
    public void onMedebottleEvent(MedebottleEvent mbe) {
        //Log.i("medebottle event", mbe.toString());

        int doseID =-1;
        Backend helper = BackendHelper.getInstance(this);
        IntendedDose iDose = helper.getNextDueDose();
        if(iDose != null) {
            doseID = iDose.getDoseID();
        }

        switch(mbe.getEventType()) {
            case CONNECTED:
                changeAppStateTo(AppStatus.CONNECTED);
                break;
            case SINGLE_TABLET_DISPENSED:
                if(iDose != null) {
                    NetworkController.getInstance().postMonitor(Monitor.createMonitor(this, doseID, PlatformCodes.TABLET_DISPENSED));
                }
                break;
            case DISPENSE_COMPLETE:
                changeAppStateTo(AppStatus.FULL_DOSE_DISPENSED);
                break;
            case NOT_FOUND:
                    NetworkController.getInstance().postMonitor(Monitor.createMonitor(this, doseID, PlatformCodes.MED_EBOTTLE_NOT_FOUND));
                break;
            case ERROR:
                Log.i(TAG, "error while in app state:"+situation);
                EventBus.getDefault().post(new UIMessageEvent(this,
                EbottleFragment.UI_MESSAGE_RECIPIENT_ID,
                EbottleFragment.EbottleFragmentTextViews.INSTRUCTIONS,
                "An error has occured:\n"+mbe.getMessage()
                        +"\nplease contact your eBottle provider"));
                changeAppStateTo(AppStatus.ERROR);
                break;
        }
    }

    /**Event listener which handles dispense request results.
     * @param dcr an Event describing whether or not the dispense request succeeded.
     * If true, the medebottle dispenses a dose, and doseDispensed() is called.*/
    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onDispenseConfirmationResult(DispenseConfirmationResult dcr) {
        if(dcr.succeeded()) {
            if(deviceId!=null) {
                IntendedDose nextDose = BackendHelper.getInstance(this).getNextDueDose();
                changeAppStateTo(AppStatus.DISPENSING);
                MedebottleController.getInstance().startDispense(deviceId, nextDose.getQuantity());
            }else{
                Toast.makeText(this, "DeviceId is not set", Toast.LENGTH_SHORT).show();
            }

        }//incorrect request logic (eg, wrong PIN toasts) is now handled in specific T:DispenseStrategys
    }

    @Subscribe (threadMode = ThreadMode.BACKGROUND)
    public void onDoseEvent(DoseEvent de) {
        //Log.i("new plan event", npe.toString());

        if(de.getEventType() == DoseEvent.DoseEventType.DOWNLOADED) {
            IntendedDose[] doses = de.getDoses();

            //add downloaded IntendedDoses to IntendedDoses table
            BackendHelper.getInstance(this).addIntendedDoses(doses);
        }
        //whatever the event type, recheck the app state
        //(this also updates the UI if necessary)
        changeAppStateTo(AppStatus.WAITING_TO_CHECK);
    }

    /**
     * Checks that this device supports bluetooth low energy.
     */
    protected void ensureBLESupported() {
        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
            new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle) //  DOESN'T WORK ON SDK 19
                //.setView(R.layout.dialog_pin)
                .setTitle("Alert!")
                .setMessage("Your device does not support Bluetooth Low Energy,"+
                        " which is required for communicating with your Med eBottle.\n"+
                        "Please install this app on another device in order to use your Med eBottle.")
                .setIcon(R.mipmap.medicine_tab)
                .show();
        }
    }

    /**
     * Checks that bluetooth is enabled on the device.
     * @return
     */
    protected boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    /**
     * Displays a dialog box to enable bluetooth manually.
     */
    protected void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }
}
