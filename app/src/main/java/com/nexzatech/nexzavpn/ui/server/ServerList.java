package com.nexzatech.nexzavpn.ui.server;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nexzatech.nexzavpn.R;
import com.nexzatech.nexzavpn.adapter.ServerAdapter;
import com.nexzatech.nexzavpn.data.model.Server;
import com.nexzatech.nexzavpn.data.repository.ServerRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServerList extends AppCompatActivity {

    private ListView serverList;
    private ImageView btnBack;
    private EditText etSearch;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());

    private Runnable searchRunnable;

    private ServerAdapter serverAdapter;

    private final List<Server> servers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_server_list);

        // Edge-to-edge system bar handling
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Initialize views
        serverList = findViewById(R.id.serverList);
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Initialize Adapter
        serverAdapter = new ServerAdapter(
                ServerList.this,
                servers
        );

        serverList.setAdapter(serverAdapter);

        // Load servers from API
        serverAdapter = new ServerAdapter(this, servers);
        serverList.setAdapter(serverAdapter);

        loadServers("");
        setupSearch();
    }

    private void loadServers() {

        ServerRepository repository = new ServerRepository();

        new Thread(() -> {

            try {

                JSONArray serverArray = repository.getServers(
                        1,
                        10,
                        ""
                );

                List<Server> apiServers = new ArrayList<>();

                // Convert JSON Array to Server Model
                for (int i = 0; i < serverArray.length(); i++) {

                    JSONObject object = serverArray.getJSONObject(i);

                    String id = object.getString("id");

                    String name = object.getString("name");

                    String country = object.getString("country");

                    Server server = new Server(
                            id,
                            name,
                            country
                    );

                    apiServers.add(server);
                }

                // Update UI on Main Thread
                runOnUiThread(() -> {

                    servers.clear();

                    servers.addAll(apiServers);

                    serverAdapter.notifyDataSetChanged();

                });

            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() -> {

                    Toast.makeText(
                            ServerList.this,
                            "Failed to load servers",
                            Toast.LENGTH_SHORT
                    ).show();

                });
            }

        }).start();
    }



    private void setupSearch() {

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                String query = s.toString().trim();

                searchRunnable = () -> loadServers(query);

                // টাইপিং থামার 500ms পর API call
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadServers(String search) {

        ServerRepository repository = new ServerRepository();

        new Thread(() -> {
            try {

                JSONArray serverArray = repository.getServers(
                        1,
                        10,
                        search
                );

                List<Server> apiServers = new ArrayList<>();

                for (int i = 0; i < serverArray.length(); i++) {

                    JSONObject object = serverArray.getJSONObject(i);

                    Server server = new Server(
                            object.getString("id"),
                            object.getString("name"),
                            object.getString("country")
                    );

                    apiServers.add(server);
                }

                runOnUiThread(() -> {
                    servers.clear();
                    servers.addAll(apiServers);
                    serverAdapter.notifyDataSetChanged();
                });

            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() ->
                        Toast.makeText(
                                ServerList.this,
                                "Failed to load servers",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }

        }).start();
    }
}