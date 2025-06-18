package com.fdx.injector.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
//import android.support.design.widget.TextInputEditText;
//import android.support.design.widget.TextInputLayout;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fdx.injector.*;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.util.ResetDefault;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import com.scottyab.rootbeer.RootBeer;

public class ExportConfig {

	private static EditText edFileName;
	private static EditText edPassword;
	private static EditText edHwid;
	private static EditText edNote;
	private static EditText edPowered;
	private static EditText edBlokApp;

	private static Button btnexit;
	public static Button btnSave;

	private static CheckBox cbPayload;
	private static CheckBox cbSni;
	private static CheckBox cbSSH;
	private static CheckBox cbSlow;
    private static CheckBox cbudp;
	private static CheckBox cbv2ray;
	private static CheckBox cbALL;
	private static CheckBox cbExpiry;
	private static CheckBox cbHwid;
	private static CheckBox cbData;
	private static CheckBox cbBlockRoot;
	private static CheckBox cbNote;
	private static CheckBox cbPassword;
	private static CheckBox cbExtraSniff;
	private static CheckBox cbBlockApp;

	private static LinearLayout llNote;
	private static LinearLayout llPassword;
	private static LinearLayout llHwid;
	private static LinearLayout llBlokkApp;

	private OnGenerateListener generateListener;
	private OnCancelListener cancelListener;
	private OnNeutralListener neutralButtonListener;

	private CharSequence generateButtonName;
	private CharSequence cancelButtonName;
	private CharSequence neutralButtonName;

	private static SharedPreferences sp;
	private static View v;

	private static LayoutInflater inflater;

	private String title;
	private static Context context;

	private static final String C_VER = "file.appVersionCode", C_VAL = "file.validade", C_LCK = "file.proteger",
			C_A_MS = "file.msg";

	public static boolean mIsProteger = true;
	public static boolean isBloquearRoot = false;
	public static boolean mDataOnly = false;
	public static boolean cPpp = false;
	public static boolean cSss = false;
	public static boolean cSp = false;
	public static boolean cSs = false;
    public static boolean Udp = false;
    public static boolean V2ray = false;
	public static String mMensagem = "";
	public static long mValidade = 0;

	public ExportConfig(Context context) {
		this.context = context;
		sp = MainApp.sp;
	}

	/** interface for positive button **/
	public interface OnGenerateListener {
		void onGenerate(String payloadGenerated);
	}

	/** interface for negative button **/
	public interface OnCancelListener {
		void onCancelListener();
	}

	/** interface for neutral button **/
	public interface OnNeutralListener {
		void onNeutralListener();
	}

	public static void showToast(String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * This should not be null
	 * This serves as your positive button
	 **/
	public void setGenerateListener(CharSequence generateButtonName, OnGenerateListener generateListener) {
		this.generateButtonName = generateButtonName;
		this.generateListener = generateListener;
	}

	/**
	 * This should not be null
	 * This serves as your negative button
	 **/
	public void setCancelListener(CharSequence cancelButtonName, OnCancelListener cancelListener) {
		this.cancelButtonName = cancelButtonName;
		this.cancelListener = cancelListener;
	}

	/**
	 * This should not be null
	 * This serves as your neutral button
	 **/
	public void setNeutralButtonListener(CharSequence neutralButtonName, OnNeutralListener neutralButtonListener) {
		this.neutralButtonName = neutralButtonName;
		this.neutralButtonListener = neutralButtonListener;
	}

	public void setDialogTitle(String title) {
		this.title = title;
	}

	public void show() {
		AlertDialog.Builder adb = new AlertDialog.Builder(context);
		adb.setView(generatorView());

		if (generateListener != null) {
			adb.setPositiveButton(generateButtonName, generate);
		}
		if (neutralButtonListener != null) {
			adb.setNeutralButton(neutralButtonName, neutral);
		}
		adb.setCancelable(false);
		final AlertDialog create = adb.create();
		create.show();
		create.getWindow().setLayout((int) (context.getResources().getDisplayMetrics().widthPixels * 0.85), -2);
		btnexit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				create.dismiss();
			}
		});
	}

	private static View generatorView() {
		inflater = LayoutInflater.from(context);
		v = inflater.inflate(R.layout.export_dialog, null);

		edFileName = v.findViewById(R.id.edFileName);
		edPassword = v.findViewById(R.id.edPassword);
		edHwid = v.findViewById(R.id.edHWID);
		edNote = v.findViewById(R.id.edNote);
		edPowered = v.findViewById(R.id.edPowered);
		edBlokApp = v.findViewById(R.id.edBlokApp);

		btnexit = v.findViewById(R.id.btnCancelSaveConfig);
		btnSave = v.findViewById(R.id.btnSaveConf);

		cbPayload = v.findViewById(R.id.isPayloadLock);
		cbSni = v.findViewById(R.id.isSniLock);
		cbSSH = v.findViewById(R.id.isServerLock);
		cbSlow = v.findViewById(R.id.isSlowLock);
        cbudp = v.findViewById(R.id.isudpLock);
		cbv2ray = v.findViewById(R.id.isv2rayock);
		cbALL = v.findViewById(R.id.isLockALL);
		cbExpiry = v.findViewById(R.id.isExpiry);
		cbHwid = v.findViewById(R.id.isHwidLock);
		cbData = v.findViewById(R.id.isLockData);
		cbBlockRoot = v.findViewById(R.id.isBlockRoot);
		cbNote = v.findViewById(R.id.isNote);
		cbPassword = v.findViewById(R.id.isUsePassword);
		cbExtraSniff = v.findViewById(R.id.isExtraSniff);
		cbBlockApp = v.findViewById(R.id.isUseBlokapp);

		llNote = v.findViewById(R.id.llNote);
		llPassword = v.findViewById(R.id.llPassword);
		llHwid = v.findViewById(R.id.llHWID);
		llBlokkApp = v.findViewById(R.id.llBlokApp);

		cbPayload.setOnCheckedChangeListener(cb);
		cbSni.setOnCheckedChangeListener(cb);
		cbSSH.setOnCheckedChangeListener(cb);
		cbExpiry.setOnCheckedChangeListener(cb);
		cbHwid.setOnCheckedChangeListener(cb);
		cbData.setOnCheckedChangeListener(cb);
		cbBlockRoot.setOnCheckedChangeListener(cb);
		cbNote.setOnCheckedChangeListener(cb);
		cbPassword.setOnCheckedChangeListener(cb);
		cbExtraSniff.setOnCheckedChangeListener(cb);
		cbBlockApp.setOnCheckedChangeListener(cb);
		cbALL.setOnCheckedChangeListener(cb);

		return v;
	}

	private DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
			// TODO: Implement this method
			if (cancelListener != null) {
				cancelListener.onCancelListener();
			}

		}
	};

	private DialogInterface.OnClickListener neutral = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface p1, int p2) {
			// TODO: Implement this method
			if (neutralButtonListener != null) {
				neutralButtonListener.onNeutralListener();
			}

		}
	};

	private DialogInterface.OnClickListener generate = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int in) {
			try {

				String sName = edFileName.getText().toString();
				if (sName.isEmpty()) {
					Toast.makeText(context, "file name cant be empty!", 0).show();
					return;
				}
				sp.edit().putString(Settings.NAME_CONFIG, sName).apply();

				Properties mConfigFile = new Properties();
				ByteArrayOutputStream tempOut = new ByteArrayOutputStream();

				Settings settings = new Settings(context);
				SharedPreferences prefs = settings.getPrefsPrivate();

				try {
					int targerId = getBuildId(context);

					//	String hhw = mHwidLock ? edHwid.getText().toString() : "";
					//	String bp = mBlokApp ? edBlokApp.getText().toString() : "";

					mConfigFile.setProperty(Settings.SSH_HARED,
							Integer.toString(prefs.getInt(Settings.SSH_HARED, 0)));
					mConfigFile.setProperty(C_VER, Integer.toString(targerId));
					mConfigFile.setProperty(C_LCK, mIsProteger ? "1" : "0");
					mConfigFile.setProperty(Settings.CONFIG_PROTEGER_KEY, mIsProteger ? "1" : "0");

					boolean stateCpwhy = cbPayload.isChecked();
					boolean stateCswhy = cbSni.isChecked();
					boolean stateCsp = cbSSH.isChecked();
					boolean stateCss = cbSlow.isChecked();
                	boolean stateUdp = cbudp.isChecked();
					boolean stateV2ray = cbv2ray.isChecked();
                    
					boolean statePcc = cbPassword.isChecked();
					boolean stateNtt = cbNote.isChecked();
					boolean stateHcc = cbHwid.isChecked();
					boolean stateBcc = cbBlockApp.isChecked();

					mConfigFile.setProperty(Settings.CP_NOTE, stateNtt ? "1" : "0");
					mConfigFile.setProperty(Settings.CP_HWID, stateHcc ? "1" : "0");
					mConfigFile.setProperty(Settings.CP_BLOCK_APP, stateBcc ? "1" : "0");

					mConfigFile.setProperty(Settings.CP_PAYLOAD, stateCpwhy ? "1" : "0");
					mConfigFile.setProperty(Settings.CP_SNI, stateCswhy ? "1" : "0");
					mConfigFile.setProperty(Settings.CP_SSH, stateCsp ? "1" : "0");
					mConfigFile.setProperty(Settings.CP_DNSTT, stateCss ? "1" : "0");
                    mConfigFile.setProperty(Settings.CP_UDP, stateUdp ? "1" : "0");
					mConfigFile.setProperty(Settings.CP_V2RAY, stateV2ray ? "1" : "0");

					mConfigFile.setProperty("bloquearRoot", isBloquearRoot ? "1" : "0");
					mConfigFile.setProperty(C_VAL, Long.toString(mValidade));
					mConfigFile.setProperty("file.pedirLogin", statePcc ? "1" : "0");
					mConfigFile.setProperty(Settings.I_PASSWORD, edPassword.getText().toString());
					mConfigFile.setProperty(Settings.ED_NOTE, edNote.getText().toString());
					mConfigFile.setProperty(Settings.ED_HWID, edHwid.getText().toString());
					mConfigFile.setProperty(Settings.ED_POWERED, edPowered.getText().toString());
					mConfigFile.setProperty(Settings.ED_BLOCK_AP, edBlokApp.getText().toString());

					mConfigFile.setProperty(Settings.M_DATAT_ONLY, mDataOnly ? "1" : "0");

					String server_me = prefs.getString("SavedSerperString", "");

					String serOvpn = prefs.getString(Settings.USUARIO_OVPN, "");
					String OvpnConf = prefs.getString(Settings.OVPN_CONFIG, "");
					String ovpnHost = prefs.getString(Settings.OVPN_HOST, "");
					String ovpnPort = prefs.getString(Settings.OVPN_PORT, "");

					/*	if (server_me.isEmpty() || serOvpn.isEmpty()) {
							throw new IOException("Server empty");
						}*/

					mConfigFile.setProperty(Settings.USUARIO_OVPN, serOvpn);
					mConfigFile.setProperty(Settings.OVPN_CONFIG, OvpnConf);
					mConfigFile.setProperty(Settings.OVPN_HOST, ovpnHost);
					mConfigFile.setProperty(Settings.OVPN_PORT, ovpnPort);

					mConfigFile.setProperty("SavedSerperString", server_me);
					mConfigFile.setProperty(Settings.PROXY_IP_PORT, prefs.getString(Settings.PROXY_IP_PORT, ""));
					//done Up

					mConfigFile.setProperty(Settings.TUNNELTYPE_KEY,
							Integer.toString(prefs.getInt(Settings.TUNNELTYPE_KEY, 0)));

					mConfigFile.setProperty(Settings.TUNNELTYPE_KEY,
							Integer.toString(prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT)));

					mConfigFile.setProperty(Settings.DNSFORWARD_KEY, settings.getVpnDnsForward() ? "1" : "0");
					mConfigFile.setProperty(Settings.DNSRESOLVER_KEY1, settings.getVpnDnsResolver1());
					mConfigFile.setProperty(Settings.DNSRESOLVER_KEY2, settings.getVpnDnsResolver2());

					mConfigFile.setProperty(Settings.UDPFORWARD_KEY, settings.getVpnUdpForward() ? "1" : "0");
					mConfigFile.setProperty(Settings.UDPRESOLVER_KEY, settings.getVpnUdpResolver());

					String customPayload = prefs.getString(Settings.CUSTOM_PAYLOAD_KEY, "");
					String ssl = prefs.getString(Settings.CUSTOM_SNI, "");

					String chave = prefs.getString(Settings.CHAVE_KEY, "");
					String nameserver = prefs.getString(Settings.NAMESERVER_KEY, "");
					String dns = prefs.getString(Settings.DNS_KEY, "");
                    
                    String udpdo = prefs.getString(Settings.UDP_DOWN, "");
					String udpup = prefs.getString(Settings.UDP_UP, "");
					String udpbu = prefs.getString(Settings.UDP_BUFFER, "");
                    String v2 = prefs.getString(Settings.V2RAY_JSON, "");
					

					int tunnelType = prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);
					if (tunnelType == Settings.bTUNNEL_TYPE_SLOWDNS) {
						if (mIsProteger && (chave.isEmpty() || nameserver.isEmpty() || dns.isEmpty())) {
							throw new IOException();
						}
					}

					String isDefaultPayload = prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true) ? "1" : "0";
					mConfigFile.setProperty(Settings.PROXY_USAR_DEFAULT_PAYLOAD, isDefaultPayload);
					mConfigFile.setProperty(Settings.CUSTOM_PAYLOAD_KEY, customPayload);
					mConfigFile.setProperty(Settings.CUSTOM_SNI, ssl);

					mConfigFile.setProperty(Settings.CHAVE_KEY, chave);
					mConfigFile.setProperty(Settings.NAMESERVER_KEY, nameserver);
					mConfigFile.setProperty(Settings.DNS_KEY, dns);
                    
                    mConfigFile.setProperty(Settings.UDP_DOWN, udpdo);
					mConfigFile.setProperty(Settings.UDP_UP, udpup);
					mConfigFile.setProperty(Settings.UDP_BUFFER, udpbu);
					mConfigFile.setProperty(Settings.V2RAY_JSON, v2);
                    
                    

					boolean isPayloadChecked = prefs.getBoolean(Settings.CB_PAYLOLOAD, false) == true;
					mConfigFile.setProperty(Settings.CB_PAYLOLOAD, isPayloadChecked ? "1" : "0");

					boolean isSniChecked = prefs.getBoolean(Settings.CB_SNI, false) == true;
					mConfigFile.setProperty(Settings.CB_SNI, isSniChecked ? "1" : "0");

					boolean isSlowChecked = prefs.getBoolean(Settings.CB_DNSTT, false) == true;
					mConfigFile.setProperty(Settings.CB_DNSTT, isSlowChecked ? "1" : "0");
                    
                    boolean isudpChecked = prefs.getBoolean(Settings.CP_UDP, false) == true;
					mConfigFile.setProperty(Settings.CP_UDP, isudpChecked ? "1" : "0");
                    
                    boolean isv2rayChecked = prefs.getBoolean(Settings.CP_V2RAY, false) == true;
					mConfigFile.setProperty(Settings.CP_V2RAY, isv2rayChecked ? "1" : "0");

					boolean isReplaceChecked = prefs.getBoolean(Settings.AUTO_REPLACE, false) == true;
					mConfigFile.setProperty(Settings.AUTO_REPLACE, isReplaceChecked ? "1" : "0");

				} catch (Exception e) {
					throw new IOException("Errorrrr");
				}

				try {
					mConfigFile.storeToXML(tempOut, "");
				} catch (FileNotFoundException e) {
					throw new IOException("File Not Found");
				} catch (IOException e) {
					throw new IOException("Error Unknown", e);
				}

				try {
					InputStream input_encoded = ResetDefault
							.encodeInput(new ByteArrayInputStream(tempOut.toByteArray()));
							
				//	sp.edit().putString(Settings.S_SAVE, new String(tempOut.toByteArray())).apply();
				
					sp.edit().putString(Settings.S_SAVE, isToString(input_encoded)).apply();
					generateListener.onGenerate("y");
				} catch (Throwable e) {
					throw new IOException("Eroor");
				}
			} catch (IOException e) {
			}
		}
	};

	private static String isToString(InputStream is) throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString();
	}

	
	private static CompoundButton.OnCheckedChangeListener cb = new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton p1, boolean p2) {
			switch (p1.getId()) {
			case R.id.isPayloadLock:
				if (p2) {
					cPpp = true;
					if (!sp.getBoolean(Settings.CB_PAYLOLOAD, false)) {
						cbPayload.setChecked(false);
						showToast("Payload not checked");
					}
				} else {
					cPpp = false;
				}
				break;
			case R.id.isSniLock:
				if (p2) {
					cSss = true;
					if (!sp.getBoolean(Settings.CB_SNI, false)) {
						cbSni.setChecked(false);
						showToast("SNI not checked");
					}
				} else {
					cSss = false;
				}
				break;
			case R.id.isServerLock:
				cSp = p2;
				break;
			case R.id.isSlowLock:
				cSs = p2;
				break;
            case R.id.isudpLock:
				Udp = p2;
				break;
            case R.id.isv2rayock:
				V2ray = p2;
				break;
			case R.id.isLockALL:
				cbPayload.setChecked(p2);
				cbSni.setChecked(p2);
				cbSSH.setChecked(p2);
				cbSlow.setChecked(p2);
                cbudp.setChecked(p2);
				cbv2ray.setChecked(p2);
				break;
			case R.id.isExpiry:
				if (p2) {
					setValidadeDate();
				} else {
					mValidade = 0;
					if (cbExpiry != null) {
						cbExpiry.setText("Expired");
					}
				}
				break;
			case R.id.isHwidLock:
				//	mHwidLock = p2;
				if (p2) {
					llHwid.setVisibility(View.VISIBLE);
				} else {
					llHwid.setVisibility(View.GONE);
				}
				break;
			case R.id.isLockData:
				mDataOnly = p2;
				if (p2) {
				} else {
				}
				break;
			case R.id.isBlockRoot:
				isBloquearRoot = p2;
				if (p2) {
				} else {
				}
				break;
			case R.id.isNote:
				//	mIsNote = p2;
				if (p2) {
					llNote.setVisibility(View.VISIBLE);
				} else {
					llNote.setVisibility(View.GONE);
				}
				break;
			case R.id.isUsePassword:
				//	mPedirSenha = p2;
				if (p2) {
					llPassword.setVisibility(View.VISIBLE);
				} else {
					llPassword.setVisibility(View.GONE);
				}
				break;
			case R.id.isUseBlokapp:
				//	mBlokApp = p2;
				if (p2) {
					llBlokkApp.setVisibility(View.VISIBLE);
				} else {
					llBlokkApp.setVisibility(View.GONE);
				}
				break;
			case R.id.isExtraSniff:
				if (p2) {
				} else {
				}
				break;
			}
		}
	};

	private static void setValidadeDate() {

		// Get Current Date
		Calendar c = Calendar.getInstance();
		final long time_hoje = c.getTimeInMillis();

		c.setTimeInMillis(time_hoje + (1000 * 60 * 60 * 24));

		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);

		mValidade = c.getTimeInMillis();

		final DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker p1, int year, int monthOfYear, int dayOfMonth) {
				Calendar c = Calendar.getInstance();
				c.set(year, monthOfYear, dayOfMonth);

				mValidade = c.getTimeInMillis();
			}
		}, mYear, mMonth, mDay);

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Oke", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog2, int which) {
				DateFormat df = DateFormat.getDateInstance();
				DatePicker date = dialog.getDatePicker();

				Calendar c = Calendar.getInstance();
				c.set(date.getYear(), date.getMonth(), date.getDayOfMonth());

				mValidade = c.getTimeInMillis();

				if (mValidade < time_hoje) {
					mValidade = 0;

					Toast.makeText(context, "Invalid Date", Toast.LENGTH_SHORT).show();

					if (cbExpiry != null)
						cbExpiry.setChecked(false);
				} else {
					long dias = ((mValidade - time_hoje) / 1000 / 60 / 60 / 24);

					if (cbExpiry != null) {
						String Test = df.format(mValidade).replace(" ", "/");
						cbExpiry.setText("Set Date Expired: " + Test);
						//cbExpiry.setText(String.format("Set Date Expired: %s (%s)", dias, df.format(mValidade)));
					}
				}
			}
		});

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mValidade = 0;

				if (cbExpiry != null) {
					cbExpiry.setChecked(false);
				}
			}
		});

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface v1) {
				mValidade = 0;
				if (cbExpiry != null) {
					cbExpiry.setChecked(false);
				}
			}
		});

		dialog.show();
	}

	public static boolean isValidadeExpirou(long validadeDateMillis) {
		if (validadeDateMillis == 0) {
			return false;
		}

		// Get Current Date
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
		//boolean experiementalTests = rootBeer.checkForMagiskNative();

		return simpleTests;
	}

}