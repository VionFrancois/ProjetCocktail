package cp.umons.testAndroid


import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
<<<<<<< HEAD
import androidx.core.app.ActivityCompat
=======
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintSet.Layout
>>>>>>> 62da6a6e847fe03b8bd07e3b3c9191ec3a5a2680
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(){

    // The icon to access the menu from TODO
    lateinit var toggle : ActionBarDrawerToggle

    private var arduinoAddress : String = ""

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

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                    // Bluetooth cancelled
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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun connectArduino() : Boolean {

        // Getting the device

        val bDevices = getBluetoothDevices() ?: return false
        val bDeviceMAC = bDevices.filter{ device ->
            device.address == this.arduinoAddress
        }
        if(bDeviceMAC.isEmpty()){
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(this.receiver, filter)
            if(this.arduinoDevice != null){
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 0)
                }
                this.bluetoothAdapter?.cancelDiscovery()
            }
        }

        // Connect to device


        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(this.receiver)
    }

}