package com.medavox.repeats.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.medavox.repeats.R;


public class QuestionsFragment extends Fragment implements View.OnClickListener {
    private Button option_1, option_2, option_3;
    private int user_id = 183183; //DEBUG NEED TO BE IN DB
    private int study_id = 183;
    private TextView questionTitle;


   public QuestionsFragment() {
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
       return inflater.inflate(R.layout.fragment_questions, container, false);
   }

    @Override
    public void onViewCreated(View view, Bundle bundle){
        option_1 = (Button)getView().findViewById(R.id.option1);
        option_1.setOnClickListener(this);
        option_2 = (Button)getView().findViewById(R.id.option2);
        option_2.setOnClickListener(this);
        option_3 = (Button)getView().findViewById(R.id.option3);
        option_3.setOnClickListener(this);
        questionTitle = (TextView)getView().findViewById(R.id.questionTitle);
        //EventBus.getDefault().register(this);
    }

    private void showToast(String message){

        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.option1:
                //sendData(PlatformCodes.USER_FEELING_OK);
                //showToast("Message sent: OK");
                break;
            case R.id.option2:
                //sendData(PlatformCodes.USER_FEELING_DROWSY);
                //showToast("Message sent: drowsy");
                break;
            case R.id.option3:
                //sendData(PlatformCodes.USER_FEELING_IN_PAIN);
                //showToast("Message sent: In pain");
                break;
        }
    }

}
