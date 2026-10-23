package com.nexzatech.nexzavpn.network;

import android.content.Context;

import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class ApiClient {

    private static OkHttpClient client;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ApiClient() {}

    public static synchronized void initialize(Context context) {
        if (client != null) return;

        CookieJar cookieJar =
                new CookieManagerProvider(context.getApplicationContext());

        client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .connectTimeout(
                        ApiConfig.CONNECT_TIMEOUT,
                        TimeUnit.SECONDS
                )
                .readTimeout(
                        ApiConfig.READ_TIMEOUT,
                        TimeUnit.SECONDS
                )
                .build();
    }

    public static OkHttpClient getClient() {
        if (client == null) {
            throw new IllegalStateException(
                    "Call ApiClient.initialize() first"
            );
        }

        return client;
    }

    public static String get(String path) throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.BASE_URL + path)
                .get()
                .build();

        return execute(request);
    }

    public static String post(String path, String json)
            throws IOException {

        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(ApiConfig.BASE_URL + path)
                .post(body)
                .build();

        return execute(request);
    }

    public static String put(String path, String json)
            throws IOException {

        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(ApiConfig.BASE_URL + path)
                .put(body)
                .build();

        return execute(request);
    }

    public static String patch(String path, String json)
            throws IOException {

        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(ApiConfig.BASE_URL + path)
                .patch(body)
                .build();

        return execute(request);
    }

    public static String delete(String path) throws IOException {
        Request request = new Request.Builder()
                .url(ApiConfig.BASE_URL + path)
                .delete()
                .build();

        return execute(request);
    }

    private static String execute(Request request)
            throws IOException {

        try (Response response = getClient()
                .newCall(request)
                .execute()) {

            String body = response.body() != null
                    ? response.body().string()
                    : "";

            if (!response.isSuccessful()) {
                throw new IOException(
                        "HTTP " + response.code() + ": " + body
                );
            }

            return body;
        }
    }
}