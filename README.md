# Get Started with Remmie 2 Android SDK


# Download the SDK & Sample App
current version is 3.0
To use the Kotlin sample app, clone or download the Remmie 2 SDK android Sample App from GitHub [here|https://github.com/rayxiao-remmiehealth/remmiedolphin-sdk-android-sampleapp].

The download includes the SDK AAR file, API documentation, and Kotlin Sample App's source code.

If you only want to download the SDK file, you can download directly from [here|https://github.com/rayxiao-remmiehealth/remmiedolphin-sdk-android-sampleapp]


# Set up Sample App in Android Studio

You can import the sample app into Android Studio directly.

After building the project, you can run the sample app on android emulator or your mobile device via adb command.

**Note:** Due to WIFI limitation of Android Emulator, it's not recommended to start with an Android Emulator.

# Play with the Sample App

## Understand AP Mode vs Dual mode

* In AP Mode, the otoscope functions as a Wi-Fi access point, broadcasting its own network with the SSID “OTOSCOPE_XXXXXX” and the password “12345678”. 
  
  To use this mode, connect your client device to the “OTOSCOPE_XXXXXX” network and then initiate Point-to-Point streaming.

  This is the default mode out of box


* In Dual Mode, the otoscope operates as a Wi-Fi client by joining your existing Wi-Fi network. 

  Your existing WIFI network should configured with DHCP service, and able to assign a usable IP address to the otoscope. 

  By invoking the SDK’s OtoscopeConnector.connectToOtoscopeInGen2Mode() method, The SDK will automatically discover the otoscope device on the current Wi-Fi network and establish a connection.

  This is the preferred mode if the otoscope is intended for use in a stable environment, such as your home, or a dedicated office.

## Use the functionalities of the Sample App
Request Android Permissions by clicking the "Get Permission" button. This will greant the Sample App Android permissions of:
* android.permission.ACCESS_FINE_LOCATION
  * android.permission.ACCESS_COARSE_LOCATION
  * android.permission.READ_EXTERNAL_STORAGE
  * android.permission.WRITE_EXTERNAL_STORAGE
  * android.permission.WRITE_SETTINGS


### Connect to the Otoscope in AP Mode 
  If the Otoscope is newly open box, or has been resetted, you should start with AP mode.

   * Turn on the device, wait for 10-20 seconds, until you see solid white blinking slowly
   * Connect to the Otoscope Wifi network “OTOSCOPE_XXXXXX” with default password "12345678"
   * Click the "Connect Otoscope in Ap Mode" button
   * You should see "Device connected" message in the sample app


### Setup Otoscope in Client Mode
   * Finish connection in AP mode, then click "Home Wifi list Using Otoscope API"
   * Otoscope will try to scan the wifi, and return a list of supported SSIDs.
   * Sample App will show a popup selection box, ask you to select wifi to connect, and input password
   * You should see "Setup client mode success" message in sample App, the Otoscope will shutdown
   * Wait for 30 seconds, if Otoscope didn't boot automatically, manually turn it on
   * Otoscope should join your home network automatically.
  
### Connect to Otoscope in Client Mode
   * Join your home wifi
   * Click "Rediscover Device" button ,if the sample App didn't show "Device discovered" with an IP.
   * Once the Otoscope device discovered, click "Connect in Client Mode" button to connect.

### Real time Streaming, video recording ange image capture

* Streaming
   
  Once connected either in AP mode or Client mode,you can click "Start streaming" button to start streaming;
  
  If you are done with streaming, click "Stop streaming" button to stop streaming.

  * The Otoscope light will show solid white during streaming 
  * Once streaming stopped, the Otoscope light will become blinking white.

  * If the light is still solid white after stop streaming, it means Otoscope encountered a network issue when stopping streaming connection, and need to be rebooted.

* Recording

  During streaming, you can click "Start Recording" and "Stop Recoding" button to start/end recording.
  You must use "Stop recording" to finish a recording, otherwise the video will not properly recorded.


* Take Picture
  
  You can click "Take Picture" button to take a picture during streaming. A message will show up with the exact file location saved.

  

### Disconnect the device

  Click "Disconnect Device" button to disconnect the connection.

  
