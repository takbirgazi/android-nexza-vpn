package com.nexzatech.nexzavpn.network;

public final class ApiConfig {

    private ApiConfig() {}

    public static final String BASE_URL =
            "https://nexza-vpn-apis.vercel.app/api/v1/";

    public static final int CONNECT_TIMEOUT = 15;
    public static final int READ_TIMEOUT = 30;
}