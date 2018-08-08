package de.uni_osnabrueck.traumschreiber.somnium;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Fragment that handles the BLE search functionality and callbacks including the fragment UI
 */

public class ConnectionFragment extends Fragment {

    private static final long SCAN_DURATION = 20000;
    public static LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    /**
     * Calls back after new BLE devices have been found & adds them to the adapter
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    public ConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        // check if necessary permissions for Ble are grated
        ((MainActivity) getActivity()).getPermission();
        // in case of disconnect refresh/clear the BLE Device list to not shit legacy devices
        if (!MainActivity.mDeviceConnected) {
            mLeDeviceListAdapter.clear();
        }
    }

    /**
     * Triggers device scanning and stops it after a pre defined period
     *
     * @param enable or disable scanning
     */
    private void scanLeDevice(final boolean enable) {
        Activity activity = getActivity();
        if (enable) {
            // Stops scanning after the set duration
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    // display search search animation
                    Activity activity = getActivity();
                    if (activity != null) {
                        ((MainActivity) getActivity()).toggleProgressBar(0);
                        Toast.makeText(getActivity(), "Stopping scan", Toast.LENGTH_SHORT).show();
                    }


                }
            }, SCAN_DURATION);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            // hide search search animation


            if (activity != null) {
                ((MainActivity) getActivity()).toggleProgressBar(1);
                Toast.makeText(getContext(), "Starting scan", Toast.LENGTH_SHORT).show();
            }


        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        // Initializes the Bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // hook up UI elements and adapter for later inflation
        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.deviceList);
        listView.setAdapter(mLeDeviceListAdapter);

        /**
         * handles the on click event on an item(BLE) device inside the ListView (displayed devices)
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // get the device at from the clicked position
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;

                Toast.makeText(getContext(), "Connecting...", Toast.LENGTH_SHORT).show();

                // toss over relevant device information to main Activity;
                MainActivity.mDeviceAddress = device.getAddress();
                MainActivity.mDeviceName = device.getName();

                // connect to the selected device
                ((MainActivity) getActivity()).connect();

                // after connecting to a device stop the scanning
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
            }

        });

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // hook up the connection button and define on click behavior
        @SuppressWarnings("ConstantConditions") ImageButton imageButton = (ImageButton) getView().findViewById(R.id.bluetoothButton);


        if (imageButton != null) {
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLeDeviceListAdapter.clear();

                    // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
                    // prompt the user to do so else the app will not be able to discover BLE devices
                    if (!mBluetoothAdapter.isEnabled()) {
                        Toast.makeText(getContext(), "Enable bluetooth and try again", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // On Android 6.0 or higher Ensures Location is enabled on the device.
                    // If Location is not currently enabled prompt the user to do so else the app
                    // will not be able to discover BLE devices
                    if (!((MainActivity) getActivity()).checkLocationPermission()) {
                        Toast.makeText(getContext(), "Enable location and try again", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // if everything is in place/enabled start scanning for devices
                    if (!mScanning) {
                        scanLeDevice(true);
                    }

                }
            });
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);

        if (!MainActivity.mDeviceConnected) {
            mLeDeviceListAdapter.clear();
        }
    }

    // helper holding class
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    // Adapter for holding devices found through scanning.
    public class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = LayoutInflater.from(getContext());
        }

        /**
         * adds a found BLE device to the device list
         *
         * @param device BLE device that will be added
         */
        void addDevice(BluetoothDevice device) {

            // set UI text because a new BLE device has been found and added
            TextView textView = (TextView) getActivity().findViewById(R.id.bluetoothButtonTextView);
            textView.setText(R.string.select_your_traumschreiber);

            // make sure no dublicats
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // prepares and arranges the list view
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }

        // clear the UI up after disconnects or data changes
        void clear() {
            mLeDevices.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
        }
    }

}
