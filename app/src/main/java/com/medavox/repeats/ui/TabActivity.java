package com.medavox.repeats.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.medavox.repeats.R;

import com.medavox.repeats.ui.fragments.CompletedDosesFragment;
import com.medavox.repeats.ui.fragments.PlanFragment;
import com.medavox.repeats.ui.fragments.EbottleFragment;
import com.medavox.repeats.ui.fragments.QuestionsFragment;
import com.medavox.repeats.ui.fragments.NetworkFragment;
import com.medavox.repeats.ui.fragments.ViewPagerAdapter;

import icepick.Icepick;

/**Implements a multi-pane tabbed interface, for relatively technical users*/
public class TabActivity extends UIActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private PlanFragment planFragment;

    private int[] tabIcons = {
            R.mipmap.medicine_tab,
            //R.mipmap.list_tab,
            R.mipmap.intended_doses_tab,
            R.mipmap.completed_doses_tab,
            R.mipmap.settings_tab,
            R.mipmap.clinic_tab
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setUpToolbar();
        setUpViewPager();
        setUpTabLayout();
        setupTabIcons();
    }

    public void onStart() {
        super.onStart();
    }

    private void setupTabIcons() {
        if(tabLayout!=null) {
            for(int i =0;i<tabIcons.length;i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setIcon(tabIcons[i]);
                }
            }
        }
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.elucid_logo_white_trans);
            getSupportActionBar().setTitle(R.string.actionBarTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if(viewPager!=null) {
            viewPager.setOffscreenPageLimit(5); //This is a hack to ensure that fragments are not destroyed. We only have 5 small frags
            addFragmentsToViewPager(viewPager);
        }
    }

    private void setUpTabLayout() {
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        if(tabLayout!=null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    private void addFragmentsToViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new EbottleFragment(), "eBottle control");
        planFragment = new PlanFragment();
        adapter.addFragment(planFragment, "Due Doses");
        adapter.addFragment(new CompletedDosesFragment(), "Taken Doses");
        adapter.addFragment(new NetworkFragment(), "Network");
        adapter.addFragment(new QuestionsFragment(), "Questions");
        viewPager.setAdapter(adapter);
    }

    //fragment behaviour (previously implemented using callbacks) has been moved into the fragments themselves
    //-- see https://developer.android.com/guide/components/fragments.html#Design
}
