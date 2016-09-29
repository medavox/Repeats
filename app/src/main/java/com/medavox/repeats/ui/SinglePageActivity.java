package com.medavox.repeats.ui;

import android.os.Bundle;

import com.medavox.repeats.R;

/**
 * @author Adam Howard
@date 17/08/16
 */
/**Simple activity for non-technical users*/
public class SinglePageActivity extends UIActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_page);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
    }

}
