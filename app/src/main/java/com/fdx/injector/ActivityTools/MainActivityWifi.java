package com.fdx.injector.ActivityTools;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fdx.injector.R;
import com.fdx.injector.ActivityTools.IProxyControl;
import com.fdx.injector.wifi.ProxyService;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class MainActivityWifi extends AppCompatActivity
implements OnClickListener, ServiceConnection, OnCheckedChangeListener {

	public static final String TAG = "ProxySettings";
	protected static final String KEY_PREFS = "proxy_pref";
	protected static final String KEY_ENABALE = "proxy_enable";
	private static final String NOTIFICATION_ID = "Tri-Net Socks";
	private IProxyControl proxyControl = null;
	private TextView tvInfo;
	private Switch cbEnable;
	private TextView checkIP;
	private SharedPreferences sp;
	private SharedPreferences.Editor edit;
	private Button openhotspot;
	private Notification.Builder notification;
	private NotificationManager notificationManager;

	private com.google.android.material.appbar.MaterialToolbar tb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//TopExceptionHandler.init(getApplicationContext());
		super.onCreate(savedInstanceState);

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification.Builder(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notification.setChannelId(getPackageName() + ".SHV");
			createNotification(notificationManager, getPackageName() + ".SHV");
		}

		sp = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		edit = sp.edit();
		setContentView(R.layout.wifi_share);
		androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_main);
       // mToolbar.setSubtitle("Host Checker");
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		tvInfo = (TextView) findViewById(R.id.tv_info);

		cbEnable = (Switch) findViewById(R.id.cb_enable);
		cbEnable.setOnCheckedChangeListener(this);
		cbEnable.setPadding(20, 0, 0, 0);
		cbEnable.setText(sp.getBoolean(KEY_ENABALE, false) ? getString(R.string.deactivate_wifi_shared)
						 : getString(R.string.activite_wifi_shared));
		cbEnable.setTextColor(sp.getBoolean(KEY_ENABALE, false) ? getResources().getColor(R.color.colorAccent)
							  : getResources().getColor(R.color.colorPrimary));

		openhotspot = (Button) findViewById(R.id.activitymainButton1);
		openhotspot.setOnClickListener(this);

		checkIP = (TextView) findViewById(R.id.activity_mainTextView);
		checkIP.setOnClickListener(this);
		checkIP.setText("Click Here");

		Intent intent = new Intent(this, ProxyService.class);
		bindService(intent, this, BIND_AUTO_CREATE);
	}

	@Override
	public void onClick(View p1) {
		int id = p1.getId();
		if (id == R.id.activity_mainTextView) {
			TextView tv = new TextView(this);
			//tv.setText(Html.fromHtml("<font color='#FFFFFF'"+"Proxy Hostname : "+getLocalIpAddress() + "\nProxy Port : " + "8080" + "</font>"));
			tv.setText("Proxy Hostname : " + getLocalIpAddress() + "\nProxy Port : " + "8080");
			//tv.setTextColor(R.color.white);
			tv.setTextColor(ContextCompat.getColor(MainActivityWifi.this, R.color.black));
			tv.setTextIsSelectable(true);
			tv.setTextSize(15f);
			new AlertDialog.Builder(this).setTitle("Long click to copy").setView(tv, 50, 0, 50, 0)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface p1, int p2) {
						p1.dismiss();
					}
				}).show();
		} else if (id == R.id.activitymainButton1) {
			openHotspot();
		}
	}

	private void openHotspot() {
		final Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
		intent.setComponent(cn);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@Override
	public void onServiceConnected(ComponentName cn, IBinder binder) {
		proxyControl = (IProxyControl) binder;
		if (proxyControl != null) {
			updateProxy();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName cn) {
		proxyControl = null;
	}

	@Override
	protected void onDestroy() {
		unbindService(this);
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		cbEnable.setText(
			isChecked ? getString(R.string.deactivate_wifi_shared) : getString(R.string.activite_wifi_shared));
		cbEnable.setTextColor(isChecked ? getResources().getColor(R.color.colorAccent)
							  : getResources().getColor(R.color.colorPrimary));
		edit.putBoolean(KEY_ENABALE, isChecked).apply();
		updateProxy();
	}

	private void updateProxy() {
		if (proxyControl == null) {
			return;
		}
		boolean isRunning = false;
		try {
			isRunning = proxyControl.isRunning();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		boolean shouldRun = sp.getBoolean(KEY_ENABALE, false);
		if (shouldRun && !isRunning) {
			startProxy();
		} else if (!shouldRun && isRunning) {
			stopProxy();
		}
		try {
			isRunning = proxyControl.isRunning();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (isRunning) {
			tvInfo.setText(R.string.proxy_on);
			cbEnable.setChecked(true);
		} else {
			tvInfo.setText(R.string.proxy_off);
			cbEnable.setChecked(false);
		}
	}

	private void startProxy() {
		boolean started = false;
		try {
			started = proxyControl.start();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (!started) {
			return;
		}
		notification.setContentTitle(getString(R.string.app_name));
		notification.setSmallIcon(R.drawable.ic_wifi);
		notification.setDefaults(Notification.DEFAULT_ALL);
		notification.setPriority(Notification.PRIORITY_HIGH);
		notification.setContentText(getResources().getString(R.string.service_text));
		notification.setTicker(getResources().getString(R.string.proxy_on));
		notification.setStyle(new Notification.BigTextStyle().bigText(getResources().getString(R.string.service_text)));
		notification.setOngoing(true);
		notification.setOnlyAlertOnce(true);
		notification.setUsesChronometer(true);
		notification.setVisibility(Notification.VISIBILITY_PUBLIC);
		notification.setContentIntent(getGraphPendingIntent());
		notificationManager.notify(NOTIFICATION_ID.hashCode(), notification.getNotification());
		Toast.makeText(this, getResources().getString(R.string.proxy_started), Toast.LENGTH_SHORT).show();
	}

	private PendingIntent getGraphPendingIntent() {
		int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE | 0 : 0;
		Intent notificationIntent = new Intent();
		notificationIntent.setComponent(new ComponentName(this, MainActivityWifi.class));
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		PendingIntent startLW = PendingIntent.getActivity(this, 0, notificationIntent, flags);
		return startLW;
	}

	@TargetApi(Build.VERSION_CODES.O)
	private void createNotification(NotificationManager notificationManager, String cID) {
		NotificationChannel notificationChannel = new NotificationChannel(cID, getString(R.string.app_name),
																		  NotificationManager.IMPORTANCE_HIGH);
		notificationChannel.setShowBadge(true);
		notificationChannel.enableLights(true);
		notificationChannel.shouldShowLights();
		notificationChannel.setLightColor(Color.GREEN);
		notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
		notificationManager.createNotificationChannel(notificationChannel);
	}

	private void stopProxy() {
		boolean stopped = false;
		try {
			stopped = proxyControl.stop();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (!stopped) {
			return;
		}
		tvInfo.setText(R.string.proxy_off);
		notificationManager.cancel(NOTIFICATION_ID.hashCode());
		Toast.makeText(this, getResources().getString(R.string.proxy_stopped), Toast.LENGTH_SHORT).show();
	}

	private String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						String sAddr = inetAddress.getHostAddress();
						return sAddr.toString();
					}
				}
			}
		} catch (SocketException ex) {
			return "ERROR Obtaining IP";
		}
		return "No IP Available";
	}

	@Override
	public boolean onSupportNavigateUp()
	{
		onBackPressed();
		return true;
	}
}