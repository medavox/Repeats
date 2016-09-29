package com.medavox.repeats.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.medavox.repeats.R;
import com.medavox.repeats.application.Application;
import com.medavox.repeats.backend.Backend;
import com.medavox.repeats.backend.BackendHelper;
import com.medavox.repeats.background.BackgroundService;
import com.medavox.repeats.datamodels.IntendedDose;
import com.medavox.repeats.events.UIMessageEvent;
import com.medavox.repeats.adapters.IntendedDoseAdapter;
import com.medavox.repeats.network.NetworkController;
import com.medavox.repeats.ui.UIActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PlanFragment extends UpdatableFragment {

    private ListView listView;
    private boolean viewCreated = false;
    private Resources res;
    IntendedDoseAdapter adapter;
    public PlanFragment CustomListView = null;
    @BindView(R.id.downloadPlanBtn) Button  downloadButton;
    @BindView(R.id.deletePlanBtn) Button  deleteButton;
    @BindView(R.id.deviceIdValue) TextView deviceIdValue;
    @BindView(R.id.userIdValue) TextView userIdValue;
    @BindView(R.id.planIdValue) TextView planIdValue;
    @BindView(R.id.editDeviceIdBtn) Button editDeviceButton;


    public final static String UI_MESSAGE_RECIPIENT_ID = "com.elucid.medi.ui.fragments.EbottleFragment";

    public enum PlanFragmentTextViews implements FragmentTextViews {
        DEVICE_ID_TEXT
    }

    public PlanFragment() {
       // Required empty public constructor
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
       // Inflate the layout for this fragment
        View mView =  inflater.inflate(R.layout.fragment_plan, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        deviceIdValue.setText(owner.getDeviceId());
        listView = (ListView) getView().findViewById(R.id.intended_dose_list);
        viewCreated = true;
        CustomListView = this;
        res = getResources();
        updateUI();
    }

    @Override
    public void updateUI() {
        Backend helper = BackendHelper.getInstance(owner);

        final List<IntendedDose> doseList = helper.getAllIntendedDoses();

        owner.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.shared_prefs_tag), 0);
                deviceIdValue.setText(sp.getString(getString(R.string.device_id), owner.getString(R.string.default_device_id_value)));
                userIdValue.setText(sp.getString(getString(R.string.user_id), owner.getString(R.string.default_user_id_value)));
                //planIdValue.setText(sp.getString(getString(R.string.plan_id), res.getInteger(R.integer.default_plan_id_value)));
                if(doseList!=null) {
                    adapter = new IntendedDoseAdapter(owner, doseList);
                    listView.setAdapter(adapter);
                }
            }
        });

    }

    public String formatDate(String d) {
        String month = d.substring(5,7);
        String day = d.substring(8,10);
        StringBuilder sb = new StringBuilder();
        sb.append(day);
        sb.append("/");
        sb.append(month);
        return sb.toString();
    }

    @OnClick(R.id.editDeviceIdBtn)
    public void onChangeDeviceIDPressed() {
        if(UIActivity.getAppState() != UIActivity.AppStatus.CONNECTED
        && UIActivity.getAppState() != UIActivity.AppStatus.DISPENSING
        && UIActivity.getAppState() != UIActivity.AppStatus.FULL_DOSE_DISPENSED
        && UIActivity.getAppState() != UIActivity.AppStatus.DISPENSE_PRESSED) {
           owner.askForDeviceName(true);
        }
        else {
            Toast.makeText(getActivity(), R.string.cant_change_device_id_while_connected_toast, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onUIMessageEvent(UIMessageEvent uime) {
        super.onUIMessageEvent(uime);
        final String msgText = uime.getMessageText();
        if(uime.getDetails() == PlanFragmentTextViews.DEVICE_ID_TEXT
                && uime.getRecipientID().equals(UI_MESSAGE_RECIPIENT_ID)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceIdValue.setText(msgText);
                }
            });
        }
    }

    @OnClick(R.id.downloadPlanBtn)
    public void downloadPressed() {
        if (BackendHelper.getInstance(getActivity()).hasPlan()) {
            Toast.makeText(getActivity(), R.string.plan_already_exists_toast, Toast.LENGTH_SHORT).show();
        } else {
            switch(Application.getBuildMode()) {
                case DEMO:
                case DEBUG:
                    //generate a fake plan for demos & debugging
                    IntendedDose fakeDose = IntendedDose.createDemoDoseDueIn(0);
                    Toast.makeText(owner, R.string.demo_plan_generated_toast, Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new DoseEvent(this, DoseEvent.DoseEventType.DOWNLOADED, fakeDose));
                    break;

                default: //otherwise, download a plan from the network
                    SharedPreferences sp = owner.getSharedPreferences(owner.getString(R.string.shared_prefs_tag), Context.MODE_PRIVATE);
                    int trialID = sp.getInt(owner.getString(R.string.trial_id), owner.getResources().getInteger(R.integer.default_trial_id_value));
                    String jwt = sp.getString(owner.getString(R.string.jwt_token), null);
                    NetworkController.getInstance().getDoseData(""+trialID);
            }
        }
    }

    @OnClick(R.id.deletePlanBtn)
    public void deletePressed(){
        if(UIActivity.getAppState() != UIActivity.AppStatus.NO_PLAN) {
            AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(getActivity());
            deleteDialogBuilder.setMessage("Delete the current plan?");
            deleteDialogBuilder.setCancelable(true);

            deleteDialogBuilder.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            deletePlan();
                        }
                    });

            deleteDialogBuilder.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog deleteDialog = deleteDialogBuilder.create();
            deleteDialog.show();
        }
        else {
            Toast.makeText(owner, R.string.no_plan_to_delete_toast, Toast.LENGTH_SHORT).show();
        }
    }

    public void deletePlan(){
        /*DatabaseHelper.getInstance(getActivity()).deletePlan();
        updateUI();*/
        //Toast.makeText(getActivity(), "This functionality is undergoing review.\nNo plans were deleted.", Toast.LENGTH_LONG).show();
        Toast.makeText(getActivity(), "Dose plan deleted.", Toast.LENGTH_SHORT).show();
        BackendHelper.getInstance(owner).deletePlan();
        BackgroundService.cancelAllReminders();
        UIActivity.changeAppStateTo(UIActivity.AppStatus.WAITING_TO_CHECK);
    }
}
