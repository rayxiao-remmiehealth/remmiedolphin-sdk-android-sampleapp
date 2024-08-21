package com.remmiehealth.remmietestappforsdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.remmie.remmieDolphinSdk.RemmieEventListener
import com.remmie.remmieDolphinSdk.RemmieOtoscopeManager
import java.util.stream.Collectors


class MainActivity : AppCompatActivity(), RemmieEventListener,
    WifiConnectionInterface {


    val TAG: String = "MainActivity"

    var lastKnownOtoscopeSSID: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this.supportActionBar != null) {
            this.supportActionBar!!.hide()
        }
        setContentView(R.layout.activity_main)
        remmieOtoscopeManager = RemmieOtoscopeManager()

        //remmieController!!.lastKnownOtoscopeSSID = "None found yet"
        remmieOtoscopeManager!!.initialize(this.applicationContext, this)
        remmieOtoscopeManager!!.configStreaming(this, findViewById(R.id.imageView))
        remmieOtoscopeManager!!.usePrivateDirectory(true)
        remmieOtoscopeManager!!.useSubdirectory(false)
        remmieOtoscopeManager!!.setCustomDirectory("patient")
        setButtons()
        this.findViewById<TextView>(R.id.retryText).visibility = View.GONE
        StatusHandler.postDelayed(StatusRunnable, 1000)

        wifiHelper = RemmieWifiHelper()
        wifiHelper!!.init(this)
        wifiHelper!!.doInitiateWifiScanner(this.applicationContext)

        wifiScanHandler.postDelayed(wifiScanRunnable, 1000)

        runOnUiThread {
            this.findViewById<ImageView>(R.id.pictureButton).visibility = View.GONE
            this.findViewById<ImageView>(R.id.recordButton).visibility = View.GONE
        }
    }

    var remmieOtoscopeManager: RemmieOtoscopeManager? = null
    internal var wifiHelper: RemmieWifiHelper? = null

    fun setTransparentNavigationAndStatusBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlagForTransparentNavigationBars(
                this,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                true
            )
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlagForTransparentNavigationBars(
                this,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                false
            )
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    fun setWindowFlagForTransparentNavigationBars(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams

        val params = win.attributes
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        win.attributes = params
    }

    @RequiresApi(api = 23)
    fun requestPermissions(callingActivity: AppCompatActivity?) {
        this.getPermisisons(this)
    }

    private fun checkPermissions(callingActivity: AppCompatActivity): Boolean {
        var didAnyPermissionsFail = false
        if (ContextCompat.checkSelfPermission(
                callingActivity,
                "android.permission.ACCESS_FINE_LOCATION"
            ) != 0
        ) {
            didAnyPermissionsFail = true
        }

        if (ContextCompat.checkSelfPermission(
                callingActivity,
                "android.permission.ACCESS_COARSE_LOCATION"
            ) != 0
        ) {
            didAnyPermissionsFail = true
        }

        if (ContextCompat.checkSelfPermission(
                callingActivity,
                "android.permission.READ_EXTERNAL_STORAGE"
            ) != 0
        ) {
            didAnyPermissionsFail = true
        }

        if (ContextCompat.checkSelfPermission(
                callingActivity,
                "android.permission.WRITE_EXTERNAL_STORAGE"
            ) != 0
        ) {
            didAnyPermissionsFail = true
        }

        val permission: Boolean
        if (VERSION.SDK_INT >= 23) {
            permission = android.provider.Settings.System.canWrite(callingActivity)
        } else {
            permission = ContextCompat.checkSelfPermission(
                callingActivity,
                "android.permission.WRITE_SETTINGS"
            ) == 0
        }

        if (!permission) {
            didAnyPermissionsFail = true
        }

        return didAnyPermissionsFail
    }


    @RequiresApi(api = 23)
    private fun getPermisisons(callingActivity: AppCompatActivity) {
        if (ContextCompat.checkSelfPermission(
                callingActivity,
                "android.permission.READ_EXTERNAL_STORAGE"
            ) != 0
        ) {
            callingActivity.requestPermissions(
                arrayOf(
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION"
                ), 3
            )
        }

        val permission: Boolean
        if (VERSION.SDK_INT >= 23) {
            permission = android.provider.Settings.System.canWrite(callingActivity)
        } else {
            permission = ContextCompat.checkSelfPermission(
                callingActivity,
                "android.permission.WRITE_SETTINGS"
            ) == 0
        }

        if (!permission) {
            if (VERSION.SDK_INT >= 23) {
                val intent = Intent("android.settings.action.MANAGE_WRITE_SETTINGS")
                intent.setData(Uri.parse("package:" + callingActivity.packageName))
                callingActivity.startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(
                    callingActivity,
                    arrayOf("android.permission.WRITE_SETTINGS"),
                    0
                )
            }
        }
    }

    private fun setButtons() {
        findViewById<Button>(R.id.getPermissionsButton).setOnClickListener() {
            requestPermissions(this);
        }
        findViewById<Button>(R.id.connectOtoscopeClientModeButton).setOnClickListener() {
            this.findViewById<TextView>(R.id.retryText).visibility = View.GONE
            connectToOtoscopeClientMode()
        }

        findViewById<Button>(R.id.stopStreamButton).setOnClickListener() {
            stopStream()
        }
        findViewById<Button>(R.id.disconnectButton).setOnClickListener() {
            disconnect()
        }

        findViewById<ImageView>(R.id.recordButton).setOnClickListener {
            videoButtonClicked()
        }
        findViewById<ImageView>(R.id.pictureButton).setOnClickListener {


            pictureButtonClicked()
        }

        findViewById<Button>(R.id.scanWifiButton).setOnClickListener() {
            wifiHelper!!.scanWifi();

        }
        findViewById<Button>(R.id.getWifiListButton).setOnClickListener() {
            displayListOfNetworks()
        }
        findViewById<Button>(R.id.connectToOtoscopeInApMode).setOnClickListener() {
            this.connectToOtoscopeApMode()
        }
        findViewById<Button>(R.id.startStreamingButton).setOnClickListener() {
            this.findViewById<TextView>(R.id.retryText).visibility = View.GONE
            startStream()
        }
        findViewById<Button>(R.id.startRecordingButton).setOnClickListener() {
            remmieOtoscopeManager!!.startRecording()

        }
        findViewById<Button>(R.id.stopRecordingButton).setOnClickListener() {
            remmieOtoscopeManager!!.stopRecording()
        }
        findViewById<Button>(R.id.takePictureButton).setOnClickListener() {
            remmieOtoscopeManager!!.capturePicture()
        }

        findViewById<Button>(R.id.homeWifiButton).setOnClickListener() {
            displayListOfWifiUsingOtoScopeAPI()
        }
        findViewById<Button>(R.id.rediscoveryButton).setOnClickListener() {
            remmieOtoscopeManager!!.startDiscovery()

        }
//        findViewById<Button>(R.id.reconnectOtoscope).setOnClickListener() {
//            remmieOtoscopeManager!!.connectToOtoscope()
//
//        }
    }


    fun displayListOfNetworks() {
        // todo ->  Android Wifi List get Using Wifi Scanner
        var ssidList: List<String> = wifiHelper!!.getAvailableSsids()
        Log.e(TAG, "SSIDs are: " + ssidList.toString())
        val innerScroll = findViewById<LinearLayout>(R.id.innerScrollview)
        innerScroll.removeAllViews()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ssidList = ssidList.stream().collect(Collectors.toList()) as ArrayList<String>
        }
        ssidList.forEach {
            if (!it.isEmpty()) {
                val networkId: String = it
                val tx: TextView = TextView(this)
                tx.setTextColor(Color.WHITE)
                tx.layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                tx.setText("SSID: " + it)
                innerScroll.addView(tx)
            }
        }

    }


    private fun displayListOfWifiUsingOtoScopeAPI() {
        // todo ->  Wifi List get Using Otoscope API
        remmieOtoscopeManager!!.listOtoscopeWifi()

    }

    override fun otoscopeWifiList(wifiList: ArrayList<String>) {
        Log.e(TAG, "OtoscopeWifiList received from device: $wifiList")
        homeWifiSelectionPopup(wifiList)
    }


    fun toastMessage(message: String) {

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun pictureButtonClicked() {
        remmieOtoscopeManager!!.capturePicture()
    }

    fun videoButtonClicked() {
        if (!remmieOtoscopeManager!!.isRecording()) {
            remmieOtoscopeManager!!.startRecording()
            findViewById<ImageView>(R.id.recordButton).setImageResource(R.drawable.stop)
        } else {
            remmieOtoscopeManager!!.stopRecording()
            findViewById<ImageView>(R.id.recordButton).setImageResource(R.drawable.record)
        }
    }


    fun displayOtoscopeSSID() {
        if (lastKnownOtoscopeSSID != null) {
            findViewById<TextView>(R.id.OtoSSIDtext).text =
                "Otoscope SSID: " + lastKnownOtoscopeSSID
        } else {
            findViewById<TextView>(R.id.OtoSSIDtext).text = "Otoscope SSID: "
        }
    }

    fun displayCurrentSSID() {
        val ssidText = wifiHelper!!.getCurrentSSID(this);

        findViewById<TextView>(R.id.CurrSSIDtext).setText(buildString {
            append("Current SSID: ")
            append('"')
            append(ssidText)
            append('"')
        })
    }


    fun isOtoscopeReadyToStream() {
        if (remmieOtoscopeManager!!.isDeviceConnected()) {
            findViewById<ImageView>(R.id.isStreamReadySignifier).setColorFilter(Color.GREEN)
//            if (streaming == false) {
//                startStream()
//                streaming = true
//            }
        } else {
            findViewById<ImageView>(R.id.isStreamReadySignifier).setColorFilter(Color.RED)
        }
    }

    override fun otoscopeEvent(
        event: RemmieEventListener.OtoscopeEvent,
        data: Any?,
        message: String
    ) {
        Log.d(TAG, "otoscope event received! "+event+",data "+data+", message:"+message)
        val eventsBox: EditText = this.findViewById(R.id.event_box)
        var eventText:String
        when (event){
            RemmieEventListener.OtoscopeEvent.STREAM_STARTED ->{eventText = "Stream Started"}
            RemmieEventListener.OtoscopeEvent.STREAM_STOPPED ->{
                eventText = "Stream Stopped"
                runOnUiThread {
                    findViewById<ImageView>(R.id.isStreamReadySignifier).setColorFilter(Color.RED)

                    this.findViewById<TextView>(R.id.retryText).visibility = View.VISIBLE
                    this.findViewById<ImageView>(R.id.pictureButton).visibility = View.GONE
                    this.findViewById<ImageView>(R.id.recordButton).visibility = View.GONE
                }

            }
            RemmieEventListener.OtoscopeEvent.SETUP_CLIENT_MODE_SUCCESS ->{eventText = "Setup client mode success"}
            RemmieEventListener.OtoscopeEvent.SETUP_CLIENT_MODE_FAILED ->{eventText = "Setup client mode failed"}
            RemmieEventListener.OtoscopeEvent.START_DISCOVER ->{eventText = "Start discovering"}
            RemmieEventListener.OtoscopeEvent.DISCOVERED ->{eventText = "Device discovered, type:"+data+", addr:"+message}
            RemmieEventListener.OtoscopeEvent.STOP_DISCOVER ->{eventText = "Stop discovering"}
            RemmieEventListener.OtoscopeEvent.CONNECTED ->{
                runOnUiThread {
                    this.findViewById<TextView>(R.id.status_label).setText("Connected in " + data + " mode")
                }
                eventText = "device connected"
            }
            RemmieEventListener.OtoscopeEvent.CONNECT_FAILED ->{eventText = "Device failed to connect"}
            RemmieEventListener.OtoscopeEvent.DISCONNECTED ->{
                runOnUiThread {
                    this.findViewById<TextView>(R.id.status_label).setText( "Disconnected")
                }
                eventText = "device disconnected!"
            }
            RemmieEventListener.OtoscopeEvent.STREAM_STARTED->{eventText = "Stream started"}
            RemmieEventListener.OtoscopeEvent.STREAM_STOPPED->{eventText = "Stream stopped"}
            RemmieEventListener.OtoscopeEvent.STREAM_FAILED->{eventText = "Stream failed,"+message}
            RemmieEventListener.OtoscopeEvent.RECORDING_STARTED->{eventText = "Recording started."}
            RemmieEventListener.OtoscopeEvent.RECORDING_FAILED->{eventText = "Recording failed,"+message}
            RemmieEventListener.OtoscopeEvent.RECORDING_FINISHED->{eventText = "Recording finished, video saved to,"+message}
            RemmieEventListener.OtoscopeEvent.PHOTO_TAKEN->{eventText = "Photo saved to "+data.toString()}
            RemmieEventListener.OtoscopeEvent.PHOTO_FAILED->{eventText = "Take Photo failed,"+data.toString()}
        }

        runOnUiThread {
            eventsBox.setText(eventsBox.text.toString()+"\n"+eventText+".")
        }

    }
    fun connectToOtoscopeClientMode() {
        remmieOtoscopeManager!!.connectOtoscopeClientMode()
    }


    fun connectToOtoscopeApMode() {
        remmieOtoscopeManager!!.bindOtoscope(wifiHelper!!.currentNetwork)
        remmieOtoscopeManager!!.connectOtoscopeAPMode()
    }

    var StatusHandler: Handler = Handler()
    var StatusRunnable = object : Runnable {
        override fun run() {
            isOtoscopeReadyToStream()
            displayOtoscopeSSID()
            displayCurrentSSID()
            runOnUiThread {
                displayCaptures()
            }
            StatusHandler.postDelayed(this, 1000)
        }
    }


    fun displayCaptures() {
        val paths: ArrayList<String> = remmieOtoscopeManager!!.listRecords()
        findViewById<LinearLayout>(R.id.innerImageHolder).removeAllViews()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            paths.forEach {
                val x = it.split("/")
                val newImageView: ImageView = ImageView(this)
                newImageView.layoutParams = ConstraintLayout.LayoutParams(100, 100)
                if (x[6].contains("png")) {
                    newImageView.setImageURI(Uri.parse(it))
                } else {
                    newImageView.setImageResource(R.drawable.record)
                }
                findViewById<LinearLayout>(R.id.innerImageHolder).addView(newImageView)
            }
        } else {
            paths.forEach {
                val x = it.split(".")
                val newImageView: ImageView = ImageView(this)
                newImageView.layoutParams = RelativeLayout.LayoutParams(100, 100)
                if (x[6].contains("png")) {
                    newImageView.setImageURI(Uri.parse(it))
                } else {
                    newImageView.setImageResource(R.drawable.record)
                }
                findViewById<LinearLayout>(R.id.innerImageHolder).addView(newImageView)
            }
        }
    }

    private fun startStream() {
        runOnUiThread {
            this.findViewById<ImageView>(R.id.pictureButton).visibility = View.VISIBLE
            this.findViewById<ImageView>(R.id.recordButton).visibility = View.VISIBLE
        }
        remmieOtoscopeManager!!.startStreamForPreview()
    }

    private fun stopStream() {
        remmieOtoscopeManager!!.stopStreamForPreview()
        findViewById<ImageView>(R.id.isStreamReadySignifier).setColorFilter(Color.RED)
        this.findViewById<TextView>(R.id.retryText).visibility = View.VISIBLE
        this.findViewById<ImageView>(R.id.pictureButton).visibility = View.GONE
        this.findViewById<ImageView>(R.id.recordButton).visibility = View.GONE
    }

    private fun disconnect() {

        remmieOtoscopeManager!!.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        StatusHandler.removeCallbacks(StatusRunnable)
    }



//    override fun failureDetected() {
//        this.findViewById<TextView>(R.id.retryText).visibility = View.VISIBLE
//        this.findViewById<ImageView>(R.id.pictureButton).visibility = View.GONE
//        this.findViewById<ImageView>(R.id.recordButton).visibility = View.GONE
//        remmieOtoscopeManager!!.stopStreamForPreview()
//        streaming = false
//    }


    var callbackToUnbindFrom: ConnectivityManager.NetworkCallback? = null
    var networkToBindTo: Network? = null
    var needToUnbind: Boolean = false
    var boundYet: Boolean = false

    override fun networkToBind(network: Network?) {
        Log.d(TAG, "Network to bind to $network")
        if (network != null) {
            networkToBindTo = network

            if (!boundYet) {
                Log.e(TAG, "Not Binded yet, start binding...")
                val connectivityManager =
                    applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (networkToBindTo != null) {
                    connectivityManager.bindProcessToNetwork(networkToBindTo)
                    boundYet = true
                }
            }

        }
    }

    override fun callbackToUnbind(networkCallback: ConnectivityManager.NetworkCallback?) {
        Log.d(TAG, "Network to unbind to $networkCallback")
        if (networkCallback != null) {
            callbackToUnbindFrom = networkCallback
            needToUnbind = true
        }
    }

    var wifiScanHandler: Handler = Handler()
    var scanStart: Int = 30
    var wifiScanRunnable = object : Runnable {
        override fun run() {
            wifiHelper!!.scanWifi()
            scanStart = 30
            wifiScanHandler.postDelayed(this, 30000)
        }
    }


    fun homeWifiSelectionPopup(SSIDs: List<String>) {
        var inflater: LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var popupView: View = inflater.inflate(R.layout.wifi_selection_popup, null)
        var popupWindow: PopupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        runOnUiThread {
            popupWindow.showAtLocation(
                this.findViewById(R.id.isLoginReadySignifier), Gravity.CENTER, 0, 0
            )
            popupView.findViewById<Button>(R.id.selectSSIDcancel).setOnClickListener() {
                popupWindow.dismiss()
            }

            var innerscroll = popupView.findViewById<LinearLayout>(R.id.selectWifiTextHolder)


            //        var SSIDs: ArrayList<String> = integrator!!.listOfAvailableWifiNetworks
            var SSIDs: ArrayList<String> = SSIDs as ArrayList<String>
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SSIDs = SSIDs.stream().collect(Collectors.toList()) as ArrayList<String>
            }
            SSIDs.forEach {
                val networkid: String = it
                val tx: TextView = TextView(this)
                tx.setTextColor(Color.WHITE)
                tx.layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )

                tx.setPadding(0, 20, 0, 20)

                tx.setText("SSID: " + it)
                innerscroll.addView(tx)
                tx.setOnClickListener() {
                    EnterPasswordPopup(networkid)
                    popupWindow.dismiss()
                }
            }
        }
    }

    fun EnterPasswordPopup(ssid: String) {
        val inflater: LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.enter_password_popup, null)
        val popupWindow: PopupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.showAtLocation(
            this.findViewById(R.id.isLoginReadySignifier), Gravity.CENTER, 0, 0
        )
        popupView.findViewById<Button>(R.id.enterPasswordCancel).setOnClickListener() {
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.ssidHolderText).text = ssid
        popupView.findViewById<Button>(R.id.submitPasswordButton).setOnClickListener() {
            val pw: String =
                popupView.findViewById<EditText>(R.id.passwordInputEditText).getText().toString()
            setupOtoscopeFirstTime(ssid, pw)
            popupWindow.dismiss()
        }

    }

    fun setupOtoscopeFirstTime(ssid: String, password: String) {
        try {
            remmieOtoscopeManager!!.configureOtoscopeClientMode(ssid, password)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}