package de.uni_osnabrueck.ikw.eegdroid;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Environment;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.FileWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.nd4j.linalg.indexing.NDArrayIndex.interval;

public class Record extends AppCompatActivity {

    private final static String TAG = Record.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private ImageButton start_record_button;
    private ImageButton pause_record_button;
    private ImageButton save_record_button;
    private ImageButton discard_record_button;

    private boolean recording = false;
    private INDArray main_data;
    private String start_time;
    private String end_time;
    private long start_watch;
    private String recording_time;
    private String session_label;
    private LineChart mChart;
    private ArrayList<Entry> lineEntries;
    private int count;
    private int cnt;


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
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                mConnectionState.setText(R.string.device_connected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;

                //invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                if (recording) {
                    storeData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }

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
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        TextView tv = (TextView) findViewById(R.id.data_value);
        String eeg_data = new String((String) tv.getText());
        Log.d(TAG, String.format("Values: " + eeg_data));

        mDeviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mConnectionState.setText(R.string.device_found);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        /// Buttons to record EEG data ///
        start_record_button = (ImageButton) findViewById(R.id.start_record_button);
        pause_record_button = (ImageButton) findViewById(R.id.pause_record_button);
        save_record_button = (ImageButton) findViewById(R.id.save_record_button);
        discard_record_button = (ImageButton) findViewById(R.id.discard_record_button);

        start_record_button.setOnClickListener(startRecord_OnClickListener);
        pause_record_button.setOnClickListener(pauseRecord_OnClickListener);
        save_record_button.setOnClickListener(saveRecord_OnClickListener);
        discard_record_button.setOnClickListener(discardRecord_OnClickListener);

        /// Plotting of EEG data ///
        lineEntries = new ArrayList<Entry>();
        count = 0;
        OnChartValueSelectedListener OnChartListener = new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight h) {
                //entry.getData() returns null here
            }

            @Override
            public void onNothingSelected() {
            }
        };

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(OnChartListener);
        mChart.setData(data);

        cnt = 0;


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
        mBluetoothLeService.disconnect();
        mBluetoothLeService = null;
        mConnectionState.setText(R.string.disconnected);
    }


    private void displayData(String data) {
        cnt += 1;
        // Conversion formula: V_in = X*1.65V/(1000 * GAIN * 2048)
        // Assuming GAIN = 64
        if (cnt % 10 == 0) {
            final float numerator = 1650000;
            final float denominator = 1000 * 64 * 2048;
            final String[] parts = data.split(" ");
            final List<Float> data_raw = new ArrayList<>();

            addEntry((Float.parseFloat(parts[0]) * numerator) / denominator);
        }
        if (data != null) {
            // data format example: +01012 -00234 +01374 -01516 +01656 +01747 +00131 -00351
            mDataField.setText(data); // print the n-dimensional array after the data
        }
    }

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
            if (uuid.equals("a22686cb-9268-bd91-dd4f-b52d03d85593")) {
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
                    if (uuid.equals("faa7b588-19e5-f590-0545-c99f193c5c3e")) {
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
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
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


    // startRecord button settings
    private View.OnClickListener startRecord_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startRecord_clicked();
        }
    };

    private void startRecord_clicked() {
        if (recording) {
            Toast.makeText(this, R.string.startRecord_clicked_whileRecording, Toast.LENGTH_LONG).show();
        } else {
            main_data = Nd4j.zeros(1, 8);
            start_time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            start_watch = System.currentTimeMillis();
            recording = true;
            Toast.makeText(this, R.string.startRecord_clicked, Toast.LENGTH_LONG).show();
            mConnectionState.setText(R.string.startRecord_clicked_whileRecording);
        }
    }


    // pauseRecord button settings
    private View.OnClickListener pauseRecord_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            pauseRecord_clicked();
        }
    };

    private void pauseRecord_clicked() {
        if (recording) {
            recording = false;
            end_time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            long stop_watch = System.currentTimeMillis();
            recording_time = Long.toString(stop_watch - start_watch);
            Toast.makeText(this, R.string.pausedRecord_clicked, Toast.LENGTH_LONG).show();
            mConnectionState.setText(R.string.pausedRecord_clicked);
        }
    }

    // saveRecord button settings
    private View.OnClickListener saveRecord_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveRecord_clicked();
        }
    };

    private void saveRecord_clicked() {
        if (recording) {
            Toast.makeText(this, R.string.saveRecord_clicked_whileRecording, Toast.LENGTH_LONG).show();
        } else {
            askForLabel();
            if (session_label == null) {
                saveSession();
            } else {
                saveSession(session_label);
                session_label = null;
            }
            Toast.makeText(this, R.string.saveRecord_clicked, Toast.LENGTH_LONG).show();
            mConnectionState.setText(R.string.device_connected);
        }
    }

    // discardRecord button settings
    private View.OnClickListener discardRecord_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            discardRecord_clicked();
        }
    };

    private void discardRecord_clicked() {
        Toast.makeText(this, R.string.discardRecord_clicked, Toast.LENGTH_LONG).show();
        mConnectionState.setText(R.string.device_connected);
    }


    private void askForLabel() {
        new MaterialDialog.Builder(this)
                .title("Please, enter the session label")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("E.g. walking, eating, sleeping, etc.",
                        "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                session_label = input.toString();
                            }
                        }).show();
    }


    private void storeData(String data) {
// Conversion formula: V_in = X*1.65V/(1000 * GAIN * 2048)
// Assuming GAIN = 64
        final float numerator = 1650000;
        final float denominator = 1000 * 64 * 2048;
        final String[] parts = data.split(" ");
        final List<Float> data_microV = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            data_microV.add((Float.parseFloat(parts[i]) * numerator) / denominator);
        }
        float[] f_microV = new float[data_microV.size()];
        int i = 0;
        for (Float f : data_microV) {
            f_microV[i++] = (f != null ? f : Float.NaN); // Or whatever default you want
        }
        INDArray curr_data = Nd4j.create(f_microV);
        main_data = Nd4j.vstack(main_data, curr_data);
    }

    private void saveSession() {
        saveSession("Default");
    }

    private void saveSession(String tag) {
        String top_header = "Session ID,Session Tag,Date,Shape (rows x columns)," +
                "Duration (ms),Starting Time,Ending Time,Resolution (ms),Unit Measure";
        String dp_header = "S1, S2, S3, S4, S5, S6, S7, S8,";
        main_data = main_data.get(interval(1, main_data.rows()));  // Remove the first row (zeros)
        UUID id = UUID.randomUUID();
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        Character delimiter = ',';
        Character break_line = '\n';
        Float current;
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        try {
            File formatted = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS),
                    date.replace('/', '-') + '_' + id + ".csv");
            // if file doesn't exists, then create it
            if (!formatted.exists()) {
                formatted.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(formatted);
            String rows = String.valueOf(main_data.rows());  // Also INDArray.shape()[0]
            String cols = String.valueOf(main_data.columns());  // Also INDArray.shape()[1]
            fileWriter.append(top_header);
            fileWriter.append(break_line);
            fileWriter.append(id.toString());
            fileWriter.append(delimiter);
            fileWriter.append(tag);
            fileWriter.append(delimiter);
            fileWriter.append(date);
            fileWriter.append(delimiter);
            fileWriter.append(rows + " x " + cols);
            fileWriter.append(delimiter);
            fileWriter.append(recording_time);
            fileWriter.append(delimiter);
            fileWriter.append(start_time);
            fileWriter.append(delimiter);
            fileWriter.append(end_time);
            fileWriter.append(delimiter);
            String resolution = String.valueOf(Float.parseFloat(recording_time) /
                    Float.parseFloat(rows));
            fileWriter.append(resolution);
            fileWriter.append(delimiter);
            fileWriter.append("µV");
            fileWriter.append(delimiter);
            fileWriter.append(break_line);
            fileWriter.append(dp_header);
            fileWriter.append(break_line);
            for (int i = 0; i < main_data.rows(); i++) {
                for (int j = 0; j < main_data.columns(); j++) {
                    current = main_data.getFloat(i, j);
                    fileWriter.append(df.format(current));
                    fileWriter.append(delimiter);
                }
                fileWriter.append(break_line);
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            Log.e(TAG, String.format("Error storing the data into a CSV file: " + e));
        }
    }


    public void addEntry(float f) {
        lineEntries.add(new Entry(count, f));
        count = count + 1;
        LineDataSet set1 = createSet(lineEntries);
        //new LineDataSet(lineEntries,"legend");
        LineData data1 = new LineData(set1);
        data1.notifyDataChanged();
        mChart.setData(data1);
        mChart.notifyDataSetChanged();
        // limit the number of visible entries
        mChart.setVisibleXRangeMaximum(20);
        // move to the latest entry
        mChart.moveViewToX(data1.getEntryCount());

    }

    private LineDataSet createSet(ArrayList<Entry> le) {

        LineDataSet set1 = new LineDataSet(le, "S1");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(Color.rgb(255, 165, 0));  // Orange color
        set1.setCircleColor(Color.WHITE);
        set1.setLineWidth(1f);
        set1.setCircleRadius(1f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        //set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setValueTextColor(Color.WHITE);
        //set1.setValueTextSize(0.1f);
        //set1.setDrawValues(false);
        return set1;
    }

}
