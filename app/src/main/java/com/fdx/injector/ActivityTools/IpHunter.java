package com.fdx.injector.ActivityTools;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.ListView;
import com.google.android.material.textview.MaterialTextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.fdx.injector.R;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.http.conn.util.InetAddressUtils;
import java.util.List;
import com.fdx.injector.coreservice.tunnel.TunnelUtils;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class IpHunter extends AppCompatActivity
{


    private MaterialTextView xIp;
    private ListView xLv;
    private ArrayList<String> xLItems;
    private ArrayAdapter<String> xAdapter;
    private TextInputEditText xEdt;
    private ToggleButton toggle;
    private Toolbar mToolbar;

    

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iphunter);
        androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_main);
        mToolbar.setSubtitle("IP Hunter");
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        xIp = (MaterialTextView)findViewById(R.id.ip);

        xEdt =(TextInputEditText)findViewById(R.id.edt);
        
        

        xLv = (ListView) findViewById(R.id.lv);
        xLItems = new ArrayList<String>();
      //  String a = new String(new byte[]{65,76,72,65,77,90,65,118,112,110,45,73,80,32,72,117,110,116,101,114});
      //  xLItems.add(a);
        xAdapter = new ArrayAdapter<String>(this,
                                            R.layout.list_text,xLItems);
        xLv.setAdapter(xAdapter);


        toggle = (ToggleButton) findViewById(R.id.toggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    xAdapter.notifyDataSetChanged();
                    if (isChecked) {
                        // The toggle is enabled
                        xEdt.setEnabled(false); 

                        if(xIp.getText().toString().startsWith(xEdt.getText().toString())){
                            xLItems.add("Starting...");
                            xLItems.add("Current IP: "+xIp.getText().toString());
                            xLItems.add("Success  Found IP: "+xIp.getText().toString());
                            xLItems.add("Stopped");
                            xEdt.setEnabled(true);
                            Toast.makeText(getApplicationContext(),"Founded IP:"+xIp.getText().toString(),Toast.LENGTH_LONG).show();
                            toggle.setChecked(false);

                            xEdt.setEnabled(true);
                        }else{
                            xLItems.add("Starting...");
                            xLItems.add("Current IP: "+xIp.getText().toString());
                            xLItems.add("Please Turn Airplane MODE OFF/ON!");

                        }

                    } else {
                        // The toggle is disabled
                        xLItems.add("Stopped");
                        xEdt.setEnabled(true);
                    }
                }
            });

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    xIp.setText(getIpAddress());
                                    String  s2 = xEdt.getText().toString();
                                    if(s2.isEmpty()||s2.isEmpty()){
                                        toggle.setEnabled(false);
                                    }else {
                                        toggle.setEnabled(true);
                                    }
                                }
                            });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log_mnu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mclearLog:
                xAdapter.clear();
                Toast.makeText(getApplicationContext(),"Log Cleared",Toast.LENGTH_LONG).show();
                return true;   
        }
        return false;
    }

    public static String getIpAddress() {
        String xHostIfAvailable = "127.0.0.1";
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (isIPv4) 
                            return sAddr;
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return xHostIfAvailable;
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

}