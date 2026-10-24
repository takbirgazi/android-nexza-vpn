package com.nexzatech.nexzavpn.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class ServerPreferences {

    private static final String PREF_NAME = "nexza_vpn_preferences";
    private static final String KEY_SELECTED_SERVER_ID = "selected_server_id";

    private final SharedPreferences preferences;

    public ServerPreferences(Context context) {
        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void saveSelectedServerId(String serverId) {
        preferences.edit()
                .putString(KEY_SELECTED_SERVER_ID, serverId)
                .apply();
    }

    public String getSelectedServerId() {
        return preferences.getString(KEY_SELECTED_SERVER_ID, null);
    }
}