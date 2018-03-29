//package de.uni_osnabrueck.traumschreiber.epilepsy.eegdroidgui;
//
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothProfile;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Binder;
//import android.os.IBinder;
//import android.support.v4.app.NotificationCompat;
//import android.util.Log;
//import android.widget.Toast;
//
//import java.util.List;
//import java.util.UUID;
//
//public class BLEConnectionService extends Service {
//    private final static String TAG = BLEConnectionService.class.getSimpleName();
//
//    public final static String ACTION_GATT_CONNECTED = "traumschreiber.ACTION_GATT_CONNECTED";
//    public final static String ACTION_GATT_DISCONNECTED = "traumschreiber.le.ACTION_GATT_DISCONNECTED";
//    public final static String ACTION_GATT_SERVICES_DISCOVERED = "traumschreiber.ACTION_GATT_SERVICES_DISCOVERED";
//    public final static String ACTION_DATA_AVAILABLE = "traumschreiber.ACTION_DATA_AVAILABLE";
//    public final static String EXTRA_DATA = "traumschreiber.le.EXTRA_DATA";
//
//    private static final int STATE_DISCONNECTED = 0;
//    private static final int STATE_CONNECTING = 1;
//    private static final int STATE_CONNECTED = 2;
//
//    public boolean notified;
//    private int mConnectionState = STATE_DISCONNECTED;
//
//    private BluetoothManager mBluetoothManager;
//    private BluetoothAdapter mBluetoothAdapter;
//    private String mBluetoothDeviceAddress;
//    private BluetoothGatt mBluetoothGatt;
//    private final IBinder mServiceBinder = new LocalBinder();
//
//
//    // Implements callback methods for GATT events that the app cares about.  For example,
//    // connection change and services discovered.
//    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            Log.d(TAG, "onConnectionStateChange: ConnectionStateChange on GattCallback");
//            String intentAction;
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                intentAction = ACTION_GATT_CONNECTED;
//                mConnectionState = STATE_CONNECTED;
//                broadcastUpdate(intentAction);
//                Log.i(TAG, "Connected to GATT server.");
//                // Attempts to discover services after successful connection.
//                Log.i(TAG, "Attempting to start service discovery:" +
//                        mBluetoothGatt.discoverServices());
//
//                // show notification in Toolbar
//                showNotification();
//
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                intentAction = ACTION_GATT_DISCONNECTED;
//                mConnectionState = STATE_DISCONNECTED;
//                Log.i(TAG, "Disconnected from GATT server.");
//                broadcastUpdate(intentAction);
//
//                // disable notification in Toolbar
//                killNotification();
//
//                //close the connection on disconnect
//                close();
//
//                MainActivity.mDeviceConnected = false;
//
//            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
//            } else {
//                Log.w(TAG, "onServicesDiscovered received: " + status);
//            }
//        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt,
//                                         BluetoothGattCharacteristic characteristic,
//                                         int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//            }
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt,
//                                            BluetoothGattCharacteristic characteristic) {
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//        }
//    };
//
//    private void showNotification() {
//
//        Toast.makeText(getBaseContext(), "This is the notification that is shown instead of doing weird stuff", Toast.LENGTH_SHORT);
//        Log.d(TAG, "showNotification: showNotification on the Console");
//
////        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
////
////        NotificationCompat.Builder mBuilder =
////                new NotificationCompat.Builder(this)
////                        .setSmallIcon(R.drawable.recordrec)
////                        .setLargeIcon(largeIcon)
////                        .setContentTitle("Somnium")
////                        .setContentText("Receiving data!")
////                        .setUsesChronometer(true)
////                        .setProgress(0, 0, true)
////                        .setCategory("CATEGORY_STATUS");
////
////
////        Intent returnIntent = new Intent(getApplicationContext(), MainActivity.class);
////        returnIntent.setAction(Intent.ACTION_MAIN);
////        returnIntent.addCategory(Intent.CATEGORY_LAUNCHER);
////        returnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////
////        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
////                returnIntent, PendingIntent.FLAG_UPDATE_CURRENT);
////
////        mBuilder.setContentIntent(contentIntent);
////
////
////        NotificationManager mNotificationManager =
////                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
////        // start
////        mNotificationManager.notify(8924, mBuilder.build());
////        notified = true;
//    }
//
//    private void broadcastUpdate(final String action) {
//        final Intent intent = new Intent(action);
//        sendBroadcast(intent);
//    }
//
//    private void broadcastUpdate(final String action,
//                                 final BluetoothGattCharacteristic characteristic) {
//        final Intent intent = new Intent(action);
//
//
//        /*******
//        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
//        // carried out as per profile specifications:
//        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            int flag = characteristic.getProperties();
//            int format = -1;
//            if ((flag & 0x01) != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                Log.d(TAG, "Heart rate format UINT16.");
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                Log.d(TAG, "Heart rate format UINT8.");
//            }
//            final int heartRate = characteristic.getIntValue(format, 1);
//            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//        } else {
//            // For all other profiles, writes the data formatted in HEX.
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            }
//        }
//        *******/
//
//        // Only else branch, because Heart rates do not matter to us
//        // For all profiles, writes the data formatted in HEX.
//        final byte[] data = characteristic.getValue();
//        if (data != null && data.length > 0) {
//            final StringBuilder stringBuilder = new StringBuilder(data.length);
//            for (byte byteChar : data)
//                stringBuilder.append(String.format("%02X ", byteChar));
//            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//        }
//        sendBroadcast(intent);
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mServiceBinder;
//    }
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        // After using a given device, you should make sure that BluetoothGatt.close() is called
//        // such that resources are cleaned up properly.  In this particular example, close() is
//        // invoked when the UI is disconnected from the Service.
//        close();
//        return super.onUnbind(intent);
//    }
//
//    /**
//     * Initializes a reference to the local Bluetooth adapter.
//     *
//     * @return Return true if the initialization is successful.
//     */
//    public boolean initialize() {
//        // For API level 18 and above, get a reference to BluetoothAdapter through
//        // BluetoothManager.
//        if (mBluetoothManager == null) {
//            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//            if (mBluetoothManager == null) {
//                Log.e(TAG, "Unable to initialize BluetoothManager.");
//                return false;
//            }
//        }
//
//        mBluetoothAdapter = mBluetoothManager.getAdapter();
//        if (mBluetoothAdapter == null) {
//            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * Connects to the GATT server hosted on the Bluetooth LE device.
//     *
//     * @param address The device address of the destination device.
//     * @return Return true if the connection is initiated successfully. The connection result
//     * is reported asynchronously through the
//     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
//     * callback.
//     */
//    public boolean connect(final String address) {
//        if (mBluetoothAdapter == null || address == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
//            return false;
//        }
//
//        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                mConnectionState = STATE_CONNECTING;
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        if (device == null) {
//            Log.w(TAG, "Device not found.  Unable to connect.");
//            return false;
//        }
//        // We want to directly connect to the device, so we are setting the autoConnect
//        // parameter to false.
//        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
//        Log.d(TAG, "Trying to create a new connection.");
//        mBluetoothDeviceAddress = address;
//        mConnectionState = STATE_CONNECTING;
//        return true;
//    }
//
//    /**
//     * Disconnects an existing connection or cancel a pending connection. The disconnection result
//     * is reported asynchronously through the
//     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
//     * callback.
//     */
//    public void disconnect() {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.disconnect();
//    }
//
//    /**
//     * After using a given BLE device, the app must call this method to ensure resources are
//     * released properly.
//     */
//    public void close() {
//        if (mBluetoothGatt == null) {
//            killNotification();
//            return;
//        }
//        // disable notification in Toolbar
//        killNotification();
//        mBluetoothGatt.close();
//        mBluetoothGatt = null;
//    }
//
//    /**
//     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
//     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
//     * callback.
//     *
//     * @param characteristic The characteristic to read from.
//     */
//    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.readCharacteristic(characteristic);
//    }
//
//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param characteristic Characteristic to act on.
//     * @param enabled        If true, enable notification.  False otherwise.
//     */
//    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
//                                              boolean enabled) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//
//        // If specific Device (HEART_RATE) is found write descriptors
////        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
////            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
////                    UUID.fromString(DeviceGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
////            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
////            mBluetoothGatt.writeDescriptor(descriptor);
////        }
//    }
//
//    /**
//     * Retrieves a list of supported GATT services on the connected device. This should be
//     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
//     *
//     * @return A {@code List} of supported services.
//     */
//    public List<BluetoothGattService> getSupportedGattServices() {
//        if (mBluetoothGatt == null) return null;
//
//        return mBluetoothGatt.getServices();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        close();
//    }
//
//    /**
//     * Disables the Notification shown outside of the app
//     */
//    public void killNotification() {
//
//        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (notified) {
//            nm.cancel(8924);
//            notified = false;
//        }
//    }
//
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
//        killNotification();
//        this.stopSelf();
//    }
//
//    public class LocalBinder extends Binder {
//        BLEConnectionService getService() {
//            return BLEConnectionService.this;
//        }
//    }
//}
