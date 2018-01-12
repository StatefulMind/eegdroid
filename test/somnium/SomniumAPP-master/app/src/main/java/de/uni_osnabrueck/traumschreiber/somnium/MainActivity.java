package de.uni_osnabrueck.traumschreiber.somnium;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import static de.uni_osnabrueck.traumschreiber.somnium.ConnectionFragment.mLeDeviceListAdapter;

public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener {


    // the received data via ble notification;
    public static String mDataString;
    public static BLEConnectionService mBLEConnectionService;
    public static boolean mDeviceConnected = false;
    public static String mDeviceName;
    public static String mDeviceAddress;
    public static BluetoothGattCharacteristic mNotifyCharacteristic;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLEConnectionService = ((BLEConnectionService.LocalBinder) service).getService();
            if (!mBLEConnectionService.initialize()) {
                Toast.makeText(MainActivity.this, "Unable to initialize Bluetooth", Toast.LENGTH_SHORT).show();
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
    public Button disconnectButton;
    public LineChart mChart;
    public String surveyUri = "http://goo.gl/forms/p0KYP5KXab";
    public BluetoothGattCharacteristic mDataGattCharacteristic;
    private TextView mUIDeviceName;
    private TextView mHeartRateText;
    private TextView mConnectionState;
    private ImageView mConnectionLight;


    /**
     * Callbacks form the BLE service are handled here.
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEConnectionService.ACTION_GATT_CONNECTED.equals(action)) {
                mDeviceConnected = true;
                updateConnectionState(R.string.connected);
                toggleProgressBar(0);
                disconnectButton.setVisibility(View.VISIBLE);

                //Set up the plotting fragment to display receded data
                plottingSetup();

            } else if (BLEConnectionService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mDeviceConnected = false;
                updateConnectionState(R.string.disconnected);
                clearUI();
                disconnectButton.setVisibility(View.INVISIBLE);

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

    /**
     * Set up and configure your Data Plot here
     */
    public void plottingSetup() {

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(false);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
        l.setEnabled(false);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(260f);
        leftAxis.setAxisMinimum(1f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

    }

    /**
     * gets triggered from pressing the connect button
     * Sends an intent to start the BLE service
     */
    public void connect() {

        Intent gattServiceIntent = new Intent(this, BLEConnectionService.class);
        startService(new Intent(this, BLEConnectionService.class));
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        // ui element hook up
        mUIDeviceName = (TextView) findViewById(R.id.deviceName);
        mConnectionState = (TextView) findViewById(R.id.connectionStatus);
        mConnectionLight = (ImageView) findViewById(R.id.connectionLight);
        mHeartRateText = (TextView) findViewById(R.id.heartRateText);
        mUIDeviceName.setText(mDeviceName);
        disconnectButton = (Button) findViewById(R.id.disconnectButton);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the toolbar view in the activity's layout xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Set the Toolbar as ActionBar for this activity instead of actionbar
        setSupportActionBar(toolbar);

        // Set up the ViewPager via the PageAdapter to enable navigation between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new SwipeFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this));

        // set up tabLayout navigation to the viewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        //Display Icons for Tab pages
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            if (i == 0)
                tabLayout.getTabAt(i).setIcon(R.drawable.recordrec);
            else
                tabLayout.getTabAt(i).setIcon(R.drawable.chartareaspline);
        }


        // check if the phone supports BLE, if not finish();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        //  Handle App introduction Slides
        //  Library and code used from https://github.com/PaoloRotolo/AppIntro
        //  Declare a new thread to do a preference check
        Thread introCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                // Create new boolean preference and set it to true
                boolean isFirstRun = getPrefs.getBoolean("firstStart", true);

                // If its the first start of the activity
                if (isFirstRun) {

                    //Launch app into Slides
                    Intent showIntro = new Intent(MainActivity.this, UserInstruction.class);
                    startActivity(showIntro);
                }

            }
        });

        //start the thread for checking
        introCheckThread.start();

    }

    // Inflate Toolbar Menu and handle intents
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void showAboutActivity(MenuItem mi) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);

    }

    public void showSurvey(MenuItem menuItem) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(surveyUri));
        startActivity(i);
    }

    public void showSettingsActivity(MenuItem mi) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void replayIntroActivity(MenuItem mi) {

        Intent intent = new Intent(this, UserInstruction.class);
        startActivity(intent);
    }

    // shows progressbar inside Toolbar to indicate progress;
    public void toggleProgressBar(int i) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (i == 1)
            progressBar.setVisibility(View.VISIBLE);

        else if (i == 0)
            progressBar.setVisibility(View.GONE);
        else
            return;
    }

    // Displays received data inside the Data Fragment
    private void displayData(String data) {
        if (data != null) {
            mDataString = data;
            // set text displaying full value
            if (mHeartRateText != null) {
                mHeartRateText.setText(data + " bpm");
            }
            // ads entry to the plot data set will update itself
            addEntry();
        }
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
                // show to user what happened!
                Toast.makeText(MainActivity.this, resourceId, Toast.LENGTH_LONG).show();
                TextView textView = (TextView) findViewById(R.id.bluetoothButtonTextView);
                ImageButton button = (ImageButton) findViewById(R.id.bluetoothButton);
                if (mDeviceConnected) {
                    button.setImageResource(R.drawable.ic_bt_success);
                    textView.setText("Connected!");
                    mConnectionLight.setImageResource(R.drawable.circle_green);
                } else {
                    mLeDeviceListAdapter.clear();
                    button.setImageResource(R.drawable.ic_bt_icon);
                    textView.setText(R.string.discover_a_traumschreiber);
                    mConnectionLight.setImageResource(R.drawable.circle_red);
                }
            }
        });
    }

    // resets the UI on disconnect etc
    private void clearUI() {

        if (mHeartRateText != null) {
            mHeartRateText.setText(R.string.heartrate);
        }
        if (mConnectionState != null) {
            mConnectionState.setText(R.string.status_disconnected);
            mConnectionLight.setImageResource(R.drawable.circle_red);
        }
        if (mUIDeviceName != null) {
            mUIDeviceName.setText(R.string.connect_to_a_device);
        }

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


        if (mChart != null) {
            mChart.clear();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mChart != null) {
            plottingSetup();
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBLEConnectionService != null) {
            final boolean result = mBLEConnectionService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // set up the plotting again
        plottingSetup();

        if (!mDeviceConnected) {
            clearUI();
            TextView textView = (TextView) findViewById(R.id.bluetoothButtonTextView);
            ImageButton button = (ImageButton) findViewById(R.id.bluetoothButton);
            mLeDeviceListAdapter.clear();
            button.setImageResource(R.drawable.ic_bt_icon);
            textView.setText(R.string.discover_a_traumschreiber);
            mChart.clear();
        }
    }

    /**
     * Data set that hold the received data for plotting
     *
     * @return
     */
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }


    /**
     * Adds new Data to the LineDataSet and notifies the chart about the change
     */
    private void addEntry() {
        if (mChart != null) {
            LineData data = mChart.getData();
            if (data != null) {
                ILineDataSet set = data.getDataSetByIndex(0);
                if (set == null) {
                    set = createSet();
                    data.addDataSet(set);
                }
                data.addEntry(new Entry(set.getEntryCount(), Integer.parseInt(String.valueOf(mDataString))), 0);
                data.notifyDataChanged();
                // let the chart know it's data has changed
                mChart.notifyDataSetChanged();
                // limit the number of visible entries
                mChart.setVisibleXRangeMaximum(300);
                // mChart.setVisibleYRange(30, AxisDependency.LEFT);
                // move to the latest entry
                mChart.moveViewToX(data.getEntryCount());
                // this automatically refreshes the chart (calls invalidate())
            }
        }
    }


    /**
     * on devices with android > 6.0 check and get the location permissions which are necessary for
     * BLE use
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void getPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "This permission is needed to use BLE!", Toast.LENGTH_LONG).show();
            }

            // showing the request to the user
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // check what the user did with the request
        if (requestCode == 1) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, "Permission denied - BLE search disabled", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * Checks if Location Services are enabled on the users device if on Android 6.0 or higher
     * On Android < 6.0 return true
     *
     * @return boolean
     */
    public boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager locationManager;
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } else return true;
    }


    public void disconnectDevice(View view) {

        mBLEConnectionService.disconnect();
    }

}
