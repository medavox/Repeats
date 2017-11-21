package com.medavox.repeats.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.medavox.repeats.R;
import com.medavox.repeats.database.Backend;
import com.medavox.repeats.database.BackendHelper;
import com.medavox.repeats.datamodels.CompletedDose;
import com.medavox.repeats.datamodels.Task;
import com.medavox.repeats.list_adapters.ActiveTasksAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Adam Howard
@date 05/09/16
 */
public class ActiveTasksFragment extends UpdatableFragment {

    @BindView(R.id.active_task_listview)     ListView listView;

    @Override
    public void updateUI() {
        Backend helper = BackendHelper.getInstance(owner);
        final List<Task> doseList = helper.getAllCompletedDoses();

        owner.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(doseList!=null) {
                    BaseAdapter adapter = new ActiveTasksAdapter(owner, doseList);
                    listView.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView =  inflater.inflate(R.layout.fragment_completed_doses, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

}
