package com.fdx.injector.wifi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.fdx.injector.ActivityTools.IProxyControl;
import com.fdx.injector.ActivityTools.ProxyServer;

public class ProxyService extends Service {

	public static final String TAG = "ProxyService";

	@Override
	public IBinder onBind(Intent binder) {
		return new IProxyControl.Stub() {

			@Override
			public boolean start() {
				return doStart();
			}

			@Override
			public boolean stop() {
				return doStop();
			}

			@Override
			public boolean isRunning() {
				return ProxyServer.getInstance().isRunning();
			}

			@Override
			public int getPort() {
				return ProxyServer.getInstance().getPort();
			}
		};
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private boolean doStart() {
		ProxyServer proxyServer = ProxyServer.getInstance();
		if (proxyServer.isRunning()) {
			return false;
		}
		return proxyServer.start();
	}

	private boolean doStop() {
		ProxyServer proxyServer = ProxyServer.getInstance();
		if (!proxyServer.isRunning()) {
			return false;
		}
		return proxyServer.stop();
	}
}