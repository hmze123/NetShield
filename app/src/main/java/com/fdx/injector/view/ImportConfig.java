package com.fdx.injector.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fdx.injector.MainApp;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.util.ResetDefault;
import com.scottyab.rootbeer.RootBeer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import de.blinkt.openvpn.core.VpnStatus;

public class ImportConfig {
	private static Context context;
	private static SharedPreferences sp;

	public ImportConfig(Context context) {
		this.context = context;
		sp = MainApp.sp;
	}

	private static final String SETTING_VERSION = "file.appVersionCode", SETTING_VALIDADE = "file.validade",
			SETTING_PROTEGER = "file.proteger", SETTING_AUTOR_MSG = "file.msg";

	public static boolean convertInputAndSave(InputStream input, Context mContext) throws IOException {
		Properties mConfigFile = new Properties();

		Settings settings = new Settings(mContext);
		SharedPreferences.Editor prefsEdit = settings.getPrefsPrivate().edit();

		try {

			InputStream decodedInput = ResetDefault.decodeInput(input);
			//InputStream decodedInput = input;

			try {
				mConfigFile.loadFromXML(decodedInput);
			} catch (FileNotFoundException e) {
				throw new IOException("File Not Found");
			} catch (IOException e) {
				throw new Exception("Error Unknown", e);
			}

			// versão check
			int versionCode = Integer.parseInt(mConfigFile.getProperty(SETTING_VERSION));

			if (versionCode > getBuildId(mContext))
				throw new IOException("Need Update App");

			//if(versionCode != getBuildId(mContext))throw new IOException("Config Lock version App");

			// validade check
			String msg = mConfigFile.getProperty(SETTING_AUTOR_MSG);
			boolean mIsProteger = mConfigFile.getProperty(SETTING_PROTEGER).equals("1") ? true : false;
			long mValidade = 0;

			try {
				mValidade = Long.parseLong(mConfigFile.getProperty(SETTING_VALIDADE));
			} catch (Exception e) {
				throw new IOException("Config Expired");
			}

			if (!mIsProteger || mValidade < 0) {
				mValidade = 0;
			} else if (mValidade > 0 && isValidadeExpirou(mValidade)) {
				throw new IOException("Config Expired");
			}

			boolean ishwid = false;
			boolean cMemekTembem = false;
			String _ishwid = mConfigFile.getProperty(Settings.CP_HWID);
			if (_ishwid != null) {
				ishwid = _ishwid.equals("1") ? true : false;
				if (ishwid) {
					String[] h = mConfigFile.getProperty(Settings.ED_HWID).split("[;]");
					for (String J : h) {
						if (ResetDefault.getHWID().equals(J)) {
							cMemekTembem = true;
						}
					}
					if (cMemekTembem == false)
						throw new IOException("HWID not valid!!");
				}
			}

			//data only
			boolean isdataomly = false;
			String _isdata = mConfigFile.getProperty(Settings.M_DATAT_ONLY);
			if (_isdata != null) {
				isdataomly = _isdata.equals("1") ? true : false;
				if (isdataomly) {
					if (isData(mContext)) {
						throw new IOException("Config for data only");
					}
				}
			}
			// bloqueia root
			boolean isBloquearRoot = false;
			String _blockRoot = mConfigFile.getProperty("bloquearRoot");
			if (_blockRoot != null) {
				isBloquearRoot = _blockRoot.equals("1") ? true : false;
				if (isBloquearRoot) {
					if (isDeviceRooted(mContext)) {
						throw new IOException("Disable root");
					}
				}
			}

			boolean ww = false;
			String pMeK = mConfigFile.getProperty(Settings.I_PASSWORD);
			prefsEdit.putString(Settings.I_PASSWORD, pMeK);
			String pedirLogin = mConfigFile.getProperty("file.pedirLogin");
			if (pedirLogin != null) {
				ww = pedirLogin.equals("1") ? true : false;
				prefsEdit.putBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, ww);
			}

			boolean nt = false;
			String isNote = mConfigFile.getProperty(Settings.CP_NOTE);
			if (isNote != null) {
				nt = isNote.equals("1") ? true : false;
				prefsEdit.putBoolean(Settings.CP_NOTE, nt);
				if (nt) {
					String pwrd = mConfigFile.getProperty(Settings.ED_POWERED);
					String note = mConfigFile.getProperty(Settings.ED_NOTE);
					prefsEdit.putString(Settings.ED_POWERED, pwrd);
					prefsEdit.putString(Settings.ED_NOTE, note);
				}
			}

			boolean pp = false;
			String isBapp = mConfigFile.getProperty(Settings.CP_BLOCK_APP);
			if (isBapp != null) {
				pp = isBapp.equals("1") ? true : false;
				prefsEdit.putBoolean(Settings.CP_BLOCK_APP, pp);
				if (pp) {
					String[] h = mConfigFile.getProperty(Settings.ED_BLOCK_AP).split("[;]");
					for (String N : h) {
						if (check(N)) {
							if (!N.equals("com.fdx.injector"))
								throw new IOException("This config block apps : " + N);
						}
					}
				}
			}

			try {

				String mSerper = mConfigFile.getProperty("SavedSerperString");
				String mOvpnServer = mConfigFile.getProperty(Settings.USUARIO_OVPN);
				
				prefsEdit.putBoolean(Settings.CONFIG_PROTEGER_KEY, mIsProteger);

				int mCsshVpn = Integer.parseInt(mConfigFile.getProperty(Settings.SSH_HARED));
				int tunType = Integer.parseInt(mConfigFile.getProperty(Settings.TUNNELTYPE_KEY));

				String chave = mConfigFile.getProperty(Settings.CHAVE_KEY);
				String nameserver = mConfigFile.getProperty(Settings.NAMESERVER_KEY);
				String dns = mConfigFile.getProperty(Settings.DNS_KEY);
                
                String udpdow = mConfigFile.getProperty(Settings.UDP_DOWN);
				String udpup = mConfigFile.getProperty(Settings.UDP_UP);
				String udpbu = mConfigFile.getProperty(Settings.UDP_BUFFER);
                String vv = mConfigFile.getProperty(Settings.V2RAY_JSON);
				

				prefsEdit.putInt(Settings.SSH_HARED, mCsshVpn);
				prefsEdit.putInt(Settings.TUNNELTYPE_KEY, tunType);

				String _proXy = mConfigFile.getProperty(Settings.PROXY_IP_PORT);
				prefsEdit.putString(Settings.PROXY_IP_PORT, _proXy != null ? _proXy : "");

				String ssl = mConfigFile.getProperty(Settings.CUSTOM_SNI);
				prefsEdit.putString(Settings.CUSTOM_SNI, ssl != null ? ssl : "");

				String _customPayload = mConfigFile.getProperty(Settings.CUSTOM_PAYLOAD_KEY);
				//	prefsEdit.putString(Settings.CUSTOM_PAYLOAD_KEY, _customPayload != null ? _customPayload : "");
				prefsEdit.putString(Settings.CUSTOM_PAYLOAD_KEY, _customPayload != null ? _customPayload : "");

				prefsEdit.putString(Settings.CHAVE_KEY, chave);
				prefsEdit.putString(Settings.NAMESERVER_KEY, nameserver);
				prefsEdit.putString(Settings.DNS_KEY, dns);
                
                prefsEdit.putString(Settings.UDP_DOWN, udpdow);
				prefsEdit.putString(Settings.UDP_UP, udpup);
				prefsEdit.putString(Settings.UDP_BUFFER, udpbu);
               // prefsEdit.putString(Settings.V2RAY_JSON, vv);
                
                String testv = mConfigFile.getProperty(Settings.V2RAY_JSON);
				prefsEdit.putString(Settings.V2RAY_JSON, testv != null ? testv : "");
				

				prefsEdit.putString("SavedSerperString", mSerper);
				prefsEdit.putString(Settings.USUARIO_OVPN, mOvpnServer);
				
				String getOvpnConf = mConfigFile.getProperty(Settings.OVPN_CONFIG);
				prefsEdit.putString(Settings.OVPN_CONFIG, getOvpnConf);
				
				String ovpnHost = mConfigFile.getProperty(Settings.OVPN_HOST);
				String ovpnPort = mConfigFile.getProperty(Settings.OVPN_PORT);
				prefsEdit.putString(Settings.OVPN_HOST, ovpnHost);
				prefsEdit.putString(Settings.OVPN_PORT, ovpnPort);
				
				int mTunnelType = Settings.bTUNNEL_TYPE_SSH_DIRECT;
				String _tunnelType = mConfigFile.getProperty(Settings.TUNNELTYPE_KEY);
				if (!_tunnelType.isEmpty()) {
					if (_tunnelType.equals(Settings.TUNNEL_TYPE_SSH_PROXY)) {
						mTunnelType = Settings.bTUNNEL_TYPE_SSH_PROXY;
					} else if (!_tunnelType.equals(Settings.TUNNEL_TYPE_SSH_DIRECT)) {
						mTunnelType = Integer.parseInt(_tunnelType);
					} else if (_tunnelType.equals(Settings.bTUNNEL_TYPE_SSH_SSL)) {
						mTunnelType = Settings.bTUNNEL_TYPE_SSH_SSL;
					} else if (_tunnelType.equals(Settings.bTUNNEL_TYPE_UDP)) {
						mTunnelType = Settings.bTUNNEL_TYPE_UDP;
					} else if (_tunnelType.equals(Settings.bTUNNEL_TYPE_V2RAY)) {
						mTunnelType = Settings.bTUNNEL_TYPE_V2RAY;
					} else if (_tunnelType.equals(Settings.bTUNNEL_TYPE_SLOWDNS)) {
						mTunnelType = Settings.bTUNNEL_TYPE_SLOWDNS;
					}
				}

				prefsEdit.putInt(Settings.TUNNELTYPE_KEY, mTunnelType);
				prefsEdit.putBoolean(Settings.CONFIG_PROTEGER_KEY, mIsProteger);
				prefsEdit.putLong(Settings.CONFIG_VALIDADE_KEY, mValidade);
				prefsEdit.putBoolean(Settings.BLOQUEAR_ROOT_KEY, isBloquearRoot);
				prefsEdit.putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD,
						!mConfigFile.getProperty(Settings.PROXY_USAR_DEFAULT_PAYLOAD).equals("1") ? false : true);

				String _isDnsForward = mConfigFile.getProperty(Settings.DNSFORWARD_KEY);
				boolean isDnsForward = _isDnsForward != null && _isDnsForward.equals("1") ? true : false;
				String dnsResolver1 = mConfigFile.getProperty(Settings.DNSRESOLVER_KEY1);
				String dnsResolver2 = mConfigFile.getProperty(Settings.DNSRESOLVER_KEY2);
				settings.setVpnDnsForward(isDnsForward);
				settings.setVpnDnsResolver1(dnsResolver1);
				settings.setVpnDnsResolver2(dnsResolver2);

				String _isUdpForward = mConfigFile.getProperty(Settings.UDPFORWARD_KEY);
				boolean isUdpForward = _isUdpForward != null && _isUdpForward.equals("1") ? true : false;
				String udpResolver = mConfigFile.getProperty(Settings.UDPRESOLVER_KEY);
				settings.setVpnUdpForward(isUdpForward);
				settings.setVpnUdpResolver(udpResolver);

				String isPayloadChecked = mConfigFile.getProperty(Settings.CB_PAYLOLOAD);
				prefsEdit.putBoolean(Settings.CB_PAYLOLOAD, isPayloadChecked.equals("1") ? true : false);

				String isSniChecked = mConfigFile.getProperty(Settings.CB_SNI);
				prefsEdit.putBoolean(Settings.CB_SNI, isSniChecked.equals("1") ? true : false);

				String isSlowChecked = mConfigFile.getProperty(Settings.CB_DNSTT);
				prefsEdit.putBoolean(Settings.CB_DNSTT, isSlowChecked.equals("1") ? true : false);
                
                
                String isudpChecked = mConfigFile.getProperty(Settings.CP_UDP);
				prefsEdit.putBoolean(Settings.CP_UDP, isudpChecked.equals("1") ? true : false);

				String isv2rayChecked = mConfigFile.getProperty(Settings.CP_V2RAY);
				prefsEdit.putBoolean(Settings.CP_V2RAY, isv2rayChecked.equals("1") ? true : false);

				String isAutoReplace = mConfigFile.getProperty(Settings.AUTO_REPLACE);
				prefsEdit.putBoolean(Settings.AUTO_REPLACE, isAutoReplace.equals("1") ? true : false);

				String cp = mConfigFile.getProperty(Settings.CP_PAYLOAD);
				prefsEdit.putBoolean(Settings.CP_PAYLOAD, cp.equals("1") ? true : false);

				String cs = mConfigFile.getProperty(Settings.CP_SNI);
				prefsEdit.putBoolean(Settings.CP_SNI, cs.equals("1") ? true : false);

				String csp = mConfigFile.getProperty(Settings.CP_SSH);
				prefsEdit.putBoolean(Settings.CP_SSH, csp.equals("1") ? true : false);

				String css = mConfigFile.getProperty(Settings.CP_DNSTT);
				prefsEdit.putBoolean(Settings.CP_DNSTT, css.equals("1") ? true : false);

			} catch (Exception e) {
				if (settings.getModoDebug()) {
					VpnStatus.logException("Error Settings", e);
				}
				throw new IOException("File Settinga Invalid" + e);
			}

			return prefsEdit.commit();

		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("File invalid", e);
		} catch (Throwable e) {
			throw new IOException("File invalid");
		}
	}

	public static boolean isValidadeExpirou(long validadeDateMillis) {
		if (validadeDateMillis == 0) {
			return false;
		}
		long date_atual = Calendar.getInstance().getTime().getTime();

		if (date_atual >= validadeDateMillis) {
			return true;
		}

		return false;
	}

	public static int getBuildId(Context context) throws IOException {
		try {
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pinfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new IOException("Build ID not found");
		}
	}

	public static boolean isDeviceRooted(Context context) {
		RootBeer rootBeer = new RootBeer(context);

		boolean simpleTests = rootBeer.detectRootManagementApps() || rootBeer.detectPotentiallyDangerousApps()
				|| rootBeer.checkForBinary("su") || rootBeer.checkForDangerousProps() || rootBeer.checkForRWPaths()
				|| rootBeer.detectTestKeys() || rootBeer.checkSuExists() || rootBeer.checkForRootNative()
				|| rootBeer.checkForMagiskBinary();
		return simpleTests;
	}

	public static boolean hmmp(String input) {
		if (input.isEmpty()) {
			return false;
		}

		String peww = sp.getString(Settings.CONFIG_INPUT_PASSWORD_KEY, "");
		if (input.equals(peww)) {
			return true;
		}
		return false;
	}

	public static boolean isData(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
		NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (!mMobile.isConnected()) {
			return true;
		}
		return false;
	}

	public static boolean check(String uri) {
		PackageManager pm = context.getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

}