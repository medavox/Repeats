package com.medavox.repeats.ui.fragments;


import android.content.Context;
import android.support.v4.app.Fragment;

import com.medavox.repeats.events.UIMessageEvent;
import com.medavox.repeats.ui.UIActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author Adam Howard
@date 23/08/16
 */
public abstract class UpdatableFragment extends Fragment {
    protected UIActivity owner;

    public abstract void updateUI();

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        updateUI();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        //System.out.println("instance of UIActivity:"+(c instanceof UIActivity));
        //create a reference to the UIActivity this fragment has become associated with
        if(c instanceof UIActivity) {
            owner = (UIActivity)c;
            //System.out.println("owner:"+owner);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        owner = null;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)//costly? avoidable??
    public void onUIMessageEvent(UIMessageEvent uime) {
        if (uime.getRecipientID().equals(UIMessageEvent.BROADCAST)) {
            switch ((UIMessageEvent.BroadcastMessages) uime.getDetails()) {
                case UPDATE:
                    updateUI();
            }
        }
    }
}
