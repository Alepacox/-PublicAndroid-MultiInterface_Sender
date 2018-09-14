package it.unicam.project.multiinterfacesender;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity implements Receive.DataCommunication, Send_step_1_manual.DataCommunication,
        Send.DataCommunication, Send_step_2.OnFragmentInteractionListener,
        Receive_step_1.DataCommunication,
        Send_step_1_auto.OnFragmentInteractionListener, Receive_step_2.DataCommunication  {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;
    private AppBarLayout appbar;
    private Toolbar toolbar;
    private boolean noLogin;
    private String deviceID;
    //Sender variables
    private boolean usingWifi;
    private boolean usingMobile;
    private boolean usingBluetooth;
    private boolean firstTimeManual=true;
    //Receiver variables
    private String wifiIp;
    private String mobileIp;
    private String bluetoothName;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent myIntent= getIntent();
        noLogin= myIntent.getBooleanExtra("nologin", true);
        deviceID= myIntent.getStringExtra("deviceID");
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        appbar= findViewById(R.id.appbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        changeSectionColor(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                changeSectionColor(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void setInterfaces(boolean[] value) {
        this.usingWifi=value[0];
        this.usingMobile=value[1];
        this.usingBluetooth=value[2];
    }

    @Override
    public boolean isTheFirstTimeManual() {
        if(firstTimeManual){
            firstTimeManual=false;
            return true;
        } else return false;
    }

    @Override
    public void setInterfacesDetails(String mobileIp, String wifiIp, String bluetoothName) {
        this.mobileIp=mobileIp;
        this.wifiIp=wifiIp;
        this.bluetoothName=bluetoothName;
    }

    @Override
    public String[] getInterfacesDetails() {
        return new String[]{mobileIp, wifiIp, bluetoothName};
    }

    @Override
    public boolean getNoLoginMode() {
        return noLogin;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private int numOfTabs;
        public SectionsPagerAdapter(FragmentManager fm, int numOfTabs) {
            super(fm);
            this.numOfTabs = numOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Send();
                case 1:
                    return new Receive();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void changeSectionColor(int section){
        int colorPrimary=0;
        int colorPrimaryDark=0;
        if(section==0){
            colorPrimary=R.color.sendPrimaryColor;
            colorPrimaryDark=R.color.sendPrimaryColorDark;
        } else {
            colorPrimary=R.color.receivePrimaryColor;
            colorPrimaryDark=R.color.receivePrimaryColorDark;
        }
        appbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this,
                colorPrimary));
        toolbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this,
                colorPrimary));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this,
                    colorPrimaryDark));
            getWindow().setNavigationBarColor(ContextCompat.getColor(MainActivity.this,
                    colorPrimary));
        }
    }


    public static void snackBarNav(final Activity activity, int containerLayour, String message, int lenght, int section) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(containerLayour),
                message, lenght);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int colorPrimary=0;
            if(section==0){
                colorPrimary=R.color.sendPrimaryColor;
            } else {
                colorPrimary=R.color.receivePrimaryColor;
            }
            final int finalColorPrimary = colorPrimary;
            snackbar.addCallback(new Snackbar.Callback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity,
                            finalColorPrimary));
                }
            });
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity,
                    R.color.snackbar_background));
        }
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
