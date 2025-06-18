package de.blinkt.openvpn;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;

import com.fdx.injector.MainActivity;
import com.fdx.injector.coreservice.config.Settings;

import java.io.IOException;
import java.io.StringReader;

import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;


public class OpenVpnApi {
	public static SharedPreferences dsp;
	public static SharedPreferences sp;
	private static final String TAG = "OpenVpnApi";

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	public static void startVpn(Context context, String inlineConfig, String userName, String pw)
			throws RemoteException {
		if (TextUtils.isEmpty(inlineConfig))
			throw new RemoteException("config is empty");
		startVpnInternal(context, inlineConfig, userName, pw);
	}

	static void startVpnInternal(Context context, String inlineConfig, String userName, String pw)
			throws RemoteException {
		ConfigParser cp = new ConfigParser();
		dsp = MainActivity.mPrefs;
		sp = MainActivity.sShared;
		try {
			cp.parseConfig(new StringReader(inlineConfig));
			VpnProfile vp = cp.convertProfile();// Analysis.ovpn
			if (vp.checkProfile(context) != com.fdx.injector.R.string.no_error_found) {
				throw new RemoteException(context.getString(vp.checkProfile(context)));
			}
			vp.mProfileCreator = context.getPackageName();

			if (dsp.getBoolean(Settings.DNSFORWARD_KEY, false) == true) {
				vp.mOverrideDNS = true;
				vp.mSearchDomain = sp.getString(Settings.DNS_DOMAIN_KEY, "").isEmpty() ? "blinkt.de"
						: sp.getString(Settings.DNS_DOMAIN_KEY, "");
				vp.mDNS1 = dsp.getString(Settings.DNSRESOLVER_KEY1, "").isEmpty() ? "8.8.8.8"
						: dsp.getString(Settings.DNSRESOLVER_KEY1, "");
				vp.mDNS2 = dsp.getString(Settings.DNSRESOLVER_KEY2, "").isEmpty() ? "8.8.4.4"
						: dsp.getString(Settings.DNSRESOLVER_KEY2, "");
			}
			vp.mUsername = userName;
			vp.mPassword = pw;

			if (sp.getBoolean(Settings.CB_PAYLOLOAD, false) == true && sp.getBoolean(Settings.CB_SNI, false) == false
					|| sp.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_OVPN_PROXY || sp.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_OVPN_SSL) {
				vp.mUseDefaultRoute = false;
				vp.mCustomRoutes = "0.0.0.0/0";
				vp.mExcludedRoutes = sp.getString(Settings.PROXY_IP_PORT, "").isEmpty() ? sp.getString(Settings.CUSTOM_SNI, "")
						: sp.getString(Settings.PROXY_IP_PORT, "").split(":")[0];
				vp.mUseCustomConfig = true;
				vp.mCustomConfigOptions = new StringBuffer().append(vp.mCustomConfigOptions)
						.append("http-proxy 127.0.0.1 8083").toString();
			}
			ProfileManager.setTemporaryProfile(context, vp);
			VPNLaunchHelper.startOpenVpn(vp, context);
		} catch (IOException | ConfigParser.ConfigParseError e) {
			throw new RemoteException(e.getMessage());
		}
	}
}
