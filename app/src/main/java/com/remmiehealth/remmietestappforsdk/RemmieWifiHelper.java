package com.remmiehealth.remmietestappforsdk;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

class RemmieWifiHelper {
    static final String TAG = "RemmieWifiHelper";
    //public
    public String lastKnownOtoscopeSSID = null;

    private WifiConnectionInterface wci;
    WifiManager wifiManager;
    private static final int CODE_WRITE_SETTINGS_PERMISSION = 0;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public Network currentNetwork;

    RemmieWifiHelper() {
    }

    public void init(WifiConnectionInterface wci) {
        this.wci = wci;
    }

    public boolean connectToOtoscopeWifi( Context appContext) {
        boolean success = true;
        if (lastKnownOtoscopeSSID==null){
            return false;
        }
        String password = "12345678";
        Log.e(TAG, "connectToWifi: Wifi " + lastKnownOtoscopeSSID + "|| Version -> " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT <= 28) {
            this.loopingConnectToWifiAttemptAndroidNineAndTen(appContext, lastKnownOtoscopeSSID, password);
        } else if (Build.VERSION.SDK_INT <= 29) {
            this.connectToWifiAndroid10(appContext, lastKnownOtoscopeSSID, password);
        } else {
            this.connectToWifiAndroidEleven(lastKnownOtoscopeSSID, password, appContext);
        }

        return success;

    }

    public boolean getIsConnectedToSpecificWifi(String ssid, Context context) {
        return this.isConnectedToSpecifiedWifi(ssid, context);
    }

    public String getCurrentSSID(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConnectionInfo().getSSID();
    }


    private boolean isConnectedToSpecifiedWifi(String ssid, Context context) {
        boolean success = false;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (this.wifiManager.getConnectionInfo().getSSID().equals(String.format("\"%s\"", ssid))) {
            success = true;
        }
        return success;
    }

    @RequiresApi(api = 29)
    private void connectToWifiAndroid10(Context context, String ssid, String password) {
        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
        builder.setSsid(ssid);
        builder.setWpa2Passphrase(password);
        WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();
        NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder();
        networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
        networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED);
        networkRequestBuilder.setNetworkSpecifier(wifiNetworkSpecifier);
        NetworkRequest networkRequest = networkRequestBuilder.build();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                public void onAvailable(Network network) {
                    super.onAvailable(network);

                    wci.networkToBind(network);
                }

                public void onLosing(@NonNull Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                }

                public void onLost(Network network) {
                    super.onLost(network);
                }

                public void onUnavailable() {
                    super.onUnavailable();
                }
            };
            connectivityManager.requestNetwork(networkRequest, networkCallback);
            this.wci.callbackToUnbind(networkCallback);
        }

    }


    public NetworkRequest createNetworkRequest(String ssid, String password) {
        WifiNetworkSpecifier.Builder mWifiNetworkSpecifierBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mWifiNetworkSpecifierBuilder = new WifiNetworkSpecifier.Builder();
            mWifiNetworkSpecifierBuilder.setSsid(ssid);
            mWifiNetworkSpecifierBuilder.setWpa2Passphrase(password);
            WifiNetworkSpecifier mWifiNetworkSpecifier = mWifiNetworkSpecifierBuilder.build();
            NetworkRequest.Builder mNetworkRequestBuilder = new NetworkRequest.Builder();
            mNetworkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            mNetworkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
            mNetworkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED);
            mNetworkRequestBuilder.setNetworkSpecifier(mWifiNetworkSpecifier);
            return mNetworkRequestBuilder.build();
        }
        return null;
    }

    ConnectivityManager.NetworkCallback mCallback;


    private void addNetworkCallBack(NetworkRequest mNetworkRequest, String ssid, String password, Context mContext) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectivityManager.NetworkCallback mCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.e("OtoScope_Connection", "onAvailable: " + network);


                currentNetwork = network;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mConnectivityManager.bindProcessToNetwork(network);
                } else {
                    ConnectivityManager.setProcessDefaultNetwork(network);
                }


            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                Log.e("OtoScope_Connection", "onLosing : Wifi Connector Class");
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                mConnectivityManager.unregisterNetworkCallback(this);
                Log.e("OtoScope_Connection", "onLost : Wifi Connector Class : ssid -> " + ssid);

                //unbind
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mConnectivityManager.bindProcessToNetwork(null);
                } else {
                    ConnectivityManager.setProcessDefaultNetwork(null);
                }
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.e("OtoScope_Connection", "onUnavailable: Wifi Connector Class");
            }
        };
        mConnectivityManager.requestNetwork(mNetworkRequest, mCallback);
    }

    private void loopingConnectToWifiAttemptAndroidNineAndTen(final Context context, final String ssid, final String password) {

        (new Thread(new Runnable() {
            public void run() {
                wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                Log.e("ssid: ", wifiManager.getConnectionInfo().getSSID());
                Log.e("ssid: ", wifiManager.getConnectionInfo().getSSID());
                connectToWifiAndroidNineAndTen(context, ssid, password);
            }
        })).start();
    }

    private void connectToWifiAndroidNineAndTen(Context context, String ssid, String password) {
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.wifiManager.disableNetwork(this.wifiManager.getConnectionInfo().getNetworkId());
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = String.format("\"%s\"", ssid);
        conf.preSharedKey = String.format("\"%s\"", password);
        conf.allowedKeyManagement.set(1);
        conf.allowedPairwiseCiphers.set(1);
        conf.allowedPairwiseCiphers.set(2);
        conf.allowedGroupCiphers.set(2);
        conf.allowedGroupCiphers.set(3);
        conf.allowedProtocols.set(1);
        int netId = this.wifiManager.addNetwork(conf);
        this.wifiManager.disconnect();
        this.wifiManager.enableNetwork(netId, true);
        this.wifiManager.reconnect();

    }

    @SuppressLint({"NewApi"})
    @RequiresApi(api = 26)
    private void connectToWifiAndroidEleven(String ssid, String password, Context context) {
        Log.e(TAG, "connectToWifiAndroidNineAndTen: Wifi " + ssid + " | " + ssid);
        NetworkRequest mNetworkRequest = createNetworkRequest(ssid, password);
        addNetworkCallBack(mNetworkRequest, ssid, password, context);

    }




    private void logResults(Context context) {
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Toast.makeText(context, "SSID: " + this.wifiManager.getConnectionInfo().getSSID(), Toast.LENGTH_SHORT).show();
        Log.e("n", this.wifiManager.getConnectionInfo().getSSID());
    }


    //see if Otoscope on network
    public boolean doesOtoscopeApModeExist() {
        boolean found = false;

        if (lastSuccessfulScan !=null) {
            for (ScanResult result : lastSuccessfulScan) {
                // Split SSID by '_'
                String[] parts = result.SSID.split("_");

                // Check if the first part equals "OTOSCOPE"
                if (parts.length > 0 && "OTOSCOPE".equals(parts[0])) {
                    lastKnownOtoscopeSSID = result.SSID;
                    Log.i(TAG,"Found otoscope AP mode wifi:"+lastKnownOtoscopeSSID);
                    found = true;
                    break;
                }
            }
        }
        return found;
    }
    public ArrayList<String> getAvailableSsids() {
        ArrayList<String> availableNetworks = new ArrayList<String>();
        if (lastSuccessfulScan != null) {
            for (int scansIndex = 0; scansIndex < lastSuccessfulScan.size(); scansIndex++) {
                availableNetworks.add(lastSuccessfulScan.get(scansIndex).SSID);
            }
        }
        return availableNetworks;
    }

    public void doInitiateWifiScanner(Context context) {
        initiateWifiScanner(context);
    }

    public void scanWifi() {
        wifiManager.startScan();
    }

    private List<ScanResult> lastSuccessfulScan;

    private void initiateWifiScanner(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
    }
    private final IntentFilter intentFilter = new IntentFilter();
    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context c, Intent intent) {
            boolean resultUpdated = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                resultUpdated = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
            }

            if (resultUpdated) {
                lastSuccessfulScan = wifiManager.getScanResults();
            }
        }
    };




}
