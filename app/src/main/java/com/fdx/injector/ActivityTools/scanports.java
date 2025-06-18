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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import android.graphics.Color;
import com.fdx.injector.R;

public class scanports extends AppCompatActivity {

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
        setContentView(R.layout.activity_ports);
        androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_main);
        mToolbar.setSubtitle("SCAN PORTS");
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        resultText = findViewById(R.id.resultText);
        editIpAddress = findViewById(R.id.editIpAddress);
        scrollView = findViewById(R.id.scrollView1);

        portScanButton = findViewById(R.id.portScanButton);

        InetAddress ipAddress = IPTools.getLocalIPv4Address();
        if (ipAddress != null){
            editIpAddress.setText(ipAddress.getHostAddress());
        }




        findViewById(R.id.portScanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doPortScan();
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




    private void doPortScan() throws Exception {
        String ipAddress = editIpAddress.getText().toString();

        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText("Invalid Ip Address");
            setEnabled(portScanButton, true);
            return;
        }

        setEnabled(portScanButton, false);

        // Perform synchronous port scan
        appendResultsText("PortScanning IP: " + ipAddress);
        ArrayList<Integer> openPorts = PortScan.onAddress(ipAddress).setPort(21).setMethodTCP().doScan();

        final long startTimeMillis = System.currentTimeMillis();

        // Perform an asynchronous port scan
        PortScan portScan = PortScan.onAddress(ipAddress).setPortsAll().setMethodTCP().doScan(new PortScan.PortListener() {
            @Override
            public void onResult(int portNo, boolean open) {
                if (open) appendResultsText("Open: " + portNo);
            }

            @Override
            public void onFinished(ArrayList<Integer> openPorts) {
                appendResultsText("Open Ports: " + openPorts.size());
                appendResultsText("Time Taken: " + ((System.currentTimeMillis() - startTimeMillis)/1000.0f));
                setEnabled(portScanButton, true);
            }
        });

        // Below is example of how to cancel a running scan
        // portScan.cancel();
    }







    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

}
