package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private LineChart mChart1;
    private LineChart mChart2;
    private LineChart mChart3;
    private LineChart mChart4;
    private LineChart mChart5;
    private LineChart mChart6;
    private LineChart mChart7;
    private LineChart mChart8;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
//        TextView tv = (TextView) findViewById(R.id.data_value);
//        String eeg_data = new String((String) tv.getText());
////        Log.d(TAG, String.format("Values: " + eeg_data));
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        OnChartValueSelectedListener ol = new OnChartValueSelectedListener(){

            @Override
            public void onValueSelected(Entry entry, Highlight h) {
                //entry.getData() returns null here
            }

            @Override
            public void onNothingSelected() {

            }
        };

        mChart1 = (LineChart) findViewById(R.id.chart1);
        mChart1.setOnChartValueSelectedListener(ol);

        mChart2 = (LineChart) findViewById(R.id.chart2);
        mChart2.setOnChartValueSelectedListener(ol);

        mChart3 = (LineChart) findViewById(R.id.chart3);
        mChart3.setOnChartValueSelectedListener(ol);

        mChart4 = (LineChart) findViewById(R.id.chart4);
        mChart4.setOnChartValueSelectedListener(ol);

        mChart5 = (LineChart) findViewById(R.id.chart5);
        mChart5.setOnChartValueSelectedListener(ol);

        mChart6 = (LineChart) findViewById(R.id.chart6);
        mChart6.setOnChartValueSelectedListener(ol);

        mChart7 = (LineChart) findViewById(R.id.chart7);
        mChart7.setOnChartValueSelectedListener(ol);

        mChart8 = (LineChart) findViewById(R.id.chart8);
        mChart8.setOnChartValueSelectedListener(ol);

        // enable description text
        mChart1.getDescription().setEnabled(false);
        mChart2.getDescription().setEnabled(false);
        mChart3.getDescription().setEnabled(false);
        mChart4.getDescription().setEnabled(false);
        mChart5.getDescription().setEnabled(false);
        mChart6.getDescription().setEnabled(false);
        mChart7.getDescription().setEnabled(false);
        mChart8.getDescription().setEnabled(false);

        // enable touch gestures
        mChart1.setTouchEnabled(true);
        mChart2.setTouchEnabled(true);
        mChart3.setTouchEnabled(true);
        mChart4.setTouchEnabled(true);
        mChart5.setTouchEnabled(true);
        mChart6.setTouchEnabled(true);
        mChart7.setTouchEnabled(true);
        mChart8.setTouchEnabled(true);

        // enable scaling and dragging
        mChart1.setDragEnabled(true);
        mChart1.setScaleEnabled(true);
        mChart1.setDrawGridBackground(false);

        mChart2.setDragEnabled(true);
        mChart2.setScaleEnabled(true);
        mChart2.setDrawGridBackground(false);

        mChart3.setDragEnabled(true);
        mChart3.setScaleEnabled(true);
        mChart3.setDrawGridBackground(false);

        mChart4.setDragEnabled(true);
        mChart4.setScaleEnabled(true);
        mChart4.setDrawGridBackground(false);

        mChart5.setDragEnabled(true);
        mChart5.setScaleEnabled(true);
        mChart5.setDrawGridBackground(false);

        mChart6.setDragEnabled(true);
        mChart6.setScaleEnabled(true);
        mChart6.setDrawGridBackground(false);

        mChart7.setDragEnabled(true);
        mChart7.setScaleEnabled(true);
        mChart7.setDrawGridBackground(false);

        mChart8.setDragEnabled(true);
        mChart8.setScaleEnabled(true);
        mChart8.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart1.setPinchZoom(true);
        mChart2.setPinchZoom(true);
        mChart3.setPinchZoom(true);
        mChart4.setPinchZoom(true);
        mChart5.setPinchZoom(true);
        mChart6.setPinchZoom(true);
        mChart7.setPinchZoom(true);
        mChart8.setPinchZoom(true);

        // set an alternative background color
        mChart1.setBackgroundColor(Color.LTGRAY);
        mChart2.setBackgroundColor(Color.LTGRAY);
        mChart3.setBackgroundColor(Color.LTGRAY);
        mChart4.setBackgroundColor(Color.LTGRAY);
        mChart5.setBackgroundColor(Color.LTGRAY);
        mChart6.setBackgroundColor(Color.LTGRAY);
        mChart7.setBackgroundColor(Color.LTGRAY);
        mChart8.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart1.setData(data);
        mChart2.setData(data);
        mChart3.setData(data);
        mChart4.setData(data);
        mChart5.setData(data);
        mChart6.setData(data);
        mChart7.setData(data);
        mChart8.setData(data);

        // get the legend (only possible after setting data)
        Legend l1 = mChart1.getLegend();
        Legend l2 = mChart2.getLegend();
        Legend l3 = mChart3.getLegend();
        Legend l4 = mChart4.getLegend();
        Legend l5 = mChart5.getLegend();
        Legend l6 = mChart6.getLegend();
        Legend l7 = mChart7.getLegend();
        Legend l8 = mChart8.getLegend();

        Typeface mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

        // modify the legend ...
        l1.setForm(Legend.LegendForm.LINE);
        l1.setTypeface(mTfLight);
        l1.setTextColor(Color.WHITE);

        l2.setForm(Legend.LegendForm.LINE);
        l2.setTypeface(mTfLight);
        l2.setTextColor(Color.WHITE);

        l3.setForm(Legend.LegendForm.LINE);
        l3.setTypeface(mTfLight);
        l3.setTextColor(Color.WHITE);

        l4.setForm(Legend.LegendForm.LINE);
        l4.setTypeface(mTfLight);
        l4.setTextColor(Color.WHITE);

        l5.setForm(Legend.LegendForm.LINE);
        l5.setTypeface(mTfLight);
        l5.setTextColor(Color.WHITE);

        l6.setForm(Legend.LegendForm.LINE);
        l6.setTypeface(mTfLight);
        l6.setTextColor(Color.WHITE);

        l7.setForm(Legend.LegendForm.LINE);
        l7.setTypeface(mTfLight);
        l7.setTextColor(Color.WHITE);

        l8.setForm(Legend.LegendForm.LINE);
        l8.setTypeface(mTfLight);
        l8.setTextColor(Color.WHITE);

        XAxis xl1 = mChart1.getXAxis();
        xl1.setTypeface(mTfLight);
        xl1.setTextColor(Color.WHITE);
        xl1.setDrawGridLines(false);
        xl1.setAvoidFirstLastClipping(true);
        xl1.setEnabled(false);

        XAxis xl2 = mChart2.getXAxis();
        xl2.setTypeface(mTfLight);
        xl2.setTextColor(Color.WHITE);
        xl2.setDrawGridLines(false);
        xl2.setAvoidFirstLastClipping(true);
        xl2.setEnabled(false);

        XAxis xl3 = mChart3.getXAxis();
        xl3.setTypeface(mTfLight);
        xl3.setTextColor(Color.WHITE);
        xl3.setDrawGridLines(false);
        xl3.setAvoidFirstLastClipping(true);
        xl3.setEnabled(false);

        XAxis xl4 = mChart4.getXAxis();
        xl4.setTypeface(mTfLight);
        xl4.setTextColor(Color.WHITE);
        xl4.setDrawGridLines(false);
        xl4.setAvoidFirstLastClipping(true);
        xl4.setEnabled(false);

        XAxis xl5 = mChart5.getXAxis();
        xl5.setTypeface(mTfLight);
        xl5.setTextColor(Color.WHITE);
        xl5.setDrawGridLines(false);
        xl5.setAvoidFirstLastClipping(true);
        xl5.setEnabled(false);

        XAxis xl6 = mChart6.getXAxis();
        xl6.setTypeface(mTfLight);
        xl6.setTextColor(Color.WHITE);
        xl6.setDrawGridLines(false);
        xl6.setAvoidFirstLastClipping(true);
        xl6.setEnabled(false);

        XAxis xl7 = mChart7.getXAxis();
        xl7.setTypeface(mTfLight);
        xl7.setTextColor(Color.WHITE);
        xl7.setDrawGridLines(false);
        xl7.setAvoidFirstLastClipping(true);
        xl7.setEnabled(false);

        XAxis xl8 = mChart8.getXAxis();
        xl8.setTypeface(mTfLight);
        xl8.setTextColor(Color.WHITE);
        xl8.setDrawGridLines(false);
        xl8.setAvoidFirstLastClipping(true);
        xl8.setEnabled(false);

        YAxis leftAxis1 = mChart1.getAxisLeft();
        leftAxis1.setTypeface(mTfLight);
        leftAxis1.setTextColor(Color.WHITE);
        leftAxis1.setAxisMaximum(25f);
        leftAxis1.setAxisMinimum(-25f);
        leftAxis1.setLabelCount(3, true);
        leftAxis1.setDrawGridLines(true);

        YAxis leftAxis2 = mChart2.getAxisLeft();
        leftAxis2.setTypeface(mTfLight);
        leftAxis2.setTextColor(Color.WHITE);
        leftAxis2.setAxisMaximum(25f);
        leftAxis2.setAxisMinimum(-25f);
        leftAxis2.setLabelCount(3, true);
        leftAxis2.setDrawGridLines(true);

        YAxis leftAxis3 = mChart3.getAxisLeft();
        leftAxis3.setTypeface(mTfLight);
        leftAxis3.setTextColor(Color.WHITE);
        leftAxis3.setAxisMaximum(25f);
        leftAxis3.setAxisMinimum(-25f);
        leftAxis3.setLabelCount(3, true);
        leftAxis3.setDrawGridLines(true);

        YAxis leftAxis4 = mChart4.getAxisLeft();
        leftAxis4.setTypeface(mTfLight);
        leftAxis4.setTextColor(Color.WHITE);
        leftAxis4.setAxisMaximum(25f);
        leftAxis4.setAxisMinimum(-25f);
        leftAxis4.setLabelCount(3, true);
        leftAxis4.setDrawGridLines(true);

        YAxis leftAxis5 = mChart5.getAxisLeft();
        leftAxis5.setTypeface(mTfLight);
        leftAxis5.setTextColor(Color.WHITE);
        leftAxis5.setAxisMaximum(25f);
        leftAxis5.setAxisMinimum(-25f);
        leftAxis5.setLabelCount(3, true);
        leftAxis5.setDrawGridLines(true);

        YAxis leftAxis6 = mChart6.getAxisLeft();
        leftAxis6.setTypeface(mTfLight);
        leftAxis6.setTextColor(Color.WHITE);
        leftAxis6.setAxisMaximum(25f);
        leftAxis6.setAxisMinimum(-25f);
        leftAxis6.setLabelCount(3, true);
        leftAxis6.setDrawGridLines(true);

        YAxis leftAxis7 = mChart7.getAxisLeft();
        leftAxis7.setTypeface(mTfLight);
        leftAxis7.setTextColor(Color.WHITE);
        leftAxis7.setAxisMaximum(25f);
        leftAxis7.setAxisMinimum(-25f);
        leftAxis7.setLabelCount(3, true);
        leftAxis7.setDrawGridLines(true);

        YAxis leftAxis8 = mChart8.getAxisLeft();
        leftAxis8.setTypeface(mTfLight);
        leftAxis8.setTextColor(Color.WHITE);
        leftAxis8.setAxisMaximum(25f);
        leftAxis8.setAxisMinimum(-25f);
        leftAxis8.setLabelCount(3, true);
        //leftAxis8.setGranularityEnabled(true);
        leftAxis8.setDrawGridLines(true);

        YAxis rightAxis1 = mChart1.getAxisRight();
        rightAxis1.setEnabled(false);

        YAxis rightAxis2 = mChart2.getAxisRight();
        rightAxis2.setEnabled(false);

        YAxis rightAxis3 = mChart3.getAxisRight();
        rightAxis3.setEnabled(false);

        YAxis rightAxis4 = mChart4.getAxisRight();
        rightAxis4.setEnabled(false);

        YAxis rightAxis5 = mChart5.getAxisRight();
        rightAxis5.setEnabled(false);

        YAxis rightAxis6 = mChart6.getAxisRight();
        rightAxis6.setEnabled(false);

        YAxis rightAxis7 = mChart7.getAxisRight();
        rightAxis7.setEnabled(false);

        YAxis rightAxis8 = mChart8.getAxisRight();
        rightAxis8.setEnabled(false);


    }

    public void addEntry(float f) {

        LineData data1 = mChart1.getData();


        if (data1 != null) {

            ILineDataSet set1 = data1.getDataSetByIndex(0);

            // set.addEntry(...); // can be called as well

            if (set1 == null) {
                set1 = createSet();
                data1.addDataSet(set1);
            }

            data1.addEntry(new Entry(set1.getEntryCount(), f), 0);
            data1.notifyDataChanged();

            // let the chart know it's data has changed
            mChart1.notifyDataSetChanged();

            // limit the number of visible entries
            mChart1.setVisibleXRangeMaximum(20);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart1.moveViewToX(data1.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    public void addEntry2(float f) {

        LineData data2 = mChart2.getData();


        if (data2 != null) {

            ILineDataSet set2 = data2.getDataSetByIndex(0);

            // set.addEntry(...); // can be called as well

            if (set2 == null) {
                set2 = createSet2();
                data2.addDataSet(set2);
            }

            data2.addEntry(new Entry(set2.getEntryCount(), f), 0);
            data2.notifyDataChanged();

            // let the chart know it's data has changed
            mChart2.notifyDataSetChanged();

            // limit the number of visible entries
            mChart2.setVisibleXRangeMaximum(20);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart2.moveViewToX(data2.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    public void addEntry3(float f) {

        LineData data3 = mChart3.getData();


        if (data3 != null) {

            ILineDataSet set3 = data3.getDataSetByIndex(0);

            // set.addEntry(...); // can be called as well

            if (set3 == null) {
                set3 = createSet3();
                data3.addDataSet(set3);
            }

            data3.addEntry(new Entry(set3.getEntryCount(), f), 0);
            data3.notifyDataChanged();

            // let the chart know it's data has changed
            mChart3.notifyDataSetChanged();

            // limit the number of visible entries
            mChart3.setVisibleXRangeMaximum(20);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart3.moveViewToX(data3.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    public void addEntry4(float f) {

        LineData data4 = mChart4.getData();


        if (data4 != null) {

            ILineDataSet set4 = data4.getDataSetByIndex(0);

            // set.addEntry(...); // can be called as well

            if (set4 == null) {
                set4 = createSet4();
                data4.addDataSet(set4);
            }

            data4.addEntry(new Entry(set4.getEntryCount(), f), 0);
            data4.notifyDataChanged();

            // let the chart know it's data has changed
            mChart4.notifyDataSetChanged();

            // limit the number of visible entries
            mChart4.setVisibleXRangeMaximum(20);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart4.moveViewToX(data4.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    public void addEntry5(float f) {

        LineData data5 = mChart5.getData();


        if (data5 != null) {

            ILineDataSet set5 = data5.getDataSetByIndex(0);

            // set.addEntry(...); // can be called as well

            if (set5 == null) {
                set5 = createSet5();
                data5.addDataSet(set5);
            }

            data5.addEntry(new Entry(set5.getEntryCount(), f), 0);
            data5.notifyDataChanged();

            // let the chart know it's data has changed
            mChart5.notifyDataSetChanged();

            // limit the number of visible entries
            mChart5.setVisibleXRangeMaximum(20);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart5.moveViewToX(data5.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    public void addEntry6(float f) {

        LineData data6 = mChart6.getData();


        if (data6 != null) {

            ILineDataSet set6 = data6.getDataSetByIndex(0);

            // set.addEntry(...); // can be called as well

            if (set6 == null) {
                set6 = createSet6();
                data6.addDataSet(set6);
            }

            data6.addEntry(new Entry(set6.getEntryCount(), f), 0);
            data6.notifyDataChanged();

            // let the chart know it's data has changed
            mChart6.notifyDataSetChanged();

            // limit the number of visible entries
            mChart6.setVisibleXRangeMaximum(20);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart6.moveViewToX(data6.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    public void addEntry7(float f) {

        LineData data7 = mChart7.getData();


        if (data7 != null) {

            ILineDataSet set7 = data7.getDataSetByIndex(0);

            // set.addEntry(...); // can be called as well

            if (set7 == null) {
                set7 = createSet7();
                data7.addDataSet(set7);
            }

            data7.addEntry(new Entry(set7.getEntryCount(), f), 0);
            data7.notifyDataChanged();

            // let the chart know it's data has changed
            mChart7.notifyDataSetChanged();

            // limit the number of visible entries
            mChart7.setVisibleXRangeMaximum(20);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart7.moveViewToX(data7.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    public void addEntry8(float f) {

        LineData data8 = mChart8.getData();


        if (data8 != null) {

            ILineDataSet set8 = data8.getDataSetByIndex(0);

            // set.addEntry(...); // can be called as well

            if (set8 == null) {
                set8 = createSet8();
                data8.addDataSet(set8);
            }

            data8.addEntry(new Entry(set8.getEntryCount(), f), 0);
            data8.notifyDataChanged();

            // let the chart know it's data has changed
            mChart8.notifyDataSetChanged();

            // limit the number of visible entries
            mChart8.setVisibleXRangeMaximum(20);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart8.moveViewToX(data8.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set1 = new LineDataSet(null, "");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setCircleColor(Color.WHITE);
        set1.setLineWidth(1f);
        set1.setCircleRadius(1f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setValueTextColor(Color.WHITE);
        set1.setValueTextSize(0.1f);
        set1.setDrawValues(false);
        return set1;
    }

    private LineDataSet createSet2() {

        LineDataSet set2 = new LineDataSet(null, "");
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setColor(Color.GREEN);
        set2.setCircleColor(Color.WHITE);
        set2.setLineWidth(1f);
        set2.setCircleRadius(1f);
        set2.setFillAlpha(65);
        //set.setHighLightColor(Color.rgb(44, 117, 117));
        set2.setValueTextColor(Color.WHITE);
        set2.setValueTextSize(0.1f);
        set2.setDrawValues(false);
        return set2;
    }

    private LineDataSet createSet3() {

        LineDataSet set3 = new LineDataSet(null, "");
        set3.setAxisDependency(YAxis.AxisDependency.LEFT);
        set3.setColor(Color.CYAN);
        set3.setCircleColor(Color.WHITE);
        set3.setLineWidth(1f);
        set3.setCircleRadius(1f);
        set3.setFillAlpha(65);
        //set.setHighLightColor(Color.rgb(44, 117, 117));
        set3.setValueTextColor(Color.WHITE);
        set3.setValueTextSize(0.1f);
        set3.setDrawValues(false);
        return set3;
    }

    private LineDataSet createSet4() {

        LineDataSet set4 = new LineDataSet(null, "");
        set4.setAxisDependency(YAxis.AxisDependency.LEFT);
        set4.setColor(Color.MAGENTA);
        set4.setCircleColor(Color.WHITE);
        set4.setLineWidth(1f);
        set4.setCircleRadius(1f);
        set4.setFillAlpha(65);
        //set.setHighLightColor(Color.rgb(44, 117, 117));
        set4.setValueTextColor(Color.WHITE);
        set4.setValueTextSize(0.1f);
        set4.setDrawValues(false);
        return set4;
    }

    private LineDataSet createSet5() {

        LineDataSet set5 = new LineDataSet(null, "");
        set5.setAxisDependency(YAxis.AxisDependency.LEFT);
        set5.setColor(Color.WHITE);
        set5.setCircleColor(Color.WHITE);
        set5.setLineWidth(1f);
        set5.setCircleRadius(1f);
        set5.setFillAlpha(65);
        //set.setHighLightColor(Color.rgb(44, 117, 117));
        set5.setValueTextColor(Color.WHITE);
        set5.setValueTextSize(0.1f);
        set5.setDrawValues(false);
        return set5;
    }

    private LineDataSet createSet6() {

        LineDataSet set6 = new LineDataSet(null, "");
        set6.setAxisDependency(YAxis.AxisDependency.LEFT);
        set6.setColor(Color.BLACK);
        set6.setCircleColor(Color.WHITE);
        set6.setLineWidth(1f);
        set6.setCircleRadius(1f);
        set6.setFillAlpha(65);
        //set.setHighLightColor(Color.rgb(44, 117, 117));
        set6.setValueTextColor(Color.WHITE);
        set6.setValueTextSize(0.1f);
        set6.setDrawValues(false);
        return set6;
    }

    private LineDataSet createSet7() {

        LineDataSet set7 = new LineDataSet(null, "");
        set7.setAxisDependency(YAxis.AxisDependency.LEFT);
        set7.setColor(Color.YELLOW);
        set7.setCircleColor(Color.WHITE);
        set7.setLineWidth(1f);
        set7.setCircleRadius(1f);
        set7.setFillAlpha(65);
        //set.setHighLightColor(Color.rgb(44, 117, 117));
        set7.setValueTextColor(Color.WHITE);
        set7.setValueTextSize(0.1f);
        set7.setDrawValues(false);
        return set7;
    }

    private LineDataSet createSet8() {

        LineDataSet set8 = new LineDataSet(null, "");
        set8.setAxisDependency(YAxis.AxisDependency.LEFT);
        set8.setColor(Color.BLACK);
        set8.setCircleColor(Color.WHITE);
        set8.setLineWidth(1f);
        set8.setCircleRadius(1f);
        set8.setFillAlpha(65);
        //set.setHighLightColor(Color.rgb(44, 117, 117));
        set8.setValueTextColor(Color.WHITE);
        set8.setValueTextSize(0.1f);
        set8.setDrawValues(false);
        return set8;
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private int cnt = 0;
    private void displayData(String data) {
        cnt += 1;
        // Conversion formula: V_in = X*1.65V/(1000 * GAIN * 2048)
        // Assuming GAIN = 64
        if (cnt % 10 == 0) {
            final float numerator = 1650000;
            final float denominator = 1000 * 64 * 2048;
            final String[] parts = data.split(" ");
            final List<Float> data_raw = new ArrayList<>();



            addEntry((Float.parseFloat(parts[0]) * numerator ) / denominator);
            addEntry2((Float.parseFloat(parts[1]) * numerator ) / denominator);
            addEntry3((Float.parseFloat(parts[2]) * numerator ) / denominator);
            addEntry4((Float.parseFloat(parts[3]) * numerator ) / denominator);
            addEntry5((Float.parseFloat(parts[4]) * numerator ) / denominator);
            addEntry6((Float.parseFloat(parts[5]) * numerator ) / denominator);
            addEntry7((Float.parseFloat(parts[6]) * numerator ) / denominator);
            addEntry8((Float.parseFloat(parts[7]) * numerator ) / denominator);
        }


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int count = 1;
//                for (String part : parts) {
//                    float f = Float.parseFloat(part);
//                    f = (f * numerator) / denominator;
//                    data_raw.add(f);
//                    switch (count) {
//                        case 1:
//                            addEntry(f);
//                            break;
//                        case 2:
//                            addEntry2(f);
//                            break;
//                        case 3:
//                            addEntry3(f);
//                            break;
//                        case 4:
//                            addEntry4(f);
//                            break;
//                        case 5:
//                            addEntry5(f);
//                            break;
//                        case 6:
//                            addEntry6(f);
//                            break;
//                        case 7:
//                            addEntry7(f);
//                            break;
//                        case 8:
//                            addEntry8(f);
//                            break;
//                    }
//                    count++;
//                }
//            }
//        }).start();

        if (data != null) {
            // data format example: +01012 -00234 +01374 -01516 +01656 +01747 +00131 -00351
            mDataField.setText(data); // print the n-dimensional array after the data
        }
    }

//    private INDArray dataToMicroVolts(INDArray data) {
//        // Conversion formula: V_in = X*1.65V/(1000 * GAIN * 2048)
//        // Assuming GAIN = 64
//        float denominator = 1000 * 64 * 2048;
//        INDArray result = (data.mul(1650000)).div(denominator);
//        return result;
//    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            if(uuid.equals("a22686cb-9268-bd91-dd4f-b52d03d85593")) {
                currentServiceData.put(LIST_NAME, "EEG Data Service");
                currentServiceData.put(LIST_UUID, uuid);
                gattServiceData.add(currentServiceData);

                ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                        new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> charas =
                        new ArrayList<BluetoothGattCharacteristic>();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);

                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
                    uuid = gattCharacteristic.getUuid().toString();
                    currentCharaData.put(
                            LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                    // If not really needed since the filtered service has only one characteristic
                    if(uuid.equals("faa7b588-19e5-f590-0545-c99f193c5c3e")){
                        currentCharaData.put(LIST_NAME, "EEG Data Values");
                        currentCharaData.put(LIST_UUID, uuid);
                        gattCharacteristicGroupData.add(currentCharaData);
                    }
                }
                mGattCharacteristics.add(charas);
                gattCharacteristicData.add(gattCharacteristicGroupData);
            }
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
