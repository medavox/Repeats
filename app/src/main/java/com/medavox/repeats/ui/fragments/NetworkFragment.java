package com.medavox.repeats.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.medavox.repeats.R;

import butterknife.ButterKnife;


public class NetworkFragment extends Fragment implements View.OnClickListener {




   public NetworkFragment() {
       // Required empty public constructor
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
       // Inflate the layout for this fragment
       View mView =  inflater.inflate(R.layout.fragment_settings, container, false);
       ButterKnife.bind(this, mView);
       return mView;
   }
    @Override
    public void onViewCreated(View view, Bundle bundle){



    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.buttonProtocol:

                break;
            case R.id.buttonDelete:

                break;
        }
    }




}
