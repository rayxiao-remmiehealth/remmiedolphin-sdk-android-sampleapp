package com.remmiehealth.remmietestappforsdk;

import android.net.ConnectivityManager;
import android.net.Network;

public interface
WifiConnectionInterface {
    public void networkToBind(Network network);
    public void callbackToUnbind(ConnectivityManager.NetworkCallback networkCallback);
}
