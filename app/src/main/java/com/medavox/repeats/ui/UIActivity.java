package com.medavox.repeats.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.medavox.repeats.R;
import com.medavox.repeats.application.Application;
import com.medavox.repeats.database.Backend;
import com.medavox.repeats.database.BackendHelper;
import com.medavox.repeats.background.BackgroundService;
import com.medavox.repeats.datamodels.IntendedDose;
import com.medavox.repeats.events.Event;
import com.medavox.repeats.events.UIMessageEvent;
import com.medavox.repeats.ui.fragments.EbottleFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;


/**@author Adam Howard
@date 17/08/16*/
/**Common functionality for any Activity providing a UI for the app.
 * Handles app logic for user input, managing bluetooth support and permissions,
 * and other common housekeeping tasks.
 * UI implementations should override this class in order to manage existing fragments and app components,
 * such as bluetooth, eBottle and network communication.*/
public abstract class UIActivity extends AppCompatActivity {
    private static final String TAG = "UIActivity";
    private static AppStatus situation = AppStatus.WAITING_TO_CHECK;

    protected AlertDialog deviceIdAlert;
    protected Timer myTimer;

    /*package-private*/ SharedPreferences sp;

    public int doseSwallowed;//todo: replace this


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                break;
            case CONNECTED:

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(Event e){
        //lol, do nowt
    }
}
