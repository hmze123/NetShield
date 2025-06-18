package com.fdx.injector.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import android.content.SharedPreferences;
import android.util.Base64;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.os.Process;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.fdx.injector.wifi.MainActivityWifi;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import com.fdx.injector.R;
import java.util.concurrent.ExecutionException;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import android.text.Html;
import android.graphics.Color;
import com.fdx.injector.coreservice.tunnel.TunnelUtils;
import androidx.appcompat.widget.Toolbar;

public class MainActivityWifi extends AppCompatActivity {
	private EditText portEditText;
	private Button start, stop, restart, hdwifi, wifiTetherButton;
	private TextView proxyStatusTextView, proxyURLTextView;
	private SharedPreferences sp;
    private Toolbar mToolbar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_wifi);
		androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_main);
        mToolbar.setSubtitle("Share VPN Connection");
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        
       
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    sp = getSharedPreferences("Wifi_Tethering",Context.MODE_PRIVATE);
		initializeViews();
		initializeListeners();


	}
    
    
    

	private void initializeListeners() {
	
        wifiTetherButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				launchHotspotSettings();
			}
		});
		restart.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				restartapp();
			}
		});
		hdwifi.setOnClickListener(new View.OnClickListener(){

                private AlertDialog.Builder ab;
                @Override
                public void onClick(View v){
                    ab = new AlertDialog.Builder(MainActivityWifi.this);
                    ab.setTitle(Html.fromHtml("How to Broadcast Wifi Via Proxy"));
                    ab.setMessage(Html.fromHtml("</strong> " + "1. Connect to VPN ,Select the Wifi Broadcast ( Proxy ) Item on the App<br>2. Enable Wifi Broadcasting On Your Device Or Hotspot <br>3. You Enter the Port Like  After :<br>For Socket Tunnel Plus ( SSH) Is <font color=#f70217>1080 , 8080</font> \nFor V2Ray , V2FlyNG Is <font color=#f70217>10809</font><br>4  Click Start , Request 4G VPN Connected , Wifi Broadcast Enabled You will see Line <font color=#f70217>192.168.xx.x</font>: Port Entered)<br>5. On Starter  You Connect That Wifi , Select Proxy Item ( In No State ) , Select Manually<br>6. Enter IP <font color=#f70217>192.x.x.x</font> ( Server Name , Server) ,  Enter Port Then Save And Reconnect Wifi<br>7. If <font color=#f70217>Error</font> , Port Busy Please Force Stop App . Wish You Success"+"</strong>"  ));
                    ab.setPositiveButton(Html.fromHtml("Okay"), null);
                    ab.create().show();
               }

          
                
            });
        start.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				start();
			}
		});
		stop.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				stop();
			}
		});
	}
	
	private void start(){
		if (!portEditText.getText().toString().matches("\\d+")) {
					proxyStatusTextView.setText(getString(R.string.enter_the_port));
					proxyURLTextView.setText("");
					return;
				}
				int port = Integer.parseInt(portEditText.getText().toString());
				String ip = getIPAddress(true);
				if (!ip.trim().startsWith("192.")) {
					proxyStatusTextView.setText(getString(R.string.turn_on_tethering));
					proxyURLTextView.setText(getString(R.string.connect_wifi));
					return;
				}
				try {
					if (!(new CheckingPortTask().execute(port).get())) {
						proxyStatusTextView.setText(getString(R.string.busy_port));
						proxyURLTextView.setText(getString(R.string.enter_another_port));
						restart.setVisibility(View.VISIBLE);
						return;
					}
				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
					proxyStatusTextView.setText(getString(R.string.errors));
					proxyURLTextView.setText("");
					return;
				}
				Intent intent = new Intent(MainActivityWifi.this, ProxyService.class);
				intent.putExtra("port", port);
				sp.edit().putString("port",portEditText.getText().toString()).apply();
				startService(intent);
				proxyStatusTextView.setText(getString(R.string.proxy_is_running));
				proxyURLTextView.setText(String.format("%s:%d", getIPAddress(true), port));
				start.setEnabled(false);
				stop.setEnabled(true);
				portEditText.setEnabled(false);
				
	}
	private void stop(){
		stopService(new Intent(MainActivityWifi.this, ProxyService.class));
		proxyStatusTextView.setText(getString(R.string.proxy_stopped));
		proxyURLTextView.setText("");
		if (isProxyServiceRunning(ProxyService.class)) {
			start.setEnabled(false);
			stop.setEnabled(true);
			portEditText.setEnabled(false);
			//startandstop.setText(R.string.stop_server);
		} else {
			start.setEnabled(true);
			stop.setEnabled(false);
			//startandstop.setText(R.string.start_server);
			//Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
		}
		portEditText.setEnabled(true);
		
	}
	private void initializeViews() {
		
		portEditText = findViewById(R.id.portEditText);
		start = findViewById(R.id.start);
		stop = findViewById(R.id.stop);
		wifiTetherButton = findViewById(R.id.WiFiTetherButton);
		proxyStatusTextView = findViewById(R.id.proxyStatus);
		proxyURLTextView = findViewById(R.id.proxyURL);
	hdwifi = findViewById(R.id.hdwifi);
		restart = findViewById(R.id.restart);
		if (isProxyServiceRunning(ProxyService.class)) {
			start.setEnabled(false);
			stop.setEnabled(true);
			portEditText.setEnabled(false);
			//startandstop.setText(R.string.stop_server);
		} else {
			start.setEnabled(true);
			stop.setEnabled(false);
			//startandstop.setText(R.string.start_server);
			//Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
		}
		portEditText.setText(sp.getString("port","8080"));
	}

	public void restartapp() {
		Intent intent = new Intent(this, MainActivityWifi.class);
		int i = 123456;
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_CANCEL_CURRENT | FLAG_IMMUTABLE);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + ((long) 2000), pendingIntent);
		System.runFinalizersOnExit(true);
		System.exit(0);
		Process.killProcess(Process.myPid());
	}
	public void stopp(){
		stop();
		System.runFinalizersOnExit(true);
		System.exit(0);
		Process.killProcess(Process.myPid());
	
	}
	public String getIPAddress(boolean useIPv4) {
		try {
			boolean isIPv4;
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress();
						isIPv4 = sAddr.indexOf(':') < 0;

						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
								return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
							}
						}
					}
				}
			}
		} catch (Exception ignored) {
		} // for now eat exceptions
		return "";
	}

	private void launchHotspotSettings() {
		Intent tetherSettings = new Intent();
		tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
		startActivity(tetherSettings);
	}

	private boolean isProxyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private class CheckingPortTask extends AsyncTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... port) {
			try {
				ServerSocket serverSocket = new ServerSocket(port[0]);
				serverSocket.close();
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

}

