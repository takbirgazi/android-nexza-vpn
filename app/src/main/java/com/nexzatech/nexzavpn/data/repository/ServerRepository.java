package com.nexzatech.nexzavpn.data.repository;

import com.nexzatech.nexzavpn.network.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class ServerRepository {

    // Get All Servers
    public JSONArray getServers(int page, int limit, String search)
            throws Exception {

        String path = "server/all?page=" + page
                + "&limit=" + limit;

        if (search != null && !search.trim().isEmpty()) {
            path += "&search=" +
                    java.net.URLEncoder.encode(
                            search.trim(),
                            "UTF-8"
                    );
        }

        String response = ApiClient.get(path);

        JSONObject json = new JSONObject(response);

        if (!json.optBoolean("success")) {
            throw new IOException(
                    json.optString("message", "Failed to load servers")
            );
        }

        return json.getJSONArray("data");
    }

    // Get Single Server
    public JSONObject getServerById(String id) throws Exception {

        String response = ApiClient.get("server/" + id);

        JSONObject jsonObject = new JSONObject(response);

        if (!jsonObject.getBoolean("success")) {
            throw new Exception(
                    jsonObject.optString(
                            "message",
                            "Failed to load server"
                    )
            );
        }

        return jsonObject.getJSONObject("data");
    }




    // END
}