package com.nexzatech.nexzavpn;

import android.app.Application;

import com.nexzatech.nexzavpn.network.ApiClient;

public class NexzaVpnApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ApiClient.initialize(this);
    }
}