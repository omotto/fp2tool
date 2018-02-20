package com.example.fp2tool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class MainActivity extends Activity {

    // -- Constants
    private static final int SET_ACTIVE_CMD         = 1;
    private static final int SET_STANDBY_CMD        = 2;
    private static final int SET_DEBUG_CMD          = 3;
    private static final int GET_FIRMWARE_CMD       = 4;
    private static final int GET_STATUS_CMD         = 5;
    private static final int SET_THRESHOLD_CMD      = 6;
    private static final int GET_THRESHOLD_CMD      = 7;
    private static final int GET_DEBUGINFO_CMD      = 8;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT      = 2;
    private static final int REQUEST_CHANNEL_VALUES = 3;

    public static final String  EXTRA_MESSAGE       = "results";

    // -- Attributes
    RFIDReader rfid = new RFIDReader();
    boolean readEvent;

    private BluetoothConnection BTConnection = null;
    private BluetoothAdapter mBluetoothAdapter = null;

    private Button activeButton, standbyButton, debugButton, stastusButton, thresholdsButton, getThresholdsButton, versionButton, debugInfoButton;

    private TextView mTitle, outText;
    private String mConnectedDeviceName = null;

    private int [] iThresholds = new int [18]; // 18 parameters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            // default values
            iThresholds[0]  = 4000;
            iThresholds[1]  = 100;
            iThresholds[2]  = 35;
            iThresholds[3]  = 35;
            iThresholds[4]  = 35;
            iThresholds[5]  = 65;
            iThresholds[6]  =  3;
            iThresholds[7]  = 70;
            iThresholds[8]  = 70;
            iThresholds[9]  = 70;
            iThresholds[10] = 30;
            iThresholds[11] = 20;
            iThresholds[12] = 50;
            iThresholds[13] =  4;
            iThresholds[14] = 24;
            iThresholds[15] = 72;
            iThresholds[16] =  0;
            iThresholds[17] = 30;

            mTitle = (TextView) findViewById(R.id.text);
            outText = (TextView) findViewById(R.id.outMessage);

            final RFIDReader.MemoryAddress HW_VERSION = new RFIDReader.MemoryAddress();
            HW_VERSION.bank                 = RFIDReader.BANK_USER;
            HW_VERSION.offset               = 0;
            HW_VERSION.size                 = 2;

            final RFIDReader.MemoryAddress FW_VERSION = new RFIDReader.MemoryAddress();
            FW_VERSION.bank                 = RFIDReader.BANK_USER;
            FW_VERSION.offset               = 64;
            FW_VERSION.size                 = 7;

            final RFIDReader.MemoryAddress HEALTH_ADDR = new RFIDReader.MemoryAddress();
            HEALTH_ADDR.bank                = RFIDReader.BANK_USER;
            HEALTH_ADDR.offset              = 120;
            HEALTH_ADDR.size                = 7;

            final RFIDReader.MemoryAddress PCA_FUNCTIONAL_TEST_ADDR = new RFIDReader.MemoryAddress();
            PCA_FUNCTIONAL_TEST_ADDR.bank   = RFIDReader.BANK_USER;
            PCA_FUNCTIONAL_TEST_ADDR.offset = 2;
            PCA_FUNCTIONAL_TEST_ADDR.size   = 4;

            final RFIDReader.MemoryAddress STATUS_OF_THE_UNIT = new RFIDReader.MemoryAddress();
            STATUS_OF_THE_UNIT.bank         = RFIDReader.BANK_USER;
            STATUS_OF_THE_UNIT.offset       = 90;
            STATUS_OF_THE_UNIT.size         = 4;

            final RFIDReader.MemoryAddress EPC_DOWNLOAD_REGISTER = new RFIDReader.MemoryAddress();
            EPC_DOWNLOAD_REGISTER.bank      = RFIDReader.BANK_EPC;
            EPC_DOWNLOAD_REGISTER.offset    = 31;
            EPC_DOWNLOAD_REGISTER.size      = 1;

            final RFIDReader.MemoryAddress CRTL_MSG_RFtoI2C = new RFIDReader.MemoryAddress();
            CRTL_MSG_RFtoI2C.bank           = RFIDReader.BANK_USER;
            CRTL_MSG_RFtoI2C.offset         = 128;
            CRTL_MSG_RFtoI2C.size           = 0;

            final RFIDReader.MemoryAddress CRTL_MSG_I2CtoRF = new RFIDReader.MemoryAddress();
            CRTL_MSG_I2CtoRF.bank           = RFIDReader.BANK_USER;
            CRTL_MSG_I2CtoRF.offset         = 159;
            CRTL_MSG_I2CtoRF.size           = 0;

            final RFIDReader.MemoryAddress EOL_FINAL_TEST_PASSED = new RFIDReader.MemoryAddress();
            EOL_FINAL_TEST_PASSED.bank      = RFIDReader.BANK_USER;
            EOL_FINAL_TEST_PASSED.offset    = 6;
            EOL_FINAL_TEST_PASSED.size      = 4;

            final RFIDReader.MemoryAddress THRESHOLDS = new RFIDReader.MemoryAddress();
            THRESHOLDS.bank                 = RFIDReader.BANK_USER;
            THRESHOLDS.offset               = 159;
            THRESHOLDS.size                 = 13;

            final String FP2_SET_ACTIVE_COMMAND     = "0001000103306300";
            final String FP2_SET_STANDBY_COMMAND    = "0001000102204200";
            //final String FP2_SET_THRESHOLD_COMMAND1 = "00010014800FA05023232341034646461E1432041848001E7EA7";
            //final String FP2_SET_THRESHOLD_COMMAND2 = "00010014800FA05023232341032323231E1432041848001E1F88";
            final String FP2_GET_THRESHOLD_COMMAND  = "0001000208801821";
            final String FP2_SET_DEBUG_COMMAND      = "00010002070189B6";
            final String FP2_GET_DEBUG_INFO_COMMAND = "000100010AA14A00";


            final String SENSOR_PASSWORD = "00000000";

/*
            byte [] byteCode = rfid.hexStringToByteArray("800fa05023232341032323231e1432041848001e");
            Log.d("crc", rfid.crc16_ccitt(byteCode));
*/

            rfid.setOpMode(true, false, 0, false);

            activeButton = (Button)findViewById(R.id.button_setActive);
            activeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
                        outText.setText("");
                        mThread sendThread = new mThread(rfid.sendWriteTag(1, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD, "0000"),
                                                         rfid.sendWriteTag(4, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_SET_ACTIVE_COMMAND),
                                                         rfid.sendReadTag(EPC_DOWNLOAD_REGISTER.size, EPC_DOWNLOAD_REGISTER.bank, EPC_DOWNLOAD_REGISTER.offset, SENSOR_PASSWORD),
                                                         rfid.sendReadTag(7, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD),
                                                         SET_ACTIVE_CMD);
                        sendThread.start();
                        // --
                        Log.d("activeButton", "Send Data: "  + new String(rfid.sendWriteTag(4, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_SET_ACTIVE_COMMAND)));
                    }
                }
            });

            standbyButton = (Button)findViewById(R.id.button_setStandBy);
            standbyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
                        outText.setText("");
                        mThread sendThread = new mThread(rfid.sendWriteTag(1, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD, "0000"),
                                                         rfid.sendWriteTag(4, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_SET_STANDBY_COMMAND),
                                                         rfid.sendReadTag(EPC_DOWNLOAD_REGISTER.size, EPC_DOWNLOAD_REGISTER.bank, EPC_DOWNLOAD_REGISTER.offset, SENSOR_PASSWORD),
                                                         rfid.sendReadTag(7, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD),
                                                         SET_STANDBY_CMD);
                        sendThread.start();
                        // --
                        Log.d("standbyButton", "Send Data: "  + new String(rfid.sendWriteTag(4, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_SET_STANDBY_COMMAND)));
                    }
                }
            });

            debugButton = (Button)findViewById(R.id.button_setDebug);
            debugButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
                        outText.setText("");
                        mThread sendThread = new mThread(rfid.sendWriteTag(1, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD, "0000"),
                                                         rfid.sendWriteTag(4, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_SET_DEBUG_COMMAND),
                                                         rfid.sendReadTag(EPC_DOWNLOAD_REGISTER.size, EPC_DOWNLOAD_REGISTER.bank, EPC_DOWNLOAD_REGISTER.offset, SENSOR_PASSWORD),
                                                         rfid.sendReadTag(7, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD),
                                                         SET_DEBUG_CMD);
                        sendThread.start();
                        // --
                        Log.d("debugButton", "Send Data: "  + new String(rfid.sendWriteTag(4, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_SET_ACTIVE_COMMAND)));
                    }
                }
            });

            versionButton = (Button)findViewById(R.id.button_getFWVersion);
            versionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
                        outText.setText("");
                        mThread sendThread = new mThread(rfid.sendReadTag(FW_VERSION.size, FW_VERSION.bank, FW_VERSION.offset, SENSOR_PASSWORD), GET_FIRMWARE_CMD);
                        sendThread.start();
                        // --
                        Log.d("versionButton", "Send Data: "  + new String(rfid.sendReadTag(FW_VERSION.size, FW_VERSION.bank, FW_VERSION.offset, SENSOR_PASSWORD)));
                    }
                }
            });

            stastusButton = (Button)findViewById(R.id.button_getStatus);
            stastusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
                        outText.setText("");
                        mThread sendThread = new mThread(rfid.sendReadTag(STATUS_OF_THE_UNIT.size, STATUS_OF_THE_UNIT.bank, STATUS_OF_THE_UNIT.offset, SENSOR_PASSWORD), GET_STATUS_CMD);
                        sendThread.start();
                        // --
                        Log.d("stastusButton", "Send Data: "  + new String(rfid.sendReadTag(STATUS_OF_THE_UNIT.size, STATUS_OF_THE_UNIT.bank, STATUS_OF_THE_UNIT.offset, SENSOR_PASSWORD)));
                    }
                }
            });

            thresholdsButton = (Button)findViewById(R.id.button_setThresholds);
            thresholdsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
                        // Set command values
                        String FP2_SET_THRESHOLD_COMMAND = "80";
                        FP2_SET_THRESHOLD_COMMAND += String.format("%04X", iThresholds[0]  & 0xffff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[1]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[2]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[3]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[4]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[5]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[6]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[7]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[8]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[9]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[10]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[11]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[12]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[13]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[14]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[15]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[16]  & 0xff);
                        FP2_SET_THRESHOLD_COMMAND += String.format("%02X", iThresholds[17]  & 0xff);
                        // Set CRC
                        byte [] byteCode = rfid.hexStringToByteArray(FP2_SET_THRESHOLD_COMMAND);
                        FP2_SET_THRESHOLD_COMMAND += rfid.crc16_ccitt(byteCode);
                        // Set Header
                        FP2_SET_THRESHOLD_COMMAND = "0001" + "0014" + FP2_SET_THRESHOLD_COMMAND;
                        outText.setText("");
                        mThread sendThread = new mThread(rfid.sendWriteTag(1, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD, "0000"),
                                                         rfid.sendWriteTag(13, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_SET_THRESHOLD_COMMAND),
                                                         rfid.sendReadTag(EPC_DOWNLOAD_REGISTER.size, EPC_DOWNLOAD_REGISTER.bank, EPC_DOWNLOAD_REGISTER.offset, SENSOR_PASSWORD),
                                                         rfid.sendReadTag(7, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD),
                                                         SET_THRESHOLD_CMD);
                        sendThread.start();
                        // --
                        Log.d("thresholdsButton", "Send Data: "  + new String(rfid.sendWriteTag(13, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_SET_THRESHOLD_COMMAND)));
                    }
                }
            });

            getThresholdsButton = (Button)findViewById(R.id.button_getThresholds);
            getThresholdsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
                        outText.setText("");
                        mThread sendThread = new mThread(rfid.sendWriteTag(1, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD, "0000"),
                                                         rfid.sendWriteTag(4, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_GET_THRESHOLD_COMMAND),
                                                         rfid.sendReadTag(EPC_DOWNLOAD_REGISTER.size, EPC_DOWNLOAD_REGISTER.bank, EPC_DOWNLOAD_REGISTER.offset, SENSOR_PASSWORD),
                                                         rfid.sendReadTag(14, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD),
                                                         GET_THRESHOLD_CMD);
                        sendThread.start();
                        // --
                        Log.d("getThresholdsButton", "Send Data: "  + new String(rfid.sendWriteTag(4, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_GET_THRESHOLD_COMMAND)));
                    }
                }
            });

            debugInfoButton = (Button)findViewById(R.id.button_getDebugInfo);
            debugInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((BTConnection != null) && (BTConnection.getState() == BTConnection.STATE_CONNECTED)) {
                        outText.setText("");
                        mThread sendThread = new mThread(rfid.sendWriteTag(1, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD, "0000"),
                                                         rfid.sendWriteTag(4, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_GET_DEBUG_INFO_COMMAND),
                                                         rfid.sendReadTag(EPC_DOWNLOAD_REGISTER.size, EPC_DOWNLOAD_REGISTER.bank, EPC_DOWNLOAD_REGISTER.offset, SENSOR_PASSWORD),
                                                         rfid.sendReadTag(17, CRTL_MSG_I2CtoRF.bank, CRTL_MSG_I2CtoRF.offset, SENSOR_PASSWORD),
                                                         GET_DEBUGINFO_CMD);
                        sendThread.start();
                        // --
                        Log.d("debugInfoButton", "Send Data: "  + new String(rfid.sendWriteTag(4, CRTL_MSG_RFtoI2C.bank, CRTL_MSG_RFtoI2C.offset, SENSOR_PASSWORD, FP2_GET_THRESHOLD_COMMAND)));
                    }
                }
            });
        }
    }

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            String readMessage = (String)msg.obj;
            Intent mIntent = new Intent(getApplicationContext(), ResultListActivity.class);

            mIntent.putExtra(MainActivity.EXTRA_MESSAGE, readMessage);
            startActivity(mIntent);
            /*
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result")
                .setMessage(readMessage)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Nothing to do here!
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
            */
        }
    };

    private class mThread extends Thread{
        private byte [] message1, message2, message3, message4;
        private int timeOut;
        private int commandCode;
        private int errorCode;
        private String EPC;

        public mThread(byte [] message1, int commnadCode) {
            this.EPC      = null;
            this.message1 = message1;
            this.message2 = null;
            this.message3 = null;
            this.message4 = null;
            this.commandCode = commnadCode;
        }

        public mThread(byte [] message1, byte [] message2, byte [] message3, byte [] message4, int commnadCode) {
            this.EPC      = null;
            this.message1 = message1;
            this.message2 = message2;
            this.message3 = message3;
            this.message4 = message4;
            this.commandCode = commnadCode;
        }
        @Override
        public void run() {
            String retMessage = null;
            if (message1 != null) {
                timeOut = 3;
                readEvent = false;
                BTConnection.write(message1);
                while ((readEvent == false) && (timeOut > 0)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timeOut--;
                }
                Log.d("mThread", "received message");
                if (readEvent != false) {
                    if ((commandCode == GET_FIRMWARE_CMD) || (commandCode == GET_STATUS_CMD))
                        errorCode = rfid.checkMessage(null, true);
                    else
                        errorCode = rfid.checkMessage(null, false);
                    if (errorCode == 0) {
                        if ((commandCode == GET_FIRMWARE_CMD) || (commandCode == GET_STATUS_CMD))
                            retMessage = rfid.parseMessageDataAs(commandCode);
                        else
                            EPC = rfid.getMessageEPC();
                    } else {
                        Log.d("mThread", "Error " + Integer.toString(errorCode));
                    }
                } else  {
                    errorCode = 0xff;
                    Log.d("mThread", "Error " + Integer.toString(errorCode));
                }
            }
            if ((message2 != null) && (errorCode == 0)) {
                timeOut = 3;
                readEvent = false;
                BTConnection.write(message2);
                while ((readEvent == false) && (timeOut > 0)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timeOut--;
                }
                Log.d("mThread", "received message");
                if (readEvent != false) {
                    errorCode = rfid.checkMessage(EPC, false);
                    if (errorCode != 0)
                        Log.d("mThread", "Error " + Integer.toString(errorCode));
                } else  {
                    errorCode = 0xff;
                    Log.d("mThread", "Error " + Integer.toString(errorCode));
                }
            }
            if ((message3 != null) && (errorCode == 0)) {
                timeOut = 3;
                readEvent = false;
                BTConnection.write(message3);
                while ((readEvent == false) && (timeOut > 0)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timeOut--;
                }
                Log.d("mThread", "received message");
                if (readEvent != false) {
                    errorCode = rfid.checkMessage(EPC, true);
                    if (errorCode != 0)
                        Log.d("mThread", "Error " + Integer.toString(errorCode));
                } else  {
                    errorCode = 0xff;
                    Log.d("mThread", "Error " + Integer.toString(errorCode));
                }
            }
            if ((message4 != null) && (errorCode == 0)) {
                timeOut = 3;
                readEvent = false;
                BTConnection.write(message4);
                while ((readEvent == false) && (timeOut > 0)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timeOut--;
                }
                Log.d("mThread", "received message");
                if (readEvent != false) {
                    errorCode = rfid.checkMessage(EPC, true);
                    if (errorCode != 0)
                        Log.d("mThread", "Error " + Integer.toString(errorCode));
                    else
                        retMessage = rfid.parseMessageDataAs(commandCode);
                } else  {
                    errorCode = 0xff;
                    Log.d("mThread", "Error " + Integer.toString(errorCode));
                }
            }
            // ---
            if (errorCode != 0)
                myHandler.obtainMessage(0x00, ("Error: " + Integer.toString(errorCode)).length(), -1, "Error: " + Integer.toString(errorCode)).sendToTarget();
            else
                myHandler.obtainMessage(0x00, retMessage.length(), -1, retMessage).sendToTarget();
            // ---
            if (errorCode == 0)  Log.d("mThread", retMessage);
        }
    }

    // The Handler that gets information back from the BluetoothConnection
    private final Handler mHandler = new Handler() {
       @Override
       public void handleMessage(Message msg) {
           switch (msg.what) {
           case BluetoothConnection.MESSAGE_STATE_CHANGE:
               Log.i("handler", "MESSAGE_STATE_CHANGE: " + msg.arg1);
               switch (msg.arg1) {
               case BluetoothConnection.STATE_CONNECTED:
                   mTitle.setText(R.string.title_connected_to);
                   mTitle.append(" ");
                   mTitle.append(mConnectedDeviceName);
                   // --- REVIEW
                   BTConnection.write(rfid.sendCmdOpenInterface1());
                   // ---
                   //mConversationArrayAdapter.clear();
                   break;
               case BluetoothConnection.STATE_CONNECTING:
                   mTitle.setText(R.string.title_connecting);
                   break;
               case BluetoothConnection.STATE_LISTEN:
               case BluetoothConnection.STATE_NONE:
                   mTitle.setText(R.string.title_not_connected);
                   break;
               }
               break;
           case BluetoothConnection.MESSAGE_WRITE:
               // byte[] writeBuf = (byte[]) msg.obj;
               // construct a string from the buffer
               // String writeMessage = new String(writeBuf);
               // Toast.makeText(getApplicationContext(),writeMessage, Toast.LENGTH_SHORT).show();
               // mConversationArrayAdapter.add("Me:  " + writeMessage);
               break;
           case BluetoothConnection.MESSAGE_READ:
               byte[] readBuf = (byte[]) msg.obj;
               // construct a string from the valid bytes in the buffer
               String readMessage = new String(readBuf, 0, msg.arg1);
               String message = rfid.getMessage(readMessage);
               if ((message != "") && (!message.equals("$>"))) {
                   rfid.pushMessage(message);
                   Log.d("RX", message);
                   outText.append(message);
                   readEvent = true;
               }
               // ----------------------------------------------------------------------------------- TO TEST
               //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
               break;
           case BluetoothConnection.MESSAGE_DEVICE_NAME:
               // save the connected device's name
               mConnectedDeviceName = msg.getData().getString(BluetoothConnection.DEVICE_NAME);
               Toast.makeText(getApplicationContext(), "Connected to "+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
               break;
           case BluetoothConnection.MESSAGE_TOAST:
               Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothConnection.TOAST), Toast.LENGTH_SHORT).show();
               break;
           }
       }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHANNEL_VALUES:
                if (resultCode == Activity.RESULT_OK) {
                    int [] values = data.getExtras().getIntArray(FileListActivity.THRESHOLDS);
                    for (int c = 0; c < 18; c++)
                        if (c == 0)
                            if (values[c] > 4095) iThresholds[c] = 4095; else iThresholds[c] = values[c];
                        else
                            if (values[c] > 255) iThresholds[c] = 255; else iThresholds[c] = values[c];
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    Log.d("connect","connect to: " + device);
                    //--
                    BTConnection = new BluetoothConnection(this, mHandler);
                    //--
                    BTConnection.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    BTConnection = new BluetoothConnection(this, mHandler);
                    //setupConnection();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BTConnection != null) {
            BTConnection.write(rfid.sendCmdCloseInterface1());
            BTConnection.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.main, menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder my_builder;
        View messageView;
        switch (item.getItemId()) {
           case R.id.scan:
               // Launch the DeviceListActivity to see devices and do scan
               Intent serverIntent = new Intent(this, DeviceListActivity.class);
               startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
               return true;
           case R.id.connect:
               if (!mBluetoothAdapter.isEnabled()) {
                   Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                   startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
                   Toast.makeText(getApplicationContext(),"Bluetooth turned on", Toast.LENGTH_LONG).show();
               } else {
                    // --- REVIEW
                   BTConnection.write(rfid.sendCmdCloseInterface1());
                    // ---
                   mBluetoothAdapter.disable();
                   mTitle.setText(R.string.title_not_connected);
                   Toast.makeText(getApplicationContext(),"Bluetooth turned off", Toast.LENGTH_LONG).show();
               }
               return true;

           case R.id.load:
               // Launch the DeviceListActivity to see devices and do scan
               Intent loadIntent = new Intent(this, FileListActivity.class);
               startActivityForResult(loadIntent, REQUEST_CHANNEL_VALUES);
               return true;
           case R.id.save:
               // Create dialog to save file
               final Activity activity = (Activity) this;
               AlertDialog.Builder builder = new AlertDialog.Builder(activity);
               // Get the layout inflater
               LayoutInflater dialog_inflater = activity.getLayoutInflater();
               // Inflate and set the layout for the dialog
               // Pass null as the parent view because its going in the dialog layout
               builder.setTitle("Set File Name");
               builder.setView(dialog_inflater.inflate(R.layout.dialog_layout, null))
               // Add action buttons
                   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int id) {
                            Dialog f = (Dialog) dialog;
                            EditText filename = (EditText) f.findViewById(R.id.filename);
                            File currentDir = new File("/sdcard/");
                            JSONObject obj = new JSONObject(); // Save data in JSON format
                            try {
                                obj.put("Absolute Threshold", iThresholds[0]);
                                obj.put("IR Threshold", iThresholds[1]);
                                obj.put("Magnetic perturbation th X", iThresholds[2]);
                                obj.put("Magnetic perturbation th Y", iThresholds[3]);
                                obj.put("Magnetic perturbation th Z", iThresholds[4]);
                                obj.put("Magnetic update max shift", iThresholds[5]);
                                obj.put("Magnetic hysteresis", iThresholds[6]);
                                obj.put("Magnetic absolute th X", iThresholds[7]);
                                obj.put("Magnetic absolute th Y", iThresholds[8]);
                                obj.put("Magnetic absolute th Z", iThresholds[9]);
                                obj.put("Stable time", iThresholds[10]);
                                obj.put("Magnetic value table life", iThresholds[11]);
                                obj.put("Ir variation check", iThresholds[12]);
                                obj.put("Ir linearity correction", iThresholds[13]);
                                obj.put("Optical free timeout", iThresholds[14]);
                                obj.put("Optical busy timeout", iThresholds[15]);
                                obj.put("Enable saturated magnetic update", iThresholds[16]);
                                obj.put("Enable ir noise check mag update", iThresholds[17]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // Save file
                            File file;
                            Writer output;
                            try {
                                file = new File(currentDir,filename.getText().toString()+".json");
                                output = new BufferedWriter(new FileWriter(file));
                                output.write(obj.toString());
                                output.close();
                                Log.d("Save File",currentDir+filename.getText().toString()+".json");
                                Log.d("Save File",obj.toString());
                                Toast.makeText(getApplicationContext(), "Thresholds saved", Toast.LENGTH_LONG).show();
                              } catch (Exception e) {
                                 Log.d("Save File","ERROR: "+e);
                                 e.printStackTrace();
                              }
                       }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Exit
                        }
                    });
                 builder.create().show();
               return true;

           case R.id.thresholds:
               messageView = getLayoutInflater().inflate(R.layout.thresholds, null, false);
               // -- Range Filters of EditTexts
               final EditText Absolute_threshold = (EditText) messageView.findViewById(R.id.editTextAbsolute_threshold);
               Absolute_threshold.setFilters(new InputFilter[]{new InputFilterMinMax("0", "4095")});
               Absolute_threshold.setText(Integer.toString(iThresholds[0]));
               final EditText Ir_threshold = (EditText) messageView.findViewById(R.id.editTextIr_threshold);
               Ir_threshold.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Ir_threshold.setText(Integer.toString(iThresholds[1]));
               final EditText Magnetic_perturbation_th_x = (EditText) messageView.findViewById(R.id.editTextMagnetic_perturbation_th_x);
               Magnetic_perturbation_th_x.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Magnetic_perturbation_th_x.setText(Integer.toString(iThresholds[2]));
               final EditText Magnetic_perturbation_th_y = (EditText) messageView.findViewById(R.id.editTextMagnetic_perturbation_th_y);
               Magnetic_perturbation_th_y.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Magnetic_perturbation_th_y.setText(Integer.toString(iThresholds[3]));
               final EditText Magnetic_perturbation_th_z = (EditText) messageView.findViewById(R.id.editTextMagnetic_perturbation_th_z);
               Magnetic_perturbation_th_z.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Magnetic_perturbation_th_z.setText(Integer.toString(iThresholds[4]));
               final EditText Magnetic_update_max_shift = (EditText) messageView.findViewById(R.id.editTextMagnetic_update_max_shift);
               Magnetic_update_max_shift.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Magnetic_update_max_shift.setText(Integer.toString(iThresholds[5]));
               final EditText ViewMagnetic_hysteresis = (EditText) messageView.findViewById(R.id.editTextMagnetic_hysteresis);
               ViewMagnetic_hysteresis.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               ViewMagnetic_hysteresis.setText(Integer.toString(iThresholds[6]));
               final EditText Magnetic_absolute_th_x = (EditText) messageView.findViewById(R.id.editTextMagnetic_absolute_th_x);
               Magnetic_absolute_th_x.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Magnetic_absolute_th_x.setText(Integer.toString(iThresholds[7]));
               final EditText Magnetic_absolute_th_y = (EditText) messageView.findViewById(R.id.editTextMagnetic_absolute_th_y);
               Magnetic_absolute_th_y.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Magnetic_absolute_th_y.setText(Integer.toString(iThresholds[8]));
               final EditText Magnetic_absolute_th_z = (EditText) messageView.findViewById(R.id.editTextMagnetic_absolute_th_z);
               Magnetic_absolute_th_z.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Magnetic_absolute_th_z.setText(Integer.toString(iThresholds[9]));
               final EditText Stable_time = (EditText) messageView.findViewById(R.id.editTextStable_time);
               Stable_time.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Stable_time.setText(Integer.toString(iThresholds[10]));
               final EditText Magnetic_value_table_life = (EditText) messageView.findViewById(R.id.editTextMagnetic_value_table_life);
               Magnetic_value_table_life.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Magnetic_value_table_life.setText(Integer.toString(iThresholds[11]));
               final EditText Ir_variation_check = (EditText) messageView.findViewById(R.id.editTextIr_variation_check);
               Ir_variation_check.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Ir_variation_check.setText(Integer.toString(iThresholds[12]));
               final EditText Ir_linearity_correction = (EditText) messageView.findViewById(R.id.editTextIr_linearity_correction);
               Ir_linearity_correction.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Ir_linearity_correction.setText(Integer.toString(iThresholds[13]));
               final EditText Optical_free_timeout = (EditText) messageView.findViewById(R.id.editTextOptical_free_timeout);
               Optical_free_timeout.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Optical_free_timeout.setText(Integer.toString(iThresholds[14]));
               final EditText Optical_busy_timeout = (EditText) messageView.findViewById(R.id.editTextOptical_busy_timeout);
               Optical_busy_timeout.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Optical_busy_timeout.setText(Integer.toString(iThresholds[15]));
               final EditText Enable_saturated_magnetic_update = (EditText) messageView.findViewById(R.id.editTextEnable_saturated_magnetic_update);
               Enable_saturated_magnetic_update.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Enable_saturated_magnetic_update.setText(Integer.toString(iThresholds[16]));
               final EditText Enable_ir_noise_check_mag_update = (EditText) messageView.findViewById(R.id.editTextEnable_ir_noise_check_mag_update);
               Enable_ir_noise_check_mag_update.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
               Enable_ir_noise_check_mag_update.setText(Integer.toString(iThresholds[17]));
               // --
               my_builder = new AlertDialog.Builder(this);
               my_builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       Log.d("Set Threshold Button", "Set Data");
                       if (Absolute_threshold.getText().toString().isEmpty())                  iThresholds[0]  = 0;
                       else iThresholds[0]  = Integer.parseInt(Absolute_threshold.getText().toString());
                       if (Ir_threshold.getText().toString().isEmpty())                        iThresholds[1]  = 0;
                       else iThresholds[1]  = Integer.parseInt(Ir_threshold.getText().toString());
                       if (Magnetic_perturbation_th_x.getText().toString().isEmpty())          iThresholds[2]  = 0;
                       else iThresholds[2]  = Integer.parseInt(Magnetic_perturbation_th_x.getText().toString());
                       if (Magnetic_perturbation_th_y.getText().toString().isEmpty())          iThresholds[3]  = 0;
                       else iThresholds[3]  = Integer.parseInt(Magnetic_perturbation_th_y.getText().toString());
                       if (Magnetic_perturbation_th_z.getText().toString().isEmpty())          iThresholds[4]  = 0;
                       else iThresholds[4]  = Integer.parseInt(Magnetic_perturbation_th_z.getText().toString());
                       if (Magnetic_update_max_shift.getText().toString().isEmpty())           iThresholds[5]  = 0;
                       else iThresholds[5]  = Integer.parseInt(Magnetic_update_max_shift.getText().toString());
                       if (ViewMagnetic_hysteresis.getText().toString().isEmpty())             iThresholds[6]  = 0;
                       else iThresholds[6]  = Integer.parseInt(ViewMagnetic_hysteresis.getText().toString());
                       if (Magnetic_absolute_th_x.getText().toString().isEmpty())              iThresholds[7]  = 0;
                       else iThresholds[7]  = Integer.parseInt(Magnetic_absolute_th_x.getText().toString());
                       if (Magnetic_absolute_th_y.getText().toString().isEmpty())              iThresholds[8]  = 0;
                       else iThresholds[8]  = Integer.parseInt(Magnetic_absolute_th_y.getText().toString());
                       if (Magnetic_absolute_th_z.getText().toString().isEmpty())              iThresholds[9]  = 0;
                       else iThresholds[9]  = Integer.parseInt(Magnetic_absolute_th_z.getText().toString());
                       if (Stable_time.getText().toString().isEmpty())                         iThresholds[10] = 0;
                       else iThresholds[10] = Integer.parseInt(Stable_time.getText().toString());
                       if (Magnetic_value_table_life.getText().toString().isEmpty())           iThresholds[11] = 0;
                       else iThresholds[11] = Integer.parseInt(Magnetic_value_table_life.getText().toString());
                       if (Ir_variation_check.getText().toString().isEmpty())                  iThresholds[12] = 0;
                       else iThresholds[12] = Integer.parseInt(Ir_variation_check.getText().toString());
                       if (Ir_linearity_correction.getText().toString().isEmpty())             iThresholds[13] = 0;
                       else iThresholds[13] = Integer.parseInt(Ir_linearity_correction.getText().toString());
                       if (Optical_free_timeout.getText().toString().isEmpty())                iThresholds[14] = 0;
                       else iThresholds[14] = Integer.parseInt(Optical_free_timeout.getText().toString());
                       if (Optical_busy_timeout.getText().toString().isEmpty())                iThresholds[15] = 0;
                       else iThresholds[15] = Integer.parseInt(Optical_busy_timeout.getText().toString());
                       if (Enable_saturated_magnetic_update.getText().toString().isEmpty())    iThresholds[16] = 0;
                       else iThresholds[16] = Integer.parseInt(Enable_saturated_magnetic_update.getText().toString());
                       if (Enable_ir_noise_check_mag_update.getText().toString().isEmpty())    iThresholds[17] = 0;
                       else iThresholds[17] = Integer.parseInt(Enable_ir_noise_check_mag_update.getText().toString());
                       dialog.cancel();
                   }
               });
               my_builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.cancel();
                   }
               });
               my_builder.setIcon(R.drawable.app_icon);
               my_builder.setTitle(R.string.thresholds);
               my_builder.setView(messageView);
               my_builder.create();
               my_builder.show();
               return true;
       }
       return false;
    }
}
