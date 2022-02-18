package com.example.temperaturesensorapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import androidx.lifecycle.ViewModel;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

//class created to store temperature data
class TempData{
    public Double tempC;
    public Double tempF;
    public String currentDateAndTime;

    //When new tempData is created, set the temperature in C and F and add the current time
    public TempData(Double tempC, Double tempF){
        this.tempC = tempC;
        this.tempF = tempF;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        currentDateAndTime = df.format(new Date());
    }
}

public class MainActivityViewModel extends ViewModel {

    private ArrayList<TempData> table;
    private Double tempC;
    private Double tempF;
    private boolean tooFar;
    private boolean tooClose;
    private Character unit;

    public MainActivityViewModel() {
        table = new ArrayList<>();
        unit = 'F';
        tooFar = false;
        tooClose = false;
        readings = "";
        result = false;
    }

    public Double getTempC(){
        return tempC;
    }
    public Double getTempF(){
        return tempF;
    }
    public boolean getTooFar(){
        return tooFar;
    }
    public boolean getTooClose(){
        return tooClose;
    }
    public Character getUnit(){
        return unit;
    }
    public int getTableLength(){
        return table.size();
    }

    public void setUnit(Character c){
        unit = c;
    }

    public ArrayList<String> getStringList() {
        ArrayList<String> recordList = new ArrayList<>();
        DecimalFormat df=new DecimalFormat("#.#");
        for(TempData data : table){
            recordList.add(data.currentDateAndTime + ":\n" + df.format(data.tempC) + "°C / " + df.format(data.tempF) + "°F");
        }
        return recordList;
    }


/* BLUETOOTH */
    //Bluetooth LE has very specific UUIDs for every service. These 4 are used to send and receive data
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //Member variables holding bluetooth variables needed to send and receive data
    private BluetoothGatt bluetoothGatt = null;
    private BluetoothDevice arduino;
    private BluetoothManager bluetoothManager;
    private String readings = "";
    private boolean result;

    //Result getter
    public boolean getResult(){return result;}

    //When anything characteristics in the bluetooth module changes, a callback is triggered
    //These are asynchronous functions
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        //When the bluetooth connect or disconnect, print out the current connection state
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                System.out.println("Connected");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                System.out.println("disconnected");
            }
        }

        //When services are discovered on this bluetooth module, enable the TX notifications(callback for receiving data)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.out.println("Service discovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bluetoothGatt = gatt;
                enableTXNotifications(gatt);
            }
        }

        //Receiving data changes the characteristics in the TX service
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            System.out.println("onCharacteristicChanged");
            byte[] data = characteristic.getValue();
            String s = new String(data, StandardCharsets.UTF_8);
            System.out.println(s);
            readings += s;
        }

    };

    //Pair with the bluetooth module, the address is hardcoded
    public void pairBluetooth(Context context){
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            String deviceAddress = "FD:CA:C8:DF:57:B3";
            arduino = bluetoothAdapter.getRemoteDevice(deviceAddress);
            System.out.println("Gatt started");
            bluetoothGatt = arduino.connectGatt(context, true, bluetoothGattCallback);
    }

    //Get temperature data from the module by sending the string "get"
    //This function will timeout in 3 seconds if no data is received
    public void getBleData(){
        if(bluetoothManager.getConnectionState(arduino, BluetoothProfile.GATT) != BluetoothProfile.STATE_CONNECTED){
            System.out.println("not connected");
            result = false;
            return;
        }
        String s = "get";
        writeRXCharacteristic(s.getBytes());

        long startTime = System.currentTimeMillis();
        //while loop that times out after 3 seconds
        while(readings.length() < 4 && (System.currentTimeMillis()-startTime)<3000){
            //do nothing until the full length is present
        }
        if((System.currentTimeMillis()-startTime)>=3000){
            //if timeout
            result = false;
            return;
        }
        if(readings.equals("too far")){
            tooFar = true;
        } else if (readings.equals("too close")){
            tooClose = true;
        } else {
            tooFar = false;
            tooClose = false;
            System.out.println(readings);
            tempF = Double.valueOf(readings);
            System.out.println(tempF);
            tempC = (tempF - 32) * 5 / 9;
            table.add(new TempData(tempC, tempF));
        }
        result = true;
        readings = "";
    }

    //Writing the value argument into the RX Characteristics of the module
    //By doing this, the information is sent to the bluetooth module from the phone
    public void writeRXCharacteristic(byte[] value)
    {
        BluetoothGattService RxService = bluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            return;
        }
        RxChar.setValue(value);
        boolean status = bluetoothGatt.writeCharacteristic(RxChar);
    }

    //Changing the setting in the TX Characteristic so that when the value in TX Characteristic
    //changes, it will notify the host(this phone)
    public void enableTXNotifications(BluetoothGatt gatt){
        BluetoothGattService RxService = gatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            return;
        }
        gatt.setCharacteristicNotification(TxChar,true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    //disconnect from bluetooth module
    public void endConnection(){
        bluetoothGatt.close();
    }
}
