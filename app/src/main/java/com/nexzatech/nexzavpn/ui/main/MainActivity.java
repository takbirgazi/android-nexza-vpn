
package com.nexzatech.nexzavpn.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nexzatech.nexzavpn.R;
import com.nexzatech.nexzavpn.data.repository.ServerRepository;
import com.nexzatech.nexzavpn.ui.server.ServerList;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import ai.bongotech.bongovpn.BongoVpn;

public class MainActivity extends AppCompatActivity {

    private FrameLayout powerButton;

    private TextView tvConnectHint, tvStatus, tvStatusMessage, tvDownload, tvUpload, tvTotalUpload, tvTotalDownload, defaultServerName, tvServerName;

    private LinearLayout autoSelect;

    private BongoVpn bongoVpn;

    private boolean isConnected = false;
    private boolean isServerReady = false;
    private boolean isLoadingServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Initialize VPN SDK after Activity is created.
        bongoVpn = new BongoVpn(this);

        // Initialize views.
        initializeViews();

        // Set initial UI state.
        setDisconnectedUI();
        setLoadingUI();

        // Register VPN listener before loading server configuration.
        setupVpnListener();

        // Request notification permission if required.
        if (!bongoVpn.hasNotificationPermission()) {
            bongoVpn.requestNotificationPermission();
        }

        // Setup notification.
        bongoVpn.showNotification()
                .showSpeed(true)
                .targetActivity(MainActivity.class)
                .smallIcon(R.drawable.ic_power)
                .title("Nexza VPN");

        // Setup click listeners.
        setupClickListeners();

        // Load server configuration.
        loadServer("1");
    }

    private void initializeViews() {

        powerButton = findViewById(R.id.btnConnect);

        tvConnectHint = findViewById(R.id.tvConnectHint);
        tvStatus = findViewById(R.id.tvStatus);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);

        tvDownload = findViewById(R.id.tvDownload);
        tvUpload = findViewById(R.id.tvUpload);

        tvTotalDownload = findViewById(R.id.tvTotalDownload);
        tvTotalUpload = findViewById(R.id.tvTotalUpload);

        autoSelect = findViewById(R.id.autoSelect);
        defaultServerName = findViewById(R.id.defaultServerName);
        tvServerName = findViewById(R.id.tvServerName);
    }

    private void setupClickListeners() {

        powerButton.setOnClickListener(view -> {

            if (!isServerReady) {
                Toast.makeText(
                        MainActivity.this,
                        "Please wait while the server loads",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (isConnected) {
                bongoVpn.stopVpn();
            } else {
                connectVpn();
            }
        });

        autoSelect.setOnClickListener(view -> {
            if (isConnected) {
                Toast.makeText(
                        MainActivity.this,
                        "Disconnect VPN before changing servers",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            startActivity(
                    new Intent(MainActivity.this, ServerList.class)
            );
        });
    }

    private void loadServer(String serverId) {

        isLoadingServer = true;
        isServerReady = false;

        powerButton.setEnabled(false);
        tvConnectHint.setText("Loading server...");

        ServerRepository repository = new ServerRepository();

        new Thread(() -> {
            try {
                JSONObject data = repository.getServerById(serverId);

                String encodedConfig = data.getString("vpn_config");

                String config = new String(
                        Base64.decode(encodedConfig, Base64.DEFAULT),
                        StandardCharsets.UTF_8
                );
                String username = data.getString("username");
                String password = data.getString("password");
                String serverName = data.getString("name");
                String serverCountry = data.getString("country");

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    try {
                        bongoVpn.attachFromString(
                                config,
                                username,
                                password
                        );

                        isServerReady = true;
                        isLoadingServer = false;

                        powerButton.setEnabled(true);
                        tvConnectHint.setText("Tap to connect");
                        defaultServerName.setText(serverName);
                        tvServerName.setText(serverCountry);

                    } catch (Exception e) {
                        isServerReady = false;
                        isLoadingServer = false;

                        powerButton.setEnabled(false);
                        tvConnectHint.setText("Server unavailable");

                        Toast.makeText(
                                MainActivity.this,
                                "Unable to prepare VPN server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    isServerReady = false;
                    isLoadingServer = false;

                    powerButton.setEnabled(false);
                    tvConnectHint.setText("Failed to load server");

                    Toast.makeText(
                            MainActivity.this,
                            "Failed to load server details",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }
        }).start();
    }

    private void setupVpnListener() {

        bongoVpn.setVpnListener(new BongoVpn.VpnListener() {

            @Override
            public void onVpnConnected() {

                isConnected = true;

                tvConnectHint.setText("Tap to disconnect");
                tvStatus.setText("Connected");
                tvStatus.setTextColor(
                        Color.parseColor("#44D35A")
                );
                tvStatusMessage.setText(
                        "Now your connection is protected"
                );
            }

            @Override
            public void onVpnStopped() {

                isConnected = false;

                tvConnectHint.setText(
                        isServerReady
                                ? "Tap to connect"
                                : "Server unavailable"
                );

                tvStatus.setText("Not Connected");
                tvStatus.setTextColor(
                        Color.parseColor("#F87171")
                );

                tvStatusMessage.setText(
                        "Your connection is not protected"
                );
            }

            @Override
            public void onStatusUpdate(String status) {

                if (status == null || status.trim().isEmpty()) {
                    return;
                }

                String[] parts = status.split("_");
                StringBuilder builder = new StringBuilder();

                for (String part : parts) {
                    if (!part.isEmpty()) {
                        String lower = part.toLowerCase();
                        String capitalized =
                                lower.substring(0, 1).toUpperCase()
                                        + lower.substring(1);

                        if (builder.length() > 0) {
                            builder.append(" ");
                        }

                        builder.append(capitalized);
                    }
                }

                tvStatusMessage.setText(builder.toString());
            }

            @Override
            public void onError(String errorMessage) {

                isConnected = false;

                tvConnectHint.setText(
                        isServerReady
                                ? "Tap to connect"
                                : "Server unavailable"
                );

                tvStatus.setText("Not Connected");
                tvStatus.setTextColor(
                        Color.parseColor("#F87171")
                );

                tvStatusMessage.setText(
                        "Connection failed. Please try again."
                );
            }

            @Override
            public void onSpeedUpdate(
                    long downloadBytes,
                    long uploadBytes,
                    long downloadSpeed,
                    long uploadSpeed
            ) {

                // Current download/upload speeds.
                tvDownload.setText(
                        bongoVpn.formatSpeed(downloadSpeed)
                );

                tvUpload.setText(
                        bongoVpn.formatSpeed(uploadSpeed)
                );

                // Total downloaded/uploaded data.
                tvTotalDownload.setText(
                        bongoVpn.formatBytes(downloadBytes)
                );

                tvTotalUpload.setText(
                        bongoVpn.formatBytes(uploadBytes)
                );
            }
        });
    }

    private void connectVpn() {

        if (!isServerReady || isLoadingServer) {
            Toast.makeText(
                    this,
                    "Server is not ready yet",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (bongoVpn.hasVpnPermission()) {
            bongoVpn.startVpn();
        } else {
            bongoVpn.requestVpnPermission();
        }
    }

    private void setDisconnectedUI() {

        tvStatus.setText("Not Connected");
        tvStatus.setTextColor(
                Color.parseColor("#F87171")
        );

        tvStatusMessage.setText(
                "Your connection is not protected"
        );
    }

    private void setLoadingUI() {

        powerButton.setEnabled(false);
        tvConnectHint.setText("Loading server...");
    }

    @Override
    protected void onDestroy() {

        if (bongoVpn != null) {
            bongoVpn.release();
        }

        super.onDestroy();
    }
}
