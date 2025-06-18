package com.fdx.injector.ActivityTools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.stealthcopter.networktools.ARPInfo;
import com.stealthcopter.networktools.IPTools;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.PortScan;
import com.stealthcopter.networktools.SubnetDevices;
import com.stealthcopter.networktools.WakeOnLan;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;
import com.stealthcopter.networktools.subnet.Device;
import android.graphics.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.fdx.injector.R;

public class ping extends AppCompatActivity {

    private TextView resultText;
    private EditText editIpAddress;
    private ScrollView scrollView;
    private Button pingButton;
    private Button wolButton;
    private Button portScanButton;
    private Button subnetDevicesButton;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
        androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_main);
        mToolbar.setSubtitle("PING");
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        resultText = findViewById(R.id.resultText);
        editIpAddress = findViewById(R.id.editIpAddress);
        scrollView = findViewById(R.id.scrollView1);
        pingButton = findViewById(R.id.pingButton);

        InetAddress ipAddress = IPTools.getLocalIPv4Address();
        if (ipAddress != null){
            editIpAddress.setText(ipAddress.getHostAddress());
        }

        findViewById(R.id.pingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doPing();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });


    }

    private void appendResultsText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultText.append(text + "\n");
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    private void setEnabled(final View view, final boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (view != null) {
                    view.setEnabled(enabled);
                }
            }
        });
    }

    private void doPing() throws Exception {
        String ipAddress = editIpAddress.getText().toString();

        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText("Invalid Ip Address");
            return;
        }

        setEnabled(pingButton, false);

        // Perform a single synchronous ping
        PingResult pingResult = null;
        try {
            pingResult = Ping.onAddress(ipAddress).setTimeOutMillis(1000).doPing();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            appendResultsText(e.getMessage());
            setEnabled(pingButton, true);
            return;
        }


        appendResultsText("Pinging Address: " + pingResult.getAddress().getHostAddress());
        appendResultsText("HostName: " + pingResult.getAddress().getHostName());
        appendResultsText(String.format("%.2f ms", pingResult.getTimeTaken()));


        // Perform an asynchronous ping
        Ping.onAddress(ipAddress).setTimeOutMillis(1000).setTimes(5).doPing(new Ping.PingListener() {
            @Override
            public void onResult(PingResult pingResult) {
                if (pingResult.isReachable) {
                    appendResultsText(String.format("%.2f ms", pingResult.getTimeTaken()));
                } else {
                    appendResultsText(getString(R.string.timeout));
                }
            }

            @Override
            public void onFinished(PingStats pingStats) {
                appendResultsText(String.format("Pings: %d, Packets lost: %d",
                        pingStats.getNoPings(), pingStats.getPacketsLost()));
                appendResultsText(String.format("Min/Avg/Max Time: %.2f/%.2f/%.2f ms",
                        pingStats.getMinTimeTaken(), pingStats.getAverageTimeTaken(), pingStats.getMaxTimeTaken()));
                setEnabled(pingButton, true);
            }

            @Override
            public void onError(Exception e) {
                // TODO: STUB METHOD
                setEnabled(pingButton, true);
            }
        });

    }

    private void doWakeOnLan() throws IllegalArgumentException {
        String ipAddress = editIpAddress.getText().toString();

        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText("Invalid Ip Address");
            return;
        }

        setEnabled(wolButton, false);

        appendResultsText("IP address: " + ipAddress);

        // Get mac address from IP (using arp cache)
        String macAddress = ARPInfo.getMACFromIPAddress(ipAddress);

        if (macAddress == null) {
            appendResultsText("Could not fromIPAddress MAC address, cannot send WOL packet without it.");
            setEnabled(wolButton, true);
            return;
        }

        appendResultsText("MAC address: " + macAddress);
        appendResultsText("IP address2: " + ARPInfo.getIPAddressFromMAC(macAddress));

        // Send Wake on lan packed to ip/mac
        try {
            WakeOnLan.sendWakeOnLan(ipAddress, macAddress);
            appendResultsText("WOL Packet sent");
        } catch (IOException e) {
            appendResultsText(e.getMessage());
            e.printStackTrace();
        } finally {
            setEnabled(wolButton, true);
        }
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }



}
