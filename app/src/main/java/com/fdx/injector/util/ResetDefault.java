package com.fdx.injector.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Build.VERSION;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fdx.injector.MainApp;
import com.fdx.injector.adapter.LogsAdapter;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.coreservice.util.securepreferences.crypto.Cryptor;
import com.fdx.injector.coreservice.util.securepreferences.model.SecurityConfig;
import com.fdx.injector.coreservice.util.Cripto;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.net.InetAddress;

import android.util.Log;
import java.net.SocketException;

public class ResetDefault {
	private static String TAG = "ResetDefault";
	public static Context mContext;

	public static SharedPreferences sp;
	public static SharedPreferences dsp;
	public static SharedPreferences uspe;
	public static SharedPreferences vdsp;
	public static SharedPreferences ddsp;
	public static LogsAdapter mLogAdapter;

	public ResetDefault(Context context) {
		this.mContext = context;
		sp = MainApp.sp;
		dsp = MainApp.dsp;
		uspe = MainApp.uspe;
		vdsp = MainApp.vdsp;
		dsp = MainApp.ddsp;
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
		mLogAdapter = new LogsAdapter(linearLayoutManager, context);
	}

	public static void resetToBasic() {
		SharedPreferences.Editor spe = sp.edit();
		SharedPreferences.Editor dspe = dsp.edit();
		SharedPreferences.Editor upe = uspe.edit();
		SharedPreferences.Editor vpe = vdsp.edit();
		SharedPreferences.Editor  dddspe =  ddsp.edit();


		//MyConfig
		spe.putInt(Settings.SSH_HARED, sp.getInt(Settings.OVPN_HARED,  uspe.getInt(Settings.UDP_HARED, vdsp.getInt(Settings.V2RAY_HARED, ddsp.getInt(Settings.DNSTT_HARED,  0)))));

		spe.remove("SavedSerperString");
		spe.remove(Settings.CUSTOM_PAYLOAD_KEY);
		spe.remove(Settings.CUSTOM_SNI);
		spe.remove(Settings.PROXY_IP_PORT);

		spe.remove(Settings.CB_PAYLOLOAD);
		spe.remove(Settings.CB_SNI);
		spe.remove(Settings.CB_DNSTT);
        spe.remove(Settings.CB_V2RAY);
		spe.remove(Settings.CB_UDP);

		spe.putInt(Settings.TUNNELTYPE_KEY, 0);

		spe.remove(Settings.CHZ_8);
		spe.remove(Settings.CHZ_9);
		spe.remove(Settings.AUTO_REPLACE);
		spe.remove(Settings.DETAI_LOG);
		spe.remove(Settings.SHOW_STATS);

		spe.remove(Settings.USUARIO_OVPN);
		spe.remove(Settings.DNS_DOMAIN_KEY);
		spe.remove(Settings.OVPN_CONFIG);
		spe.remove(Settings.CONFIG_PROTEGER_KEY);
		spe.remove(Settings.OVPN_HOST);
		spe.remove(Settings.OVPN_PORT);

		spe.remove(Settings.CP_PAYLOAD).apply();
		spe.putBoolean(Settings.CP_PAYLOAD, false).apply();

		spe.remove(Settings.CP_SNI).apply();
		spe.putBoolean(Settings.CP_SNI, false).apply();

        spe.remove(Settings.CP_UDP).apply();
		spe.putBoolean(Settings.CP_UDP, false).apply();

        
        spe.remove(Settings.CP_V2RAY).apply();
		spe.putBoolean(Settings.CP_V2RAY, false).apply();
        
		spe.remove(Settings.CP_SSH).apply();
		spe.putBoolean(Settings.CP_SSH, false).apply();

		spe.remove(Settings.CP_DNSTT).apply();
		spe.putBoolean(Settings.CP_DNSTT, false).apply();

		spe.remove(Settings.LOG_LEVE);
		//	spe.remove(Constant.cAutoClear);
		spe.remove(Settings.CONFIG_INPUT_PASSWORD_KEY);
		//	spe.remove(Constant.cAp);

		//config reset
		spe.remove(Settings.NAME_CONFIG);
		spe.remove(Settings.S_SAVE);
		spe.remove(Settings.I_PASSWORD);
		spe.remove(Settings.CP_NOTE);
		spe.remove(Settings.ED_POWERED);
		spe.remove(Settings.ED_NOTE);
		spe.remove(Settings.M_DATAT_ONLY);
		spe.remove(Settings.CP_HWID);
		spe.remove(Settings.ED_HWID);
		spe.remove(Settings.CP_BLOCK_APP);
		spe.remove(Settings.ED_BLOCK_AP);
		
		spe.remove(Settings.DISABLE_DELAY_KEY);

		//SlowDns
		spe.remove(Settings.CHAVE_KEY);
		spe.remove(Settings.NAMESERVER_KEY);
		spe.remove(Settings.DNS_KEY);
        
        //udp
		spe.remove(Settings.UDP_DOWN);
		spe.remove(Settings.UDP_UP);
		spe.remove(Settings.UDP_BUFFER);
        
        //v2ray 
        spe.remove(Settings.V2RAY_JSON);
        
		//Config
		spe.putInt(Settings.TUNNELTYPE_KEY, 1);
		spe.remove(Settings.CONFIG_PROTEGER_KEY);
		spe.remove(Settings.CONFIG_VALIDADE_KEY);
		spe.remove(Settings.BLOQUEAR_ROOT_KEY);
		spe.remove(Settings.CUSTOM_PAYLOAD_KEY);
		spe.remove(Settings.CUSTOM_SNI);
		spe.remove(Settings.PROXY_USAR_DEFAULT_PAYLOAD);
		spe.remove(Settings.CP_NOTE);
		spe.remove(Settings.ED_NOTE);

		//All about server
		spe.remove(Settings.USUARIO_KEY);
		spe.remove(Settings.SENHA_KEY);
		spe.remove(Settings.SERVIDOR_KEY);
		spe.remove(Settings.SERVIDOR_PORTA_KEY);
		spe.remove(Settings.PROXY_IP_KEY);
		spe.remove(Settings.PROXY_PORTA_KEY);

		//Dns Udp
		dspe.putBoolean(Settings.DNSFORWARD_KEY, false);
		dspe.remove(Settings.DNSRESOLVER_KEY1);
		dspe.remove(Settings.DNSRESOLVER_KEY2);
		dspe.remove(Settings.UDPRESOLVER_KEY);

		dspe.remove(Settings.WAKELOCK_KEY);

		mLogAdapter.clearLog();

		spe.apply();
		dspe.apply();
		spe.commit();
		dspe.commit();
	}

	public static final String md5(String str) {
		try {
			MessageDigest instance = MessageDigest.getInstance("MD5");
			instance.update(str.getBytes());
			byte[] digest = instance.digest();
			StringBuilder stringBuilder = new StringBuilder();
			for (byte b : digest) {
				String toHexString = Integer.toHexString(b & 255);
				while (toHexString.length() < 2) {
					toHexString = new StringBuffer().append("0").append(toHexString).toString();
				}
				stringBuilder.append(toHexString);
			}
			return stringBuilder.toString();
		} catch (NoSuchAlgorithmException e) {
			return (String) null;
		}
	}

	public static String getHWID() {
		return md5(
				new StringBuffer().append(new StringBuffer()
						.append(new StringBuffer()
								.append(new StringBuffer()
										.append(new StringBuffer()
												.append(new StringBuffer()
														.append(new StringBuffer().append(Build.SERIAL)
																.append(Build.BOARD.length() % 5).toString())
														.append(Build.PRODUCT.length() % 5).toString())
												.append(Build.BOARD.length() % 5).toString())
										.append(Build.MODEL.length() % 5).toString())
								.append(Build.MANUFACTURER.length() % 5).toString())
						.append(Build.DEVICE.length() % 5).toString()).append(Build.HARDWARE).toString());
	}

	public static void copyToClipboard(Context context, String str) {
		if (VERSION.SDK_INT >= 11) {
			((ClipboardManager) context.getSystemService("clipboard"))
					.setPrimaryClip(ClipData.newPlainText("log", str));
		} else {
			((android.text.ClipboardManager) context.getSystemService("clipboard")).setText(str);
		}
	}

	public static void trimCache(Context context) {
		try {
			File dir = context.getCacheDir();
			if (dir != null && dir.isDirectory()) {
				deleteDir(dir);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it
		return dir.delete();
	}

	public static String getSockIP() {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface nextElement = networkInterfaces.nextElement();
				if (nextElement.getName().contains("wlan") || nextElement.getName().contains("ap")
						|| nextElement.getName().contains("wl")) {
					Enumeration<InetAddress> inetAddresses = nextElement.getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						InetAddress nextElement2 = inetAddresses.nextElement();
						if (!nextElement2.isLoopbackAddress() && nextElement2.getAddress().length == 4) {
							Log.d(TAG, nextElement2.getHostAddress());
							return nextElement2.getHostAddress();
						}
					}
					continue;
				}
			}
			return null;
		} catch (SocketException e2) {
			Log.e(TAG, e2.toString());
			return null;
		}
	}
	
	private static Cryptor mCrypto;
	static {
		mCrypto = Cryptor.initWithSecurityConfig(
			new SecurityConfig.Builder( new String(new byte[]{69,100,1,})).build());
	}
	
	public static InputStream encodeInput(InputStream in) throws Throwable {
		//ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		String strBase64 = mCrypto.encryptToBase64(getBytesArrayInputStream(in)
			.toByteArray());
		
		//Cripto.encrypt(KEY_PASSWORD, in, out);
		
		return new ByteArrayInputStream(strBase64.getBytes());
	}
	
	public static InputStream decodeInput(InputStream in) throws Throwable {
		byte[] byteDecript;
		
		ByteArrayOutputStream byteArrayOut = getBytesArrayInputStream(in);
		
		try {
			byteDecript = mCrypto.decryptFromBase64(byteArrayOut.toString());
		} catch (IllegalArgumentException e) {
			// decodifica confg antigas
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Cripto.decrypt(Settings.AUTO_CLEAR_LOGS_KEY , new ByteArrayInputStream(byteArrayOut.toByteArray()), out);
			byteDecript = out.toByteArray();
		}
		
		return new ByteArrayInputStream(byteDecript);
	}
	
	public static ByteArrayOutputStream getBytesArrayInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();
		
		return buffer;
	}


}