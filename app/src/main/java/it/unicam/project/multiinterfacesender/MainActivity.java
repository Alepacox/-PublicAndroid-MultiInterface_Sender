package it.unicam.project.multiinterfacesender;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import it.unicam.project.multiinterfacesender.Receive.Receive;
import it.unicam.project.multiinterfacesender.Receive.Receive_step_1;
import it.unicam.project.multiinterfacesender.Receive.Receive_step_2;
import it.unicam.project.multiinterfacesender.Send.Send;
import it.unicam.project.multiinterfacesender.Send.Send_device_list;
import it.unicam.project.multiinterfacesender.Send.Send_step_1_auto;
import it.unicam.project.multiinterfacesender.Send.Send_step_1_manual;
import it.unicam.project.multiinterfacesender.Send.Send_step_2;
import it.unicam.project.multiinterfacesender.Service.BluetoothSend;

public class MainActivity extends AppCompatActivity implements Receive.DataCommunication, Send_step_1_manual.DataCommunication,
        Send.DataCommunication, Send_step_2.DataCommunication,
        Receive_step_1.DataCommunication, Send_device_list.DataCommunication,
        Send_step_1_auto.DataCommunication, Receive_step_2.DataCommunication  {

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
    private CustomViewPager mViewPager;
    private boolean noLogin;
    //User details
    private String username;
    private String devicename;
    private String uToken;
    private String dToken;
    //Sender variables
    private boolean firstTimeManual=true;
    private boolean usingWifi;
    private boolean usingMobile;
    private boolean usingBluetooth;
    //Receiver variables
    private String wifiIp;
    private String wifiSSID;
    private boolean mobileIp;
    private String bluetoothName;
    private RWStorage rw;
    //AIDL stuff
    public static Intent wifiServiceIntent;
    public static Intent mobileServiceIntent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent myIntent= getIntent();
        noLogin= myIntent.getBooleanExtra("nologin", true);
        username = myIntent.getStringExtra("username");
        devicename = myIntent.getStringExtra("devicename");
        uToken= myIntent.getStringExtra("uToken");
        dToken= myIntent.getStringExtra("dToken");
        rw= new RWStorage(this);
        if(!noLogin){
            rw.writeDeviceToken(dToken);
            rw.writeUserToken(uToken);
        }
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        appbar= findViewById(R.id.appbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mViewPager = (CustomViewPager) findViewById(R.id.container);
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
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isTheFirstTimeManual() {
        if(firstTimeManual){
            firstTimeManual=false;
            return true;
        } else return false;
    }

    @Override
    public void setUsedInterfaces(boolean usingWifi, boolean usingMobile, boolean usingBluetooth) {
        this.usingWifi=usingWifi;
        this.usingMobile=usingMobile;
        this.usingBluetooth=usingBluetooth;
    }

    @Override
    public void setInterfacesDetails(boolean mobileIp, String wifiIp, String wifiSSID, String bluetoothName) {
        this.mobileIp=mobileIp;
        this.wifiIp=wifiIp;
        this.wifiSSID=wifiSSID;
        this.bluetoothName=bluetoothName;
    }

    @Override
    public String[] getInterfacesDetails() {
        return new String[]{String.valueOf(mobileIp), wifiIp, wifiSSID, bluetoothName};
    }

    @Override
    public String getDeviceName() {
        return devicename;
    }

    @Override
    public boolean getNoLoginMode() {
        return noLogin;
    }

    @Override
    public void setSwipable(boolean swipable) {
        this.mViewPager.setSwipeable(swipable);
        if(swipable) {
            this.tabLayout.setVisibility(View.VISIBLE);
        } else this.tabLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setChoosenDevice(String ID) {

    }
    @Override
    public String getUToken() {
        return uToken;
    }

    @Override
    public String getDToken() {
        return dToken;
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

    public void logout() {
        rw.deleteUserToken();
        Intent i = new Intent(this, Login.class);
        Toast.makeText(this, "Logout avvenuto con successo", Toast.LENGTH_LONG).show();
        startActivity(i);
        finish();
    }


    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        Fragment test = (Fragment) getSupportFragmentManager().findFragmentByTag("Send_step_2");
        if (test != null && test.isVisible()) {
            if (doubleBackToExitPressedOnce) {
                if(usingWifi){
                    DirectlyConnect.disconnectWifi();
                    unbindService(DirectlyConnect.wifiServiceConnection);
                    android.os.Process.killProcess(DirectlyConnect.wifiProcessID);
                }
                if(usingMobile){
                    DirectlyConnect.disconnectMobile();
                    unbindService(DirectlyConnect.mobileServiceConnection);
                    android.os.Process.killProcess(DirectlyConnect.mobileProcessID);
                }
                if(usingBluetooth){
                    BluetoothSend.clear();
                }
                Toast.makeText(getApplicationContext(), "Connessione terminata", Toast.LENGTH_SHORT).show();
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            MainActivity.snackBarNav(this, R.id.send_container,
                    "Premi ancora indietro per chiudere la connessione", Snackbar.LENGTH_SHORT, 0);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
        else {
            super.onBackPressed();
        }
    }
}
