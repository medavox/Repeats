package com.medavox.repeats.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.medavox.repeats.R;
import com.medavox.repeats.datamodels.IntendedDose;
import com.medavox.repeats.utility.DateTime;

import java.util.List;

import butterknife.BindView;

/**
 * @author jamesburnstone
@date 03/07/2016
 */
public class IntendedDoseAdapter extends BaseAdapter {
    @BindView(R.id.intended_row_date)        TextView    timeDueTV;
    @BindView(R.id.intended_row_time_start)   TextView    textViewTimeStart;
    @BindView(R.id.intended_row_time_end)     TextView    textViewTimeEnd;
    @BindView(R.id.intended_row_quantity)    TextView    textViewQuantity;

    /**Our list of Intended Doses*/
    private List<IntendedDose> data;

    /**Layout inflator to call external xml layout () */
    private static LayoutInflater inflater = null;
    private Context context;

    public IntendedDoseAdapter(Context c, List<IntendedDose> d) {
        this.context = c;
        //Take passed values
        data = d;
        inflater = (LayoutInflater)(c.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IntendedDose dose = data.get(position);/*
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dose.getTimeStart());
        Log.i("IntendedDoseAdapter", "time start:"+ cal.getTime());
        cal.setTimeInMillis(dose.getTimeEnd());
        Log.i("IntendedDoseAdapter", "time end:"+ cal.getTime());
        cal.setTimeInMillis(dose.getTimeDue());
        Log.i("IntendedDoseAdapter", "time due:"+ cal.getTime());*/
        View view = convertView;

        if(view == null) {
            view = inflater.inflate(R.layout.intended_dose_table_row, parent, false);
        }

        ((TextView)view.findViewById(R.id.intended_row_time_start)).setText(DateTime.getNiceFormat(dose.getTimeStart(), true));
        ((TextView)view.findViewById(R.id.intended_row_time_end)).setText(DateTime.getNiceFormat(dose.getTimeEnd(), true));
        //((TextView)view.findViewById(R.id.dateTextView)).setText(DateTime.getNiceDate(dose.getTimeStart()));
        ((TextView)view.findViewById(R.id.intended_row_quantity)).setText(""+dose.getQuantity());
        return view;
    }

    @Override
    public boolean areAllItemsEnabled () {
        return true;
    }
    @Override
    public boolean isEnabled (int position) {
        return true;
    }


}
