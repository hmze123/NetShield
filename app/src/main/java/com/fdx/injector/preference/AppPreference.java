package com.fdx.injector.preference;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.preference.*;
import com.fdx.injector.R;
import com.fdx.injector.coreservice.config.SettingsConstants;
import com.fdx.injector.coreservice.logger.ConnectionStatus;
import com.fdx.injector.coreservice.logger.SkStatus;
//import schetosz.mods.util.Utils;
//import schetosz.mods.activities.BaseActivity;


public class AppPreference extends PreferenceFragmentCompat
implements Preference.OnPreferenceChangeListener, SettingsConstants,
SkStatus.StateListener
{
	private Handler mHandler;
	private SharedPreferences mPref;
	
	private String[] settings_disabled_keys = {
		AUTO_PINGER,
		PINGER,
		SSH_COMPRESSION,
		NETWORK_SPEED
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		SkStatus.addStateListener(this);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		SkStatus.removeStateListener(this);
	}
	
	
	@Override
	public void onCreatePreferences(Bundle bundle, String root_key)
	{
		// Load the Preferences from the XML file
		setPreferencesFromResource(R.xml.app_preference, root_key);
		
		mPref = getPreferenceManager().getDefaultSharedPreferences(getContext());
		
	//	Preference dnsForwardPreference = (CheckBoxPreference)findPreference(DNSFORWARD_KEY);
	//	dnsForwardPreference.setOnPreferenceChangeListener(this);
		
		// update view
		setRunningTunnel(SkStatus.isTunnelActive());
	}
	
	private void onChangeUseVpn(boolean use_vpn){
		
		for (String key : settings_disabled_keys){
			getPreferenceManager().findPreference(key).setEnabled(use_vpn);
		}
		
		use_vpn = true;
		if (use_vpn) {
		}
		else {
			String[] list = {
				
			};
			for (String key : list) {
				getPreferenceManager().findPreference(key).setEnabled(false);
			}
		}
	}
	
	private void setRunningTunnel(boolean isRunning) {
		if (isRunning) {
			for (String key : settings_disabled_keys){
				getPreferenceManager().findPreference(key).setEnabled(false);
			}
			} else {
			onChangeUseVpn(true);
		}
	}
	
	
	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue)
	{
		switch (pref.getKey()) {
			
			
			/*case DNSFORWARD_KEY:
			boolean isDnsForward = (boolean) newValue;
			if(isDnsForward==true){
			/*	mPref.edit().putBoolean("Google_dns",true).apply();
				Intent TunnDNS = new Intent(getActivity(), CustomDNS.class);
				getActivity().startActivity(TunnDNS);*
			}
			break;*/
			
		}
		return true;
	}
	
	@Override
	public void updateState(String state, String logMessage, int localizedResId, ConnectionStatus level, Intent intent)
	{
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				setRunningTunnel(SkStatus.isTunnelActive());
			}
		});
	}
}