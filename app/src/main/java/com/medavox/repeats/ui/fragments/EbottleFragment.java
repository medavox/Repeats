package com.medavox.repeats.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.medavox.repeats.R;
import com.medavox.repeats.application.Application;
import com.medavox.repeats.database.Backend;
import com.medavox.repeats.database.BackendHelper;
import com.medavox.repeats.background.BackgroundService;
import com.medavox.repeats.datamodels.CompletedDose;
import com.medavox.repeats.datamodels.IntendedDose;
import com.medavox.repeats.events.UIMessageEvent;
import com.medavox.repeats.ui.UIActivity;
import com.medavox.repeats.utility.DateTime;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * @author jamesburnstone
@date 18/12/2015
 */
/**The main dashboard for taking doses. Provides a button to take medication, along with information
 * about the time the last dose was completed (taken or missed), and the time the next dose is due*/
public class EbottleFragment extends UpdatableFragment {

    @BindView(R.id.pill_instruct)   TextView    instructionsTextView;

    @BindView(R.id.nextDoseDate)    TextView    nextDoseDateTV;//the date the next dose is due
    @BindView(R.id.nextDoseQuan)    TextView    nextDoseQuantityTV;//the quantity due
    @BindView(R.id.nextDoseDue)     TextView    nextDoseDueInTV;//how long until that dose is due

    @BindView(R.id.prevDoseTaken)   TextView    lastDoseTakenOrMissedTV;
    @BindView(R.id.prevDoseQuan)    TextView    lastDoseQuantityTV;//quantity due
    @BindView(R.id.prevDoseDate)    TextView    lastDoseTimeAgoTV;

    //@BindView(R.id.buttonDispense)  Button      buttonDispense;
    @BindView(R.id.buttonTaken)     Button      buttonTaken;


    private IntendedDose displayedNextDose;
    private CompletedDose displayedPrevDose;


    public final static String UI_MESSAGE_RECIPIENT_ID = "eLucid eBottle Fragment";
    public final static String TAG = "elucid.EbottleFragment";

    public enum EbottleFragmentTextViews implements FragmentTextViews {
        INSTRUCTIONS,
        PREVIOUS_DOSE,
        NEXT_DOSE,
        PREVIOUS_DOSE_QUANTITY,
        NEXT_DOSE_QUANTITY,
        NEXT_DOSE_DUE,
        PREVIOUS_DOSE_TAKEN;
    }

    public EbottleFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_ebottle, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }


    /**Listens for UI Message Events, which tell the fragment to update a specified View with the specified text.*/
    @Override
    @Subscribe (threadMode = ThreadMode.BACKGROUND)
    public void onUIMessageEvent (UIMessageEvent uime) {
        super.onUIMessageEvent(uime);
        if(uime.getRecipientID().equals(EbottleFragment.UI_MESSAGE_RECIPIENT_ID)) {
            TextView tv = getTextViewFromEnum((EbottleFragmentTextViews)uime.getDetails());
            setInstructionText(uime.getMessageText(), tv);
        }
    }

    private TextView getTextViewFromEnum(EbottleFragmentTextViews ebftv) {
        switch(ebftv) {

            case INSTRUCTIONS:
                return instructionsTextView;
            case PREVIOUS_DOSE:
                return lastDoseTimeAgoTV;
            case NEXT_DOSE:
                return nextDoseDateTV;
            case PREVIOUS_DOSE_QUANTITY:
                return lastDoseQuantityTV;
            case NEXT_DOSE_QUANTITY:
                return nextDoseQuantityTV;
            case NEXT_DOSE_DUE:
                return nextDoseDueInTV;
            case PREVIOUS_DOSE_TAKEN:
                return lastDoseTakenOrMissedTV;
        }
        //"should" not reach here. But it's possible
        return null;
    }

    @Override
    public void updateUI() {
        final Backend helper = BackendHelper.getInstance(owner);
        final IntendedDose doseToTake = helper.getNextDueDose();
        final CompletedDose lastDose = helper.getPreviousDoseCompleted();

        String instructionsText = null;

        String nextDoseDate = null;
        String nextDoseQuant = null;
        String nextDoseDue = null;

        String lastDoseTakenOrMissed = null;
        String lastDoseQuant = null;
        String lastDoseTimeAgo = null;

        //update the next due dose info-displays, based on what (if any) data we just got from the local DB
        if(displayedNextDose == null || !displayedNextDose.equals(doseToTake)) {//if there is no displayed next dose, or it's different to the just-got copy
            displayedNextDose = doseToTake;//set displayedNextDose to the new value, even if it's null
            Log.i(TAG, "new nextDose:"+doseToTake);
            if (doseToTake != null) {

                nextDoseDate = "at " + DateTime.getNiceFormat(doseToTake.getTimeStart());
                nextDoseQuant = doseToTake.getQuantity() + " tablet(s)";
                long timeUntil = displayedNextDose.getTimeStart() - System.currentTimeMillis();
                String timeDue = (timeUntil <= 0 ? "now" : "in " + DateTime.getDuration(timeUntil));
                nextDoseDue = "Due " + timeDue;
            } else {
                nextDoseDate = owner.getString(R.string.no_next_dose);
                nextDoseQuant = "";
                nextDoseDue = "";
            }

        }
        //update the last taken dose info-displays, based on what (if any) data we just got from the local DB
        if(displayedPrevDose == null || !displayedPrevDose.equals(lastDose)) {
            displayedPrevDose = lastDose;
            //Log.i(TAG, "new lastDose:"+lastDose);

            if(lastDose != null) {
                //long timeUntil = lastDose.getEffectiveDate() - System.currentTimeMillis();
                //lastDoseTimeAgoTV.setText(DateTime.getDuration(timeUntil)+" ago");
                lastDoseTakenOrMissed = lastDose.getStatus()+"\n at "+DateTime.getNiceFormat(lastDose.getEffectiveDate());
                lastDoseQuant = lastDose.getQuantity()+" tablet(s)";
                lastDoseTimeAgo = "";
            }
            else {
                lastDoseTakenOrMissed = owner.getString(R.string.no_prev_dose);
                lastDoseQuant = "";
                lastDoseTimeAgo = "";
            }
        }

        //set the patient instructions display, based on the current App Status
        switch(UIActivity.getAppState()) {
            case NO_PLAN:
                instructionsText = owner.getString(R.string.no_plan_advice_text);
                break;

            case NO_NEXT_DOSE:
                if(helper.getIntendedDoseCount() > 0
                && helper.getIntendedDoseCount() == helper.getCompletedDoseCount()) {
                    instructionsText = owner.getString(R.string.all_doses_completed_advice_text);
                }
                else {
                    instructionsText = owner.getString(R.string.no_next_dose_advice_text);
                }

                break;

            case DOSE_DUE_FUTURE:
                instructionsText = owner.getString(R.string.no_doses_currently_due);
                break;

            case DOSE_DUE_NOW://if there's a dose due now, Next Dose displays that one, not the dose after it
                instructionsText = owner.getString(R.string.dose_due_instruction);
                break;
        }


        final String[] texts = {instructionsText, nextDoseDate, nextDoseQuant, nextDoseDue,
        lastDoseTakenOrMissed, lastDoseQuant, lastDoseTimeAgo};
        final TextView[] views = {instructionsTextView, nextDoseDateTV, nextDoseQuantityTV, nextDoseDueInTV,
                lastDoseTakenOrMissedTV, lastDoseQuantityTV, lastDoseTimeAgoTV};
        //batch all TextView updates together into a single runnable
        owner.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < views.length; i++) {
                    if(texts[i] != null){
                        views[i].setText(texts[i]);
                    }
                }
            }
        });

    }

    /**Helper method to change the message text for the specified TextView.
     *  @param text the new text display in the TextView
     *  @param views the TextView(s) to change*/
    public void setInstructionText(String text, TextView... views) {
        final String textFinal = text; //hack to allow use of text in inner class
        final TextView[] finalViews = views;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {//do all setText()s in a single thread to save on thread allocation
                for(TextView tv : finalViews) {
                    tv.setText(textFinal);
                }
            }
        });
    }

    /**
     * Gets a DispenseRequestStrategy from DispenseStategyFactory,
     * then defers user dispense request.
     */
    //@OnClick(R.id.buttonDispense)
    public void dispensePressed() {
        owner.doseSwallowed = 0; //reset back to 0
        //Log.i(TAG, "owner's state:"+owner.getAppState());
        switch(owner.getAppState()) {
            case ERROR:
                UIActivity.changeAppStateTo(UIActivity.AppStatus.WAITING_TO_CHECK);
                //if the user interacts with the app when it's in an error state
                //rouse the app from its errorful torpor and respond to the user
            case DOSE_DUE_NOW:
                UIActivity.changeAppStateTo(UIActivity.AppStatus.DISPENSE_PRESSED);
                //DispenseRequestStrategy drs = DispenseRequestFactory.getDispenseStrategy(DispenseRequestStrategy.DispenseType.NONE);
                /*if( drs != null) {
                    drs.requestDispense(getActivity());
                }*/

                break;

            case DOSE_DUE_FUTURE://dose is not due now, but there is another dose at some point
                //check when dose in future is due - how long in the future.

                //get plan object for next dose due
                //IntendedDose dose = BackendHelper.getInstance(getActivity()).getNextDueDose();

                /*
                //calculate hours and mins until this dose
                long dueTime = dose.getTimeStart();

                long timeNow = System.currentTimeMillis();
                long timeTillNextDose = dueTime - timeNow;

                //if dose is due today the user can be given the choice to take it now
                boolean sameDay = timeTillNextDose < (24* 60 * 60 * 1000);//milliseconds in a day

                if (sameDay) {
                    DispenseRequestFactory.getDispenseStrategy(DispenseRequestStrategy.DispenseType.EARLY).requestDispense(getActivity());
                }
*/
                Toast.makeText(getActivity(), R.string.dose_not_yet_due_toast, Toast.LENGTH_SHORT).show();
                break;

            case NO_PLAN:
            case NO_NEXT_DOSE:
                Toast.makeText(getActivity(), R.string.no_future_doses_toast, Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(getActivity(), R.string.cant_press_dispense_now_toast, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /** The user presses this to confirm that they have swallowed the pill(s).*/
    @OnClick(R.id.buttonTaken)
    public void takenPressed() {
        if(UIActivity.getAppState() == UIActivity.AppStatus.FULL_DOSE_DISPENSED) {
            //disconnect medebottle
            //MedebottleController.getInstance().disconnect();
            if(Application.getBuildMode() != Application.BuildMode.DEBUG) {
                Backend db = BackendHelper.getInstance(getActivity());
                IntendedDose justDoneDose = db.getNextDueDose();

                //cancel all alarms & timers relating to this dose (based on doseID)
                BackgroundService.cancelRemindersWithDoseID(justDoneDose.getDoseID());

                //update local SQLite table dosesCompleted (and by extension, the platform)
                CompletedDose doneDose = new CompletedDose(justDoneDose, CompletedDose.DOSE_TAKEN);
                db.addCompletedDose(doneDose);


                //SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.sharedPrefsTag), 0);
                //String patientID = sp.getString(getString(R.string.PatientID), getString(R.string.DefaultPatientIDValue));

                //send completed dose to the platform
                //NetworkController.getInstance().postDoseData(doneDose);
                /*
                NetworkController.sendMedicationAdministration(
                        MedicationAdministrationFactory.getJSON(resourceIdentifier,
                                String.valueOf(owner.doseSwallowed),
                                "Device:elucidmhealth/" + owner.getDeviceId(),
                                DateTime.getTimeStamp(),
                                "Patient:" + patientID));*/
            }
            /*else {
                String resourceIdentifier = UUID.randomUUID().toString();
                String patientID = owner.getSharedPreferences(getString(R.string.sharedPrefsTag),0)
                        .getString(getString(R.string.PatientID), getString(R.string.DefaultPatientIDValue));
                NetworkController.sendMedicationAdministration(
                        MedicationAdministrationFactory.getJSON(resourceIdentifier,
                                String.valueOf(owner.doseSwallowed), "Device:elucidmhealth/"+owner.getDeviceId(),
                                DateTime.getTimeStamp(), "Patient:"+patientID));//convert long datetimes in FHIR format:
                //2016-08-09T14:12:55+0100
            }*/
            //EventBus.getDefault().post(new UserEvent(this, UserEvent.UserEventType.DOSE_SWALLOWED));
            //reset app state back to waiting for dose info and such
            UIActivity.changeAppStateTo(UIActivity.AppStatus.DOSE_SWALLOWED);
        }
        else if(UIActivity.getAppState() == UIActivity.AppStatus.ERROR) {
            UIActivity.changeAppStateTo(UIActivity.AppStatus.WAITING_TO_CHECK);
            //refresh app state on user interaction after error,
            takenPressed();//then re-run
        }
        else if(Application.getBuildMode() == Application.BuildMode.DEBUG) {
            //for testing cancelling a dose by ID
            Backend db = BackendHelper.getInstance(getActivity());
            if(db.hasNextDueDose()) {
                IntendedDose justDoneDose = db.getNextDueDose();
                //cancel all alarms & timers relating to this dose (based on doseID)
                BackgroundService.cancelRemindersWithDoseID(justDoneDose.getDoseID());
            }
        }
        else {
            //gently notify the user that now is not the time to press the Taken button
            Toast.makeText(owner, owner.getString(R.string.should_not_press_taken_toast), Toast.LENGTH_LONG).show();
        }
    }
}

