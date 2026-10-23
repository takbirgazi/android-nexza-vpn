package com.nexzatech.nexzavpn.network;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CookieManagerProvider implements CookieJar {

    private static final String PREF_NAME = "api_cookies";
    private static final String COOKIE_KEY = "cookies";

    private final SharedPreferences preferences;
    private final List<Cookie> cookies = new ArrayList<>();

    public CookieManagerProvider(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        loadCookies();
    }

    @Override
    public synchronized void saveFromResponse(
            HttpUrl url,
            List<Cookie> newCookies
    ) {
        long now = System.currentTimeMillis();

        cookies.removeIf(cookie ->
                cookie.expiresAt() <= now
        );

        for (Cookie newCookie : newCookies) {
            cookies.removeIf(oldCookie ->
                    oldCookie.name().equals(newCookie.name())
                            && oldCookie.domain().equals(newCookie.domain())
                            && oldCookie.path().equals(newCookie.path())
            );

            if (newCookie.expiresAt() > now) {
                cookies.add(newCookie);
            }
        }

        persistCookies();
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        long now = System.currentTimeMillis();

        cookies.removeIf(cookie ->
                cookie.expiresAt() <= now
        );

        persistCookies();

        List<Cookie> result = new ArrayList<>();

        for (Cookie cookie : cookies) {
            if (cookie.matches(url)) {
                result.add(cookie);
            }
        }

        return result;
    }

    private void persistCookies() {
        JSONArray array = new JSONArray();

        try {
            for (Cookie cookie : cookies) {
                JSONObject item = new JSONObject();

                item.put("url", cookie.secure()
                        ? "https://" + cookie.domain()
                        : "http://" + cookie.domain());

                item.put("cookie", cookie.toString());

                array.put(item);
            }

            preferences.edit()
                    .putString(COOKIE_KEY, array.toString())
                    .apply();

        } catch (Exception ignored) {
            // Production app: log through a safe logging system.
        }
    }

    private void loadCookies() {
        String stored = preferences.getString(COOKIE_KEY, "[]");

        try {
            JSONArray array = new JSONArray(stored);

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);

                HttpUrl url = HttpUrl.parse(item.getString("url"));

                if (url == null) continue;

                Cookie cookie = Cookie.parse(
                        url,
                        item.getString("cookie")
                );

                if (cookie != null
                        && cookie.expiresAt() > System.currentTimeMillis()) {
                    cookies.add(cookie);
                }
            }
        } catch (Exception ignored) {
            cookies.clear();
        }
    }

    public synchronized void clearCookies() {
        cookies.clear();

        preferences.edit()
                .remove(COOKIE_KEY)
                .apply();
    }
}