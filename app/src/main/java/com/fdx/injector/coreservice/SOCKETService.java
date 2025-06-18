package com.fdx.injector.coreservice;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.fdx.injector.MainApp;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.coreservice.core.CHZSSL;
import com.fdx.injector.coreservice.core.CHZSupport;
import com.fdx.injector.coreservice.core.MainThread;
import com.fdx.injector.coreservice.core.SOCKETHTTP;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.VpnStatus;

public class SOCKETService extends Service implements Runnable, Handler.Callback {
	public String TAG = "SOCKETService";

	private void log(String msg) {
		Log.i(TAG, msg);
	}

	public static String FLAG_VPN_START = "START";
	public static String FLAG_VPN_STOP = "STOP";

	public NotificationManager nm;
	public SharedPreferences sp;
	public SharedPreferences dsp;

	private final String CHANNEL_ID = "Chz_Channel_ID";
	private final String CHANNEL_NAME = "Chz_Channel_Name";

	private OpenVPNService vpnService = new OpenVPNService();

	public static boolean isRunning = false;

	private Thread mInjectThread;

	private int listen_port = 8083;
	private ServerSocket listen_socket;
	private Socket input;
	private static Socket output = null;
	private MainThread sc1;
	private MainThread sc2;
	private Handler mHandler;
	private PowerManager.WakeLock wakeLock;
	private Settings mPrefs;
	
	private void setWakelock() {
		try {
			this.wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(1, "SimpleInjector::Tag");
			this.wakeLock.acquire();
			VpnStatus.logWarning("<strong>WakeLock Activated</strong>");
		} catch (Exception e) {
			Log.d("WAKELOCK", e.getMessage());
		}
	}

	private void unsetWakelock() {
		if (this.wakeLock != null && this.wakeLock.isHeld()) {
			// Log.e("WAKELOCK", "is disabled");
			VpnStatus.logWarning("<strong>WakeLock Released</strong>");
			this.wakeLock.release();
		}
	}

	private IOpenVPNServiceInternal mService;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = IOpenVPNServiceInternal.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
		}
	};

	public void isServiceRunning() {
		setStatus(vpnService.getStatus());
	}

	public void setStatus(String connectionState) {
		if (connectionState != null)
			switch (connectionState) {
			case "DISCONNECTED":
				isRunning = false;
				break;
			case "CONNECTED":
				isRunning = true;
				break;
			case "WAIT":
				break;
			case "AUTH":
				isRunning = false;
				break;
			case "RECONNECTING":
				break;
			case "NONETWORK":
				break;
			}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler(this);
		isServiceRunning();
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		sp = MainApp.sp;
		dsp = MainApp.dsp;
		mPrefs = new Settings(this);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent arg0, int arg1, int arg2) {
		String action = arg0.getAction();

		if (action == FLAG_VPN_START) {

			if (mPrefs.getWakelock()) {
				setWakelock();
			}

			if (sp.getBoolean(Settings.CB_PAYLOLOAD, false) == true && sp.getBoolean(Settings.CB_SNI, false) == false
					|| sp.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_OVPN_PROXY || sp.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_OVPN_SSL) {

				if (mInjectThread != null) {
					mInjectThread.interrupt();
					log("minject interupt");
				}
				mInjectThread = new Thread(this, "mInjectThread");
				log("select minject SSL");
				mInjectThread.start();
				log("mInjec start");
			} else {
				onTestBind();
			}
		} else if (action == FLAG_VPN_STOP) {
			stopSelf();
			stopInjectThread();
			if (mPrefs.getWakelock()) {
				unsetWakelock();
			}
		}
		return START_NOT_STICKY;
	}

	public void onTestBind() {
		try {
			Intent intent2 = new Intent(SOCKETService.this, OpenVPNService.class).setAction(OpenVPNService.START_SERVICE);
			bindService(intent2, mConnection, Context.BIND_AUTO_CREATE);
			OpenVpnApi.startVpn(SOCKETService.this, sp.getString(Settings.OVPN_CONFIG, ""),
					sp.getString(Settings.USUARIO_OVPN, "").split(":")[0],
					sp.getString(Settings.USUARIO_OVPN, "").split(":")[1]);
		} catch (Exception e) {
		}
	}

	@Override
	public void run() {
		try {
			log("Socket started");
			if (listen_socket == null) {
				listen_socket = new ServerSocket(listen_port, 50, InetAddress.getByName("127.0.0.1"));
				listen_socket.setReuseAddress(true);
			}
			//	listen_socket = new ServerSocket(listen_port);
			//	listen_socket.setReuseAddress(true);
			chz.sendEmptyMessage(1);
			while (true) {
				input = listen_socket.accept();
				input.setSoTimeout(0);

				if (sp.getBoolean(Settings.CB_PAYLOLOAD, false) == true
						&& sp.getBoolean(Settings.CB_SNI, false) == false) {
					output = new SOCKETHTTP(input).socket2();
				} else if (sp.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_OVPN_PROXY) {
					output = new CHZSSL(input).socket();
				} else {
					output = new CHZSupport(input).socket();
				}

				if (input != null) {
					input.setKeepAlive(true);
				}
				if (output != null) {
					output.setKeepAlive(true);
				}
				if (output == null) {
					output.close();
					log("Closes output");
				} else if (output.isConnected()) {
					frotect(output);
					sc1 = new MainThread(input, output, true);
					sc2 = new MainThread(output, input, false);
					sc1.setDaemon(true);
					sc1.start();
					sc2.setDaemon(true);
					sc2.start();
					log("Daemonn OK");
				}
			}
		} catch (Exception e) {
			log(e.getMessage());
		}
	}

	public final void frotect(Socket socket) {
		if (new OpenVPNService().protect(socket)) {
			String a5 = "Socket protected";
			log("Socket protected");
			Handler handler = this.mHandler;
			handler.sendMessage(handler.obtainMessage(1, a5));
		}
	}

	void stopInjectThread() {
		try {
			if (sc1 != null) {
				sc1.interrupt();
				sc1 = null;
			}
			if (sc2 != null) {
				sc2.interrupt();
				sc2 = null;
			}
			if (listen_socket != null) {
				listen_socket.close();
				listen_socket = null;
			}
			if (input != null) {
				input.close();
				input = null;
			}
			if (output != null) {
				output.close();
				output = null;
			}
			if (mInjectThread != null) {
				mInjectThread.interrupt();
			}
		} catch (Exception e) {
			log("Stop Inject : " + e.getMessage());
		}
	}

	public static boolean protect(VpnService vpnService) {
		if (output == null) {
			Log.i("Vpn Protect", "Socket is null");
			return false;
		} else if (output.isClosed()) {
			Log.i("Vpn Protect", "Socket is closed");
			return false;
		} else if (!output.isConnected()) {
			Log.i("Vpn Protect", "Socket not connected");
			return false;
		} else if (vpnService.protect(output)) {
			Log.i("Vpn Protect", "Socket has protected");
			return true;
		} else {
			Log.i("Vpn Protect", "Failed to protecting socket, reboot this device required");
			return false;
		}
	}

	@Override
	public boolean handleMessage(Message arg0) {
		int rec = arg0.what;
		if (rec == 1) {
			log("Callback");
		}
		return true;
	}

	private Handler chz = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				onTestBind();
			}
		}
	};
}
