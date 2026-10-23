package com.nexzatech.nexzavpn;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.data.DefaultVPN;
import com.data.VPNData;

import ai.bongotech.bongovpn.BongoVpn;

public class MainActivity extends AppCompatActivity {

    FrameLayout powerButton;
    TextView tvConnectHint, tvStatus, tvStatusMessage, tvDownload, tvUpload, tvInfo, tvTotalUpload, tvTotalDownload;
    Boolean isConnected = false;
    LinearLayout autoSelect;
    BongoVpn bongoVpn = new BongoVpn(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        powerButton = findViewById(R.id.btnConnect);
        tvConnectHint = findViewById(R.id.tvConnectHint);
        tvStatus = findViewById(R.id.tvStatus);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        tvDownload = findViewById(R.id.tvDownload);
        tvUpload = findViewById(R.id.tvUpload);
        tvTotalDownload =findViewById(R.id.tvTotalDownload);
        tvTotalUpload =  findViewById(R.id. tvTotalUpload);
        autoSelect = findViewById(R.id.autoSelect);


        if(!(bongoVpn.hasNotificationPermission())){
            bongoVpn.requestNotificationPermission();
        }

        DefaultVPN defaults = new DefaultVPN();
        VPNData data = defaults.getJapanVpn();

        String config = data.config;
        String username = data.username;
        String password = data.password;

        bongoVpn.attachFromString(config, username, password);

        // VPN Start Button
        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected){
                    bongoVpn.stopVpn();
                }else {
                    connectVpn();
                }
            }
        });

        // Auto Select Button
        autoSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(MainActivity.this, ServerList.class));
            }
        });

        bongoVpn.setVpnListener(new BongoVpn.VpnListener() {
            @Override
            public void onVpnConnected() {
                isConnected = true;
                tvConnectHint.setText("Tap to Disconnect!");
                tvStatus.setText("Connected");
                tvStatus.setTextColor(Color.parseColor("#44D35A"));
                tvStatusMessage.setText("Now your connection is protected");
            }

            @Override
            public void onVpnStopped() {
                isConnected = false;
                tvConnectHint.setText("Tap to connect");
                tvStatus.setText("Not Connected");
                tvStatus.setTextColor(Color.parseColor("#F87171"));
                tvStatusMessage.setText("Your connection is not protected");

            }

            @Override
            public void onStatusUpdate(String status) {

                String[] text = status.split("_");
                StringBuilder updateTextBuilder = new StringBuilder();

                for (String newText : text) {
                    if (!newText.isEmpty()) {
                        String lower = newText.toLowerCase();
                        String capitalized = lower.substring(0, 1).toUpperCase() + lower.substring(1);
                        updateTextBuilder.append(capitalized).append(" ");
                    }
                }
                String updateText = updateTextBuilder.toString().trim();
                tvStatusMessage.setText(updateText);
            }

            @Override
            public void onError(String errorMessage) {
                isConnected = false;
                tvConnectHint.setText("Tap to connect");
                tvStatus.setText("Not Connected");
                tvStatus.setTextColor(Color.parseColor("#F87171"));
                tvStatusMessage.setText("Your connection is not protected");
            }

            @Override
            public void onSpeedUpdate(long downloadBytes, long uploadBytes, long downloadSpeed, long uploadSpeed) {

                String uploadB = bongoVpn.formatSpeed(uploadSpeed);
                String downB = bongoVpn.formatSpeed(downloadSpeed);
                String totalUp = bongoVpn.formatBytes(uploadBytes);
                String totalDown = bongoVpn.formatBytes(downloadBytes);
                // == Speed and Data Count ===
                tvUpload.setText(uploadB);
                tvDownload.setText(downB);
                tvTotalUpload.setText(totalUp);
                tvTotalDownload.setText(totalDown);


            }

        });

        bongoVpn.showNotification()
                .showSpeed(true)
                .targetActivity(MainActivity.class)
                .smallIcon(R.drawable.ic_power)
                .title("Nexza VPN");
    }

    // ===== Connect VPN Method Start =====
    private void connectVpn(){
        isConnected = false;
        if(bongoVpn.hasVpnPermission()){
            bongoVpn.startVpn();
        }else {
            bongoVpn.requestVpnPermission();
        }
    }
    // ===== Connect VPN Method End =====


    @Override
    protected void onDestroy() {
        if(bongoVpn !=  null){
            bongoVpn.release();
        }
        super.onDestroy();
    }


}