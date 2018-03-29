package de.uni_osnabrueck.traumschreiber.epilepsy.eegdroidgui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ConnectToDeviceFragment.OnFragmentInteractionListener, ShowStatisticsFragment.OnFragmentInteractionListener {
    private final static String TAG = MainActivity.class.getSimpleName();


    private BluetoothAdapter mBluetoothAdapter;

    //Will hold the attributes of a connected Traumschreiber, when done
    public static boolean mDeviceConnected = false;
    public static String mDeviceName;
    public static String mDeviceAddress;
    //public static BluetoothGattCharacteristic mNotifyCharacteristic;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public BluetoothGattCharacteristic mDataGattCharacteristic;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            // TODO: If BLE is not supported, we should do graceful error handling
            return;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            // TODO: Graceful error handling again
            return;
        }

        //mTraumschreiberHandler = new TraumschreiberHandler(mBluetoothAdapter, this);
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

    //TODO: Read the Toast
    public void getPermission() {
        Toast.makeText(this, "It is not yet implemented to ask for permission. Do so ASAP. Apps may crash otherwise. Use Somnium as orientation", Toast.LENGTH_SHORT).show();
    }

    //TODO: This methods checks the locationPermission
    public boolean checkLocationPermission() {
        return true;
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
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 1) {
                return ConnectToDeviceFragment.newInstance();
            }

            if (position == 2) {
                return ShowStatisticsFragment.newInstance("Test 1", "param 2");
            }


            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }


    //https://stackoverflow.com/questions/24777985/how-to-implement-onfragmentinteractionlistener#27666001
    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }


    public static String mDataString;
    public static BLEConnectionService mBLEConnectionService;
    public static BluetoothGattCharacteristic mNotifyCharacteristic;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLEConnectionService = ((BLEConnectionService.LocalBinder) service).getService();
            if (!mBLEConnectionService.initialize()) {
                Toast.makeText(MainActivity.this, "Unable to initialize Bluetooth", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onServiceConnected: Unable to initialize Bluetooth");
                finish();
            }
            // connects to the selected device
            mBLEConnectionService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLEConnectionService = null;
        }
    };



    //TODO: This method is supposed to connect to a Traumschreiber device. Implement later
    public void connect(String deviceAddress) {

        Log.d(TAG, "connect: Called connect for device with address " + deviceAddress);

        Intent gattServiceIntent = new Intent(this, BLEConnectionService.class);
        startService(new Intent(this, BLEConnectionService.class));
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "connect: Trying to connect to device " + deviceAddress);

    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEConnectionService.ACTION_GATT_CONNECTED.equals(action)) {
                mDeviceConnected = true;
                updateConnectionState(R.string.connected);
//                toggleProgressBar(0);
//                disconnectButton.setVisibility(View.VISIBLE);

                //Set up the plotting fragment to display receded data
//                plottingSetup();

            } else if (BLEConnectionService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mDeviceConnected = false;
                updateConnectionState(R.string.disconnected);
                clearUI();
//                disconnectButton.setVisibility(View.INVISIBLE);

                unbindService(mServiceConnection);
                mBLEConnectionService = null;

            } else if (BLEConnectionService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                // loop trough the device services !
                mDataGattCharacteristic = mBLEConnectionService.getSupportedGattServices().get(2).getCharacteristics().get(0);

                mNotifyCharacteristic = mDataGattCharacteristic;

                Log.i("fetched UUID ", mNotifyCharacteristic.getUuid().toString());

                mBLEConnectionService.setCharacteristicNotification(
                        mDataGattCharacteristic, true);


            } else if (BLEConnectionService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BLEConnectionService.EXTRA_DATA));
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEConnectionService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEConnectionService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEConnectionService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEConnectionService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // Displays received data inside the Data Fragment
    private void displayData(String data) {
        if (data != null) {
            mDataString = data;
            // set text displaying full value
//            if (mHeartRateText != null) {
//                mHeartRateText.setText(data + " bpm");
//            }
//            // ads entry to the plot data set will update itself
//            addEntry();
            Log.d(TAG, "displayData: Received data: "+data);
        }
    }


    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mConnectionState.setText(resourceId);
                // show to user what happened!
                Toast.makeText(MainActivity.this, resourceId, Toast.LENGTH_LONG).show();
                TextView textView = (TextView) findViewById(R.id.bluetoothButtonTextView);
                ImageButton button = (ImageButton) findViewById(R.id.bluetoothButton);
                if (mDeviceConnected) {
//                    button.setImageResource(R.drawable.ic_bt_success);
//                    textView.setText("Connected!");
//                    mConnectionLight.setImageResource(R.drawable.circle_green);
                    Toast.makeText(MainActivity.this, "We are connected!", Toast.LENGTH_SHORT).show();
                } else {
//                    mLeDeviceListAdapter.clear();
//                    button.setImageResource(R.drawable.ic_bt_icon);
//                    textView.setText(R.string.discover_a_traumschreiber);
//                    mConnectionLight.setImageResource(R.drawable.circle_red);
                    Toast.makeText(MainActivity.this, "We are disconnected!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // resets the UI on disconnect etc
    private void clearUI() {
//
//        if (mHeartRateText != null) {
//            mHeartRateText.setText(R.string.heartrate);
//        }
//        if (mConnectionState != null) {
//            mConnectionState.setText(R.string.status_disconnected);
//            mConnectionLight.setImageResource(R.drawable.circle_red);
//        }
//        if (mUIDeviceName != null) {
//            mUIDeviceName.setText(R.string.connect_to_a_device);
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBLEConnectionService != null) {
            mBLEConnectionService.killNotification();
        }
        mBLEConnectionService = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);


//        if (mChart != null) {
//            mChart.clear();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//
//        if (mChart != null) {
//            plottingSetup();
//        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBLEConnectionService != null) {
            final boolean result = mBLEConnectionService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

//        // set up the plotting again
//        plottingSetup();
//
//        if (!mDeviceConnected) {
//            clearUI();
//            TextView textView = (TextView) findViewById(R.id.bluetoothButtonTextView);
//            ImageButton button = (ImageButton) findViewById(R.id.bluetoothButton);
//            mLeDeviceListAdapter.clear();
//            button.setImageResource(R.drawable.ic_bt_icon);
//            textView.setText(R.string.discover_a_traumschreiber);
//            mChart.clear();
//        }
    }

    public void disconnectDevice(View view) {

        mBLEConnectionService.disconnect();
    }


}
