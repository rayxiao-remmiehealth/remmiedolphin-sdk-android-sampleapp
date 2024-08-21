# Get Started with Remmie 2 Android SDK


## Download the SDK & Sample App
current version is 3.0
To use the Kotlin sample app, clone or download the Remmie 2 SDK android Sample App from GitHub [here|ph].

The download includes the SDK AAR file, API documentation, and Kotlin Sample App's source code.

If you only want to download the SDK file, you can download directly from [here|ph]


## Set up Sample App in Android Studio
1. Extract the contents of the Appstore_SDK_<version>.zip file somewhere to your local machine.
   
2. Start Android Studio and import the IAP sample apps to your workspace:

   1. On the Welcome to Android Studio screen, from the More Actions menu (the three stacked dots in the upper right) select Import Project (Eclipse ADT, Gradle, etc.) to open a project import window.
Or from within Android Studio, select File > New > Import Project.

   1. Navigate to the location where you extracted the contents of the SDK zip file

   2. Select the folder for one of the IAP sample projects, and click OK.

3. Generate the APK for the sample app:
   1. Enable developer options and USB debugging on your Android mobile device, and connect the device to your development computer. For help with connecting your device, see [Android Debug Bridge (adb)|https://developer.android.com/tools/adb].
   2. In Android Studio, select Run > Run 'app' to install the app to that device. 

**Note:** Due to WIFI limitation of Android Emulator, it's not recommended to start with an Android Emulator.

You should now be able to run this sample app on your mobile device.

When submitting your actual app to the Amazon Appstore, an Amazon signature will be applied to your app, regardless of whether you submit a signed or unsigned APK.


## Play with the Sample App

### Understand AP Mode vs Dual mode
* In AP Mode, the otoscope functions as a Wi-Fi access point, broadcasting its own network with the SSID “OTOSCOPE_XXXXXX” and the password “12345678”. 
  
  To use this mode, connect your client device to the “OTOSCOPE_XXXXXX” network and then initiate Point-to-Point streaming.

  This is the default mode out of box


* In Dual Mode, the otoscope operates as a Wi-Fi client by joining your existing Wi-Fi network. 
  Your existing WIFI network should configured with DHCP service, and able to assign a usable IP address to the otoscope. 
  By invoking the SDK’s OtoscopeConnector.connectToOtoscopeInGen2Mode() method, The SDK will automatically discover the otoscope device on the current Wi-Fi network and establish a connection.

  This is the preferred mode if the otoscope is intended for use in a stable environment, such as your home, or a dedicated office.

### Use the functionalities of the Sample App
1, request Android Permissions by clicking the "Get Permission" button. This will greant the Sample App Android permissions of:
* android.permission.ACCESS_FINE_LOCATION
* android.permission.ACCESS_COARSE_LOCATION
* android.permission.READ_EXTERNAL_STORAGE
* android.permission.WRITE_EXTERNAL_STORAGE
* .permission.WRITE_SETTINGS


2, if the Otoscope is newly open box, or has been resetted, you should start with AP mode.

   a. Turn on the device, wait for 10-20 seconds, until you see solid white blinking slowly
   b. Connect to the Otoscope Wifi network “OTOSCOPE_XXXXXX” with default password "12345678"
   c. Click the "Connect Otoscope in Ap Mode" button
   d. You should see "Device connected" message in the sample app


3, Setup Otoscope in Client Mode
   a. Finish connection in AP mode, then click "Home Wifi list Using Otoscope API"
   b. Otoscope will try to scan the wifi, and return a list of supported SSIDs.
   c. Sample App will show a popup selection box, ask you to select wifi to connect, and input password
   d. You should see "Setup client mode success" message in sample App, the Otoscope will shutdown
   e. Wait for 30 seconds, if Otoscope didn't boot automatically, manually turn it on
   f. Otoscope should join your home network automatically.
  
4, Connect to Otoscope in Client Mode
   a. Join your home wifi
   b. Click "Rediscover Device" button ,if the sample App didn't show "Device discovered" with an IP.
   c. Once the Otoscope device discovered, click "Connect in Client Mode" button to connect.

5, Real time Streaming, video recording ange image capture
Once connected either in AP mode or Client mode,you can click "Start streaming" button to start streaming;
If you are done with streaming, click "Stop streaming" button to stop streaming.

The Otoscope light will show solid white during streaming 

During streaming, you can click "Start Recording" and "Stop Recoding" button to start/end recording.
You must use "Stop recording" to finish a recording, otherwise the video will not properly recorded.

You can click "Take Picture" button to take a picture during streaming. A message will show up with the exact file location saved.

Once streaming stopped, the Otoscope light will become blinking white.

If the light is still solid white after stop streaming, it means Otoscope encountered a network issue when stopping streaming connection, and need to be rebooted.

6, Disconnect the device

Click "Disconnect Device" button to disconnect the connection.

  
