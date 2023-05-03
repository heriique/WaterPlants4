package com.example.waterplants.model

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MyBluetooth(private val appCompatActivity: AppCompatActivity, private val handler: Handler) {
    private var bluetoothAdapter: BluetoothAdapter
    private lateinit var btSocket : BluetoothSocket

    private var _isConnected = MutableLiveData(false)
    val isConnected : LiveData<Boolean>
        get() {
            return _isConnected
        }

    // Serial port UUID
    // https://stackoverflow.com/questions/4632524/how-to-find-the-uuid-of-serial-port-bluetooth-device
    private val _myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // Tag for debug info
    private val _tag = MyBluetooth::class.qualifiedName

    // ActivityResultLauncher for requesting BT permissions with API < 33
    private var requestBluetooth = appCompatActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            /*Toast.makeText(appCompatActivity.applicationContext,
                "Permission to connect is granted!", Toast.LENGTH_SHORT).show()*/
            MessageThread.postMessage("Permission to connect is granted!",
                MessageType.TOAST, handler)
        }else{
            requestBTPermissions()
        }
    }

    // ActivityResultLauncher for requesting BT permissions with API >= 33
    private val requestMultiplePermissions =
        appCompatActivity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(_tag, "${it.key} = ${it.value}")
            }
        }

    private fun onPermissionDenied() {
        /*Toast.makeText(appCompatActivity.applicationContext,
            "BLUETOOTH_CONNECT permission is not granted! Please allow.",
            Toast.LENGTH_LONG).show()*/
        MessageThread.postMessage("BLUETOOTH_CONNECT permission is not granted! Please allow.",
            MessageType.TOAST, handler)
        requestBTPermissions()
        _isConnected.postValue(false)
    }

    init {
        //requestBTPermissions() // Seems not necessary here. Will be done later when needed.
        val bluetoothManager = appCompatActivity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    private fun requestBTPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    fun scanAndConnect(): Boolean {
        if (ActivityCompat.checkSelfPermission(appCompatActivity,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // We don't have permissions.
            onPermissionDenied()
            return false
        }
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        if (pairedDevices != null) {
            for (device in pairedDevices) {
                if (device.name == "HC-05") {
                    return connect(device.address)
                }
            }
        }
        else {

        }
        return false
    }

    fun connect(address: String): Boolean {
        // Address discovered with 3rd party Bluetooth Scanner app
        // Probably unique address for each HC-05 device.
        val device = bluetoothAdapter.getRemoteDevice(address) //"98:DA:50:01:33:48" - Eric sin HC-05
        Log.d(_tag, "Connecting to ... $device")
        Log.d(_tag, "Device name: ${device.name}")
        //Toast.makeText(appCompatActivity.applicationContext, "Connecting...", Toast.LENGTH_SHORT).show()
        MessageThread.postMessage("Connecting...", MessageType.TOAST, handler)
        if (ActivityCompat.checkSelfPermission(
                appCompatActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // We don't have permissions.
            onPermissionDenied()
            return false
        }
        //Toast.makeText(appCompatActivity.applicationContext, "Connecting to ... ${device.name}\n mac: ${device.uuids[0]}\n address: ${device.address}", Toast.LENGTH_SHORT).show()
        bluetoothAdapter.cancelDiscovery()

        // Establish connection
        try {
            btSocket = device.createRfcommSocketToServiceRecord(_myUUID)
            /* Here is the part the connection is made, by asking the device to create a RfcommSocket (Unsecure socket I guess), It map a port for us or something like that */
            btSocket.connect()
            Log.d(_tag, "Connection made.")
            /*Toast.makeText(appCompatActivity.applicationContext,
                "Connection made.", Toast.LENGTH_SHORT).show()*/
            MessageThread.postMessage("Connection made.", MessageType.TOAST, handler)
            _isConnected.value = true
            ConnectedThread().start()
            return true
        } catch (e: IOException) {
            try {
                btSocket.close()
            } catch (e2: IOException) {
                Log.d(_tag, "Unable to end the connection.\n" + e2.message)
                /*Toast.makeText(appCompatActivity.applicationContext,
                    "Unable to end the connection.", Toast.LENGTH_SHORT).show()*/
                MessageThread.postMessage("Unable to end the connection.", MessageType.TOAST, handler)
                _isConnected.value = false
                return false
            }

            Log.d(_tag, "Socket creation failed.\n" + e.message)
            /*Toast.makeText(appCompatActivity.applicationContext,
                "Socket creation failed.", Toast.LENGTH_SHORT).show()*/
            MessageThread.postMessage("Socket creation failed.", MessageType.TOAST, handler)
            _isConnected.postValue(false)
            return false
        }

    }

    fun disconnect() {
        ConnectedThread().cancel()
    }

    // Old write function, do not use
    fun writeData2(data: String): Boolean {
        if (!isConnected.value!!)
            return false
        val outStream: OutputStream
        try {
            outStream = btSocket.outputStream
        } catch (e: IOException) {
            Log.d(_tag, "Error before sending stuff", e)
            return false
        }
        val msgBuffer = data.toByteArray()

        try {
            outStream.write(msgBuffer)
        } catch (e: IOException) {
            Log.d(_tag, "Error while sending stuff", e)
            return false
        }
        return true
    }

    fun writeData(data: String) {
        ConnectedThread().write(data.toByteArray())
    }

    // Old function, reading is done in ConnectedThread()
    fun readData(): String {
        if (!isConnected.value!!)
            return ""
        var inStream = btSocket.inputStream
        try {
            inStream = btSocket.inputStream
        } catch (e: IOException) {
            Log.d(_tag, "Error before receiving stuff", e)
        }

        var s = ""

        try {
            while (inStream.available() > 0) {
                // https://developer.android.com/reference/java/io/InputStream#read()
                val c = inStream.read().toChar()
                s += c
                if (c == '\n')
                    break
            }
        } catch (e: IOException) {
            Log.d(_tag, "Error while receiving stuff", e)
        } finally {
            Log.i(_tag, "INFO: Read string: $s")
        }
        return s
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanAndConnect()
        }
    }

    // https://developer.android.com/guide/topics/connectivity/bluetooth/transfer-data
    // Inner class is static
    private inner class ConnectedThread : Thread() {
        private val inStream: InputStream = btSocket.inputStream
        private val outStream: OutputStream = btSocket.outputStream
        private val buffer: ByteArray = ByteArray(1024)

        override fun run() {
            var numBytes: Int // Bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    inStream.read(buffer)
                } catch (e: IOException) {
                    Log.d(_tag, "Input stream was disconnected", e)
                    _isConnected.postValue(false)
                    break
                }

                // Send the obtained bytes to the message handler.
                /*val readMsg = handler.obtainMessage()
                val bundle = Bundle().apply { putString(MessageType.READ,
                    buffer.decodeToString(0, numBytes)) }
                readMsg.data = bundle
                readMsg.sendToTarget()*/
                MessageThread.postMessage(buffer.decodeToString(0, numBytes), MessageType.READ, handler)
            }
        }

        fun write(bytes: ByteArray) {
            try {
                outStream.write(bytes)
            } catch (e: IOException) {
                Log.e(_tag, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                /*val writeErrorMsg = handler.obtainMessage()
                val bundle = Bundle().apply {
                    putString(MessageType.TOAST, "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)*/
                MessageThread.postMessage("Couldn't send data to the other device", MessageType.TOAST, handler)
                return
            }

            // Share the sent message with the message handler.
            /*val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, buffer)*/
            /*val writtenMsg = handler.obtainMessage()
            val bundle = Bundle().apply { putString(MessageType.WRITE, String(bytes)) }
            writtenMsg.data = bundle
            writtenMsg.sendToTarget()*/
            MessageThread.postMessage(String(bytes), MessageType.WRITE, handler)
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                btSocket.close()
            } catch (e: IOException) {
                Log.e(_tag, "Could not close the connect socket", e)
            }
        }
    }

    /*override fun close() {
        onDestroy()
    }*/

}