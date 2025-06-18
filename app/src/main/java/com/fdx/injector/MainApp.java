package com.fdx.injector;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

import com.fdx.injector.coreservice.SocksHttpCore;
import com.fdx.injector.coreservice.config.Settings;

import org.conscrypt.Conscrypt;

import java.security.Security;

/** App */
public class MainApp extends Application {
	private static MainApp mApp;
	private int night_mode;
	private Settings mConfig;
	public static SharedPreferences sp;
	public static SharedPreferences dsp;
	public static SharedPreferences uspe;
	public static SharedPreferences vdsp;
	public static SharedPreferences ddsp;
	public static Context ctx;

	@Override
	public void onCreate() {
		super.onCreate();
	/*	if (!chetoszclass.z(getApplicationContext()).equals(chetoszclass.x())) {
			return;
		}*/
//		Logger.initialize(this);
		try {
			Process p = Runtime.getRuntime().exec("su");
		} catch (Exception e) {
		}
		mApp = this;
		SocksHttpCore.init(this);
		Security.insertProviderAt(Conscrypt.newProvider(), 1);
		mConfig = new Settings(this);
		sp = mConfig.getPrefsPrivate();
		uspe = mConfig.getPrefsPrivate();
		ddsp = mConfig.getPrefsPrivate();
		sp = mConfig.getPrefsPrivate();
		dsp = PreferenceManager.getDefaultSharedPreferences(this);
		ctx = this;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		// LocaleHelper.setLocale(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// LocaleHelper.setLocale(this);
	}

	public static MainApp getApp() {
		return mApp;
	}

	public void setModoNoturno(Context text) {
		if (new Settings(this).getModoNoturno().equals("on")) {
			night_mode = AppCompatDelegate.MODE_NIGHT_YES;
		} else {
			night_mode = AppCompatDelegate.MODE_NIGHT_NO;
		}
		AppCompatDelegate.setDefaultNightMode(night_mode);
	}
}
