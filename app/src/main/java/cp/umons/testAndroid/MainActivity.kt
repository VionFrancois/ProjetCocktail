package cp.umons.testAndroid


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(){

    private lateinit var toggle : ActionBarDrawerToggle

    private val arduinoAddress : String = "8F:57:26:23:11:00"
    private val arduinoUUID : String = "00001101-0000-1000-8000-00805F9B34FB"
    private val arduinoPin : Int = 8610

    private var permissionsCount = 0

    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var bluetoothManager: BluetoothManager
    private var arduinoDevice: BluetoothDevice? = null
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1?.action){
                BluetoothDevice.ACTION_FOUND -> {
                    val device : BluetoothDevice? = p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    val dAddress = device?.address
                    if(dAddress == arduinoAddress){
                        arduinoDevice = device
                    }
                }
            }
        }
    }

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkBlePermissions()

        connectArduino()
        val socket = connectToArduino()

        // Get the drawerLayout and the Navigation View items
        val drawerLayout : DrawerLayout = findViewById(R.id.nav_menu)
        val navView : NavigationView = findViewById(R.id.navView)
        val mainPage : ConstraintLayout = findViewById(R.id.main_page)

        // Set the icon and menu state
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // The menu items listener
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.pompes_menu -> Log.i("Message", "Pompes menu pressed")
                R.id.boissons_menu -> Log.i("Message", "Boissons menu pressed")
                R.id.cocktail_menu -> Log.i("Message", "Cocktail menu pressed")
                R.id.verser_menu -> Log.i("Message", "Verser menu pressed")
                R.id.POWER -> this.finishAffinity()
            }
            true
        }
        mainPage.setOnClickListener {
            drawerLayout.closeDrawers()
        }
        val grenadineButton : Button = findViewById(R.id.grenadine_main_button)
        val mentheButton : Button = findViewById(R.id.menthe_main_button)
        grenadineButton.setOnClickListener {
            try {
                Log.i("Message", "Grenadine button pressed")
                socket?.sendMessage("M1230\nM222\n$")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        mentheButton.setOnClickListener {
            try {
                Log.i("Message", "Menthe button pressed")
                socket?.sendMessage("M1230\nM322\n$")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    private fun getMissingBlePermissions(): Array<String?>? {
        var missingPermissions: Array<String?>? = null
        // For Android 12 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions = arrayOfNulls(1)
                missingPermissions[0] = Manifest.permission.BLUETOOTH_SCAN
            }
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (missingPermissions == null) {
                    missingPermissions = arrayOfNulls(1)
                    missingPermissions[0] = Manifest.permission.BLUETOOTH_CONNECT
                } else {
                    missingPermissions = Arrays.copyOf(missingPermissions, missingPermissions.size + 1)
                    missingPermissions[missingPermissions.size - 1] = Manifest.permission.BLUETOOTH_CONNECT
                }
            }
        }
        return missingPermissions
    }

    private fun checkBlePermissions() {
        val missingPermissions = getMissingBlePermissions()
        if (missingPermissions.isNullOrEmpty()) {
            Log.i("TAG", "checkBlePermissions: Permissions is already granted")
            return
        }
        for (perm in missingPermissions) Log.d(
            "TAG",
            "checkBlePermissions: missing permissions $perm"
        )
        permissionsCount = missingPermissions.size
        requestPermissions(missingPermissions, 0x55)
    }

    private fun enableBluetooth() : Boolean {
        this.bluetoothManager = getSystemService(BluetoothManager::class.java)
        this.bluetoothAdapter = bluetoothManager.adapter
        if(bluetoothAdapter == null) {
            return false
        }
        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForActivityResult(StartActivityForResult()){
                if(it.resultCode != Activity.RESULT_CANCELED){
                    Toast.makeText(applicationContext, "Connection canceled", Toast.LENGTH_LONG).show()
                }
            }.launch(enableBtIntent)
        }
        return true
    }

    private fun getBluetoothDevices() : Set<BluetoothDevice>? {
        val bDevices = if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED) {
            this.enableBluetooth()
            null
        } else {
            this.bluetoothAdapter?.bondedDevices
        }

        val a : String = ""
        bDevices?.forEach { device -> a.plus(device.name) }
        Log.d("TAG", a)
        return bDevices
    }

    private fun bluetoothOff() : Boolean{
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        this.bluetoothAdapter?.disable()
        return true
    }

    /**
     * Connect to the Arduino (easy method)
     */
    private fun connectToArduino() : socketThread?{
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.i("ERROR", "Permission not granted")
            return null
        }

        this.bluetoothManager = getSystemService(BluetoothManager::class.java)
        this.bluetoothAdapter = bluetoothManager.adapter
        this.arduinoDevice = this.bluetoothAdapter?.getRemoteDevice(this.arduinoAddress)

        if(this.arduinoDevice == null){
            Log.i("ERROR", "Device not found")
            return null
        }
        val socketThread = socketThread()
        socketThread.start()
        return socketThread
    }

    private inner class socketThread : Thread() {

        private lateinit var socket : BluetoothSocket
        private var isConnected = false

        override fun run() {
            super.run()
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            this.socket = arduinoDevice?.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))!!
            bluetoothAdapter?.cancelDiscovery()
            socket.connect()
            this.isConnected = true
        }

        fun sendMessage(message: String){
            if(!this.isConnected){
                Log.i("ERROR", "Not connected")
                return
            }
            val buffer = message.toByteArray()
            val outputStream = socket.outputStream
            outputStream?.write(buffer)
            outputStream?.flush()
            val buffer2 = ByteArray(1024)
            val bytes = socket.inputStream.read(buffer2)
            val response = String(buffer2, 0, bytes)
            Log.i("Message", response)
        }
    }

    /**
     * Connect to the Arduino (hard method)
     * Idk which one is better
     */
    private fun connectArduino() : Boolean {

        // Getting the device

        var bDevices = getBluetoothDevices() ?: return false
        var bDeviceMAC = bDevices.filter{ device ->
            device.address == this.arduinoAddress
        }
        if(bDeviceMAC.isEmpty()){
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(this.receiver, filter)
            if(this.arduinoDevice != null){
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED) {
                    this.bluetoothAdapter?.startDiscovery()
                    bDevices = getBluetoothDevices() ?: return false

                    bDeviceMAC = bDevices.filter{ device ->
                        device.address == this.arduinoAddress
                    }

                    if(bDeviceMAC.isEmpty()){
                        Log.i("ERROR", "The arduino MAC address wasn't found")
                    }
                }
                this.bluetoothAdapter?.cancelDiscovery()
                return false
            }
        }

        if (this.bluetoothAdapter?.isEnabled == true) {
            val prefs_btdev = getSharedPreferences("btdev", 0)
            val btdevaddr = prefs_btdev.getString("btdevaddr", "?")
            if (btdevaddr !== "?") {
                val device = this.bluetoothAdapter?.getRemoteDevice(btdevaddr)
                val SERIAL_UUID = UUID.fromString(this.arduinoUUID) // bluetooth serial port service
                var socket: BluetoothSocket? = null
                try {
                    socket = device?.createRfcommSocketToServiceRecord(SERIAL_UUID)
                } catch (e: Exception) {
                    Log.e("", "Error creating socket")
                }
                try {
                    socket!!.connect()
                    Log.e("", "Connected")
                } catch (e: IOException) {
                    Log.e("", e.message!!)
                    try {
                        Log.e("", "trying fallback...")
                        socket = device?.javaClass?.getMethod(
                            "createRfcommSocket",
                            *arrayOf<Class<*>?>(Int::class.javaPrimitiveType)
                        )?.invoke(device, 1) as BluetoothSocket
                        socket.connect()
                        Log.e("", "Connected")
                    } catch (e2: Exception) {
                        Log.e("", "Couldn't establish Bluetooth connection!")
                    }
                }
            } else {
                Log.e("", "BT device not selected")
            }
        }

        return true
    }

    /**
     * Send a message to the arduino
     */
    private fun sendBluetoothMessage(socket : BluetoothSocket, pump : Byte, quantity : ByteArray) : Boolean{
        try{
            if(!socket.isConnected){
                return false
            }
            val outputStream = socket.outputStream
            val message = byteArrayOf('M'.code.toByte(), pump).plus(quantity)
            outputStream.write(message)

            outputStream.flush()
            return true
        }
        catch (e : IOException){
            Log.e("EXCEPTION", "sendBluetoothMessage: $e")
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(this.receiver)
    }

}