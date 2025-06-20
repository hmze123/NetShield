package com.fdx.injector.coreservice;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fdx.injector.MainApp;
import com.fdx.injector.R;
import com.fdx.injector.coreservice.aidl.IUltraSSHServiceInternal;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.coreservice.ext.RotateProxy;
import com.fdx.injector.coreservice.logger.ConnectionStatus;
import com.fdx.injector.coreservice.logger.SkStatus;
import com.fdx.injector.coreservice.tunnel.DNSTunnelThread;
import com.fdx.injector.coreservice.tunnel.TunnelManagerThread;
import com.fdx.injector.coreservice.tunnel.TunnelUtils;
import com.fdx.injector.coreservice.util.DummyActivity;
import com.fdx.injector.util.ResetDefault;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

import de.blinkt.openvpn.core.VpnStatus;

public class SocksHttpService extends Service implements SkStatus.StateListener {
	private static final String TAG = SocksHttpService.class.getSimpleName();
	public static final String START_SERVICE = "com.fdx.injector.coreservice:startTunnel";

	private static final int PRIORITY_MIN = -2;
	private static final int PRIORITY_DEFAULT = 0;
	private static final int PRIORITY_MAX = 2;

	private NotificationManager mNotificationManager;
	public static boolean isRunning = false;


	private Handler mHandler;
	private myhandler mh;
	private Settings mPrefs;
	private Thread mTunnelThread;
	private TunnelManagerThread mTunnelManager;
	private ConnectivityManager connMgr;
	public static PowerManager.WakeLock wakeLock;

	private final IBinder mBinder = new IUltraSSHServiceInternal.Stub() {

		@Override
		public void stopVPN() {
			SocksHttpService.this.stopTunnel();
		}
	};

	private static SocksHttpService instance = null;
	public static boolean isServiceRunning = false;
	public static boolean isInstanceCreated() {
		return instance != null;
	} // met

	private DNSTunnelThread mDnsThread;

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");

		super.onCreate();
		isServiceRunning = true;
		wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(1,
				"SimpleInjector::tag");

		mPrefs = new Settings(this);
		mHandler = new Handler();

		connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		instance = this;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");

		startTunnelBroadcast();

		SkStatus.addStateListener(this);

		if (intent != null && START_SERVICE.equals(intent.getAction()))
			return START_NOT_STICKY;

		String stateMsg = getString(SkStatus.getLocalizedState(SkStatus.getLastState()));
		showNotification(stateMsg, NOTIFICATION_CHANNEL_NEWSTATUS_ID, 0, ConnectionStatus.LEVEL_START, null);

		new Thread(new Runnable() {
			@Override
			public void run() {
				startTunnel();
			}
		}).start();

		// return Service.START_STICKY;
		return Service.START_NOT_STICKY;
	}

	/** Tunnel */
	public synchronized void startTunnel() {
		SkStatus.updateStateString(SkStatus.SSH_INICIANDO, getString(R.string.starting_service_ssh));
		networkStateChange(this, true);
		VpnStatus.logWarning(String.format("Local IP: %s", getIpPublic()));

		if (mPrefs.getWakelock()) {
			wakeLock.acquire();
			VpnStatus.logWarning("<strong>WakeLock Activated</strong>");
		}
		try {
			SharedPreferences prefs = mPrefs.getPrefsPrivate();
			int tunnelType = prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);

			if (tunnelType == Settings.bTUNNEL_TYPE_SLOWDNS) {
				mPrefs.setBypass(true);
				mDnsThread = new DNSTunnelThread(this);
				mDnsThread.start();
			}
			mTunnelManager = new TunnelManagerThread(mHandler, this);
			mTunnelManager.setOnStopClienteListener(new TunnelManagerThread.OnStopCliente() {
				@Override
				public void onStop() {
					endTunnelService();
				}
			});

			mTunnelThread = new Thread(mTunnelManager);
			mTunnelThread.start();
		} catch (Exception e) {
			SkStatus.logException(e);
			endTunnelService();
		}
	}

	public synchronized void stopTunnel() {
		SharedPreferences prefs = mPrefs.getPrefsPrivate();
		int tunnelType = prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);
		if (tunnelType == Settings.bTUNNEL_TYPE_SLOWDNS) {
			mPrefs.setBypass(false);
			if (mDnsThread != null) {
				mDnsThread.interrupt();
			}
			mDnsThread = null;
		}
		if (mTunnelManager != null) {
			mTunnelManager.stopAll();
			networkStateChange(this, true);
			if (mTunnelThread != null) {
				mTunnelThread.interrupt();
			}

			mTunnelManager = null;
		}

		if (mh != null) {
			mh.gop();
		}
	}

	protected String getIpPublic() {

		final android.net.NetworkInfo network = connMgr.getActiveNetworkInfo();

		if (network != null && network.isConnectedOrConnecting()) {
			return TunnelUtils.getLocalIpAddress();
		} else {
			return "Indisponivel";
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		isServiceRunning = false;
		super.onDestroy();
		stopTunnel();
		stopTunnelBroadcast();
		instance = null;
		SkStatus.removeStateListener(this);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Log.d(TAG, "task removed");
		Intent intent = new Intent(this, DummyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onLowMemory()
	 */
	@Override
	public void onLowMemory() {
		super.onLowMemory();

		VpnStatus.logWarning("Low Memory Warning!");
	}

	public void endTunnelService() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				stopForeground(true);
				stopSelf();
				SkStatus.removeStateListener(SocksHttpService.this);
			}
		});
	}

	private void connected() {
		Vibrator vb_service = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vb_service.vibrate(150);
	}

	/** Notificação */
	public static final String NOTIFICATION_CHANNEL_BG_ID = "openvpn_bg";

	public static final String NOTIFICATION_CHANNEL_NEWSTATUS_ID = "openvpn_newstat";
	public static final String NOTIFICATION_CHANNEL_USERREQ_ID = "openvpn_userreq";

	private Notification.Builder mNotifyBuilder = null;
	private String lastChannel;

	public synchronized void showNotification(final String msg, final String channel, long when,
											  ConnectionStatus status, Intent intent) {
		int icon = getIconByConnectionStatus(status);

		if (mNotifyBuilder == null) {
			// mNotificationManager = (NotificationManager)
			// getSystemService(Context.NOTIFICATION_SERVICE);

			mNotifyBuilder = new Notification.Builder(this).setContentTitle(getString(R.string.app_name))
					.setOnlyAlertOnce(true).setOngoing(true);

			// Try to set the priority available since API 16 (Jellybean)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				addVpnActionsToNotification(mNotifyBuilder);
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				lpNotificationExtras(mNotifyBuilder, Notification.CATEGORY_SERVICE);
		}

		int priority = PRIORITY_DEFAULT;
		if (channel.equals(NOTIFICATION_CHANNEL_BG_ID))
			priority = PRIORITY_MIN;
		else if (channel.equals(NOTIFICATION_CHANNEL_USERREQ_ID))
			priority = PRIORITY_MAX;

		mNotifyBuilder.setSmallIcon(icon);
		mNotifyBuilder.setContentText(msg);

		if (status == ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT) {
			PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0 | PendingIntent.FLAG_IMMUTABLE);
			mNotifyBuilder.setContentIntent(pIntent);
		} else {
			mNotifyBuilder.setContentIntent(getGraphPendingIntent(this));
		}

		if (when != 0)
			mNotifyBuilder.setWhen(when);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			jbNotificationExtras(priority, mNotifyBuilder);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			//noinspection NewApi
			mNotifyBuilder.setChannelId(channel);
		}

		String tickerText = msg;
		if (tickerText != null && !tickerText.equals(""))
			mNotifyBuilder.setTicker(tickerText);

		Notification notification = mNotifyBuilder.build();

		int notificationId = channel.hashCode();

		startForeground(notificationId, notification);

		mNotificationManager.notify(notificationId, notification);

		if (lastChannel != null && !channel.equals(lastChannel)) {
			// Cancel old notification
			mNotificationManager.cancel(lastChannel.hashCode());
		}

		lastChannel = channel;
		// mNotificationShowing = true;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void lpNotificationExtras(Notification.Builder nbuilder, String category) {
		nbuilder.setCategory(category);
		nbuilder.setLocalOnly(true);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void jbNotificationExtras(int priority, Notification.Builder nbuilder) {
		try {
			if (priority != 0) {
				Method setpriority = nbuilder.getClass().getMethod("setPriority", int.class);
				setpriority.invoke(nbuilder, priority);

				Method setUsesChronometer = nbuilder.getClass().getMethod("setUsesChronometer", boolean.class);
				setUsesChronometer.invoke(nbuilder, true);
			}

			// ignore exception
		} catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException
				 | IllegalAccessException e) {
			SkStatus.logException(e);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	private void addVpnActionsToNotification(Notification.Builder nbuilder) {

		Intent reconnectVPN = new Intent(this, MainReceiver.class);
		reconnectVPN.setAction(MainReceiver.ACTION_SERVICE_RESTART);
		PendingIntent reconnectPendingIntent = PendingIntent.getBroadcast(this, 0, reconnectVPN,
				PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		nbuilder.addAction(R.drawable.ic_autorenew_black_24dp, getString(R.string.reconnect), reconnectPendingIntent);

		Intent disconnectVPN = new Intent(this, MainReceiver.class);
		disconnectVPN.setAction(MainReceiver.ACTION_SERVICE_STOP);
		PendingIntent disconnectPendingIntent = PendingIntent.getBroadcast(this, 0, disconnectVPN,
				PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		nbuilder.addAction(R.drawable.ic_power_settings_new_black_24dp, getString(R.string.stop),
				disconnectPendingIntent);
	}

	private int getIconByConnectionStatus(ConnectionStatus level) {
		switch (level) {
			case LEVEL_CONNECTED:
				return R.drawable.ic_leaf;
			case LEVEL_AUTH_FAILED:
			case LEVEL_NONETWORK:
			case LEVEL_NOTCONNECTED:
			case LEVEL_CONNECTING_NO_SERVER_REPLY_YET:
			case LEVEL_CONNECTING_SERVER_REPLIED:
			case UNKNOWN_LEVEL:
			default:
				//  connected();
				return R.drawable.ic_cloud_off_black_24dp;
		}
	}

	// Usado tamb├йm pelo tunnel VPN
	public static PendingIntent getGraphPendingIntent(Context context) {
		// Let the configure Button show the Log

		//   Intent intent = new Intent();
		//   intent.setComponent(new ComponentName(context, context.getPackageName() +
		// ".SocksHttpMainActivity"));
		//  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP|
		// Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_IMMUTABLE |
		// PendingIntent.FLAG_UPDATE_CURRENT);

		//  PendingIntent startLW = PendingIntent.getActivity(context, 0, intent, 0);
		//  return startLW;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setComponent(new ComponentName(context, context.getPackageName() + ".SocksHttpMainActivity"));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

		PendingIntent pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
		return pending;
	}

	/** SkStatus.StateListener */
	@Override
	public void updateState(String state, String msg, int resid, ConnectionStatus level, Intent intent) {

		// If the process is not running, ignore any state,
		// Notification should be invisible in this state

		if (mTunnelThread == null)
			return;

		String channel = NOTIFICATION_CHANNEL_BG_ID;
		if (level.equals(ConnectionStatus.LEVEL_CONNECTED)) {
			if (MainApp.sp.getBoolean(Settings.HOT_SPOT, false)) {
				if (ResetDefault.getSockIP() != null) {
					myhandler handler = new myhandler(this, 9001);
					this.mh = handler;
					handler.setDaemon(true);
					this.mh.start();
					VpnStatus.logWarning("<font color='#00FF7F'>Hotspot/Tether Enabled: "+ ResetDefault.getSockIP() +":9001</font>");
				}else{
					VpnStatus.logWarning("<font color='#FF5733'>Please turn on MOBILE Hotspot before use this</font>");
				}
			}
		}
		//	String MsgMe = ResetDefault.getSockIP() + ":9001";
		String stateMsg = getString(SkStatus.getLocalizedState(SkStatus.getLastState()));
		showNotification(stateMsg, channel, 0, level, null);
	}

	/** Tunnel Broadcast */
	private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
		@Override
		public void onAvailable(Network net) {
			//VpnStatus.logDebug("Rede disponivel");
		}

		@Override
		public void onLost(Network net) {
			//	VpnStatus.logDebug("Rede perdida");
		}

		@Override
		public void onUnavailable() {
			//	VpnStatus.logDebug("Rede indisponivel");
		}
	};

	public static final String TUNNEL_SSH_RESTART_SERVICE = SocksHttpService.class.getName()
			+ "::restartservicebroadcast",
			TUNNEL_SSH_STOP_SERVICE = SocksHttpService.class.getName() + "::stopservicebroadcast";

	private void startTunnelBroadcast() {
		if (Build.VERSION.SDK_INT >= 24) {
			connMgr.registerDefaultNetworkCallback(networkCallback);
		}

		IntentFilter broadcastFilter = new IntentFilter();
		broadcastFilter.addAction(TUNNEL_SSH_STOP_SERVICE);
		broadcastFilter.addAction(TUNNEL_SSH_RESTART_SERVICE);

		LocalBroadcastManager.getInstance(this).registerReceiver(mTunnelSSHBroadcastReceiver, broadcastFilter);
	}

	private void stopTunnelBroadcast() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mTunnelSSHBroadcastReceiver);

		if (Build.VERSION.SDK_INT >= 24)
			connMgr.unregisterNetworkCallback(networkCallback);
	}

	private BroadcastReceiver mTunnelSSHBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action == null) {
				return;
			}

			if (action.equals(TUNNEL_SSH_RESTART_SERVICE)) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						if (mTunnelManager != null) {
							mTunnelManager.reconnectSSH();
						}
					}
				}).start();
			} else if (action.equals(TUNNEL_SSH_STOP_SERVICE)) {
				endTunnelService();
			}
		}
	};

	private static String lastStateMsg;

	protected void networkStateChange(Context context, boolean showStatusRepetido) {
		String netstatestring;

		try {
			// deprecated in 29
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (networkInfo == null) {
				netstatestring = "not connected";
			} else {
				String subtype = networkInfo.getSubtypeName();
				if (subtype == null)
					subtype = "";
				String extrainfo = networkInfo.getExtraInfo();
				if (extrainfo == null)
					extrainfo = "";

				netstatestring = String.format("%2$s %4$s to %1$s %3$s", networkInfo.getTypeName(),
						networkInfo.getDetailedState(), extrainfo, subtype);
			}

		} catch (Exception e) {
			netstatestring = e.getMessage();
		}

		if (showStatusRepetido || !netstatestring.equals(lastStateMsg))
			VpnStatus.logWarning(netstatestring);

		lastStateMsg = netstatestring;
	}

	public class myhandler extends Thread {
		public int j;
		public ServerSocket listen_socket;
		public Socket input;

		public myhandler(SocksHttpService myService, int i2) {
			this.j = i2;
		}

		public void gop() {
			try {
				if (listen_socket != null) {
					listen_socket.close();
					listen_socket = null;
				}
				if (input != null) {
					input.close();
					input = null;
				}
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
			}
		}

		@Override
		public void run() {
			try {
				listen_socket = new ServerSocket(this.j);
				while (!Thread.interrupted()) {
					input = listen_socket.accept();
					input.setSoTimeout(0);
					new RotateProxy(input).start();
				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}
}
