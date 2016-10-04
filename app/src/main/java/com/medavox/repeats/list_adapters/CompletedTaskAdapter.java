package com.medavox.repeats.list_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.medavox.repeats.R;
import com.medavox.repeats.datamodels.CompletedDose;
import com.medavox.repeats.utility.DateTime;

import java.util.List;

/**
 * @author Adam Howard
@date 08/09/2016
 */
public class CompletedTaskAdapter extends BaseAdapter {

    /**Our list of Intended Doses*/
    private List<CompletedDose> data;

    /**Layout inflator to call external xml layout () */
    private static LayoutInflater inflater = null;
    private Context context;

    public CompletedTaskAdapter(Context c, List<CompletedDose> d) {
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
        CompletedDose dose = data.get(position);

        //Log.i("CompletedTaskAdapter", "convertView:"+convertView);
        View view = convertView;

        if(view == null) {
            view = inflater.inflate(R.layout.completed_dose_table_row, parent, false);
        }

        ((TextView)view.findViewById(R.id.completed_row_date)).setText(DateTime.getNiceDate(dose.getEffectiveDate()));
        ((TextView)view.findViewById(R.id.completed_row_time)).setText(DateTime.getNiceTime(dose.getEffectiveDate()));

        //long timeSince = System.currentTimeMillis() - dose.getEffectiveDate();
        //((TextView)view.findViewById(R.id.completed_row_time_since)).setText(DateTime.getDuration(timeSince));

        ((TextView)view.findViewById(R.id.completed_row_quantity)).setText(""+dose.getQuantity());
        ((TextView)view.findViewById(R.id.completed_row_status)).setText(dose.getStatus());
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
