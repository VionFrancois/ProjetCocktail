package cp.umons.testAndroid


import android.Manifest
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity(){

    private lateinit var toggle : ActionBarDrawerToggle

    private val arduinoAddress : String = "8F5726231100"
    private val arduinoUUID : String = "00001101-0000-1000-8000-00805F9B34FB"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkBlePermissions()

        connectArduino()

        // Get the drawerLayout and the Navigation View items
        val drawerLayout : DrawerLayout = findViewById(R.id.nav_menu)
        val navView : NavigationView = findViewById(R.id.navView)

        // Set the icon and menu state
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // The menu items listener
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.pompes_menu -> Toast.makeText(applicationContext, "Pompes clicked", Toast.LENGTH_SHORT).show()
                R.id.boissons_menu -> Toast.makeText(applicationContext, "Boissons clicked", Toast.LENGTH_SHORT).show()
                R.id.cocktail_menu -> Toast.makeText(applicationContext, "Cocktail clicked", Toast.LENGTH_SHORT).show()
                R.id.verser_menu -> Toast.makeText(applicationContext, "Verser clicked", Toast.LENGTH_SHORT).show()
                R.id.POWER -> this.finishAffinity()
            }
            true
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
                        Toast.makeText(applicationContext, "The arduino MAC address wasn't found", Toast.LENGTH_LONG).show()
                    }
                }
                this.bluetoothAdapter?.cancelDiscovery()
                return false
            }
        }

        // Connect to device
        // If we arrive here, we know that there is only one device in bDeviceMAC, which is the one we want
        val connectThread = ConnectThread(bDeviceMAC.first())
        connectThread.start()

        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(this.receiver)
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread(){

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString(arduinoUUID))
        }

        override fun run() {
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.connect()
        }

        fun cancel(){
            try{
                mmSocket?.close()
            }catch(e : IOException){
                Toast.makeText(applicationContext, "Could not load the client socket " + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

}