package com.fdx.injector;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowMetrics;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.amrdeveloper.codeview.CodeView;
import com.fdx.injector.ActivityTools.HostChecker;
import com.fdx.injector.ActivityTools.IpHunter;
import com.fdx.injector.ActivityTools.MainActivityWifi;
import com.fdx.injector.ActivityTools.MyIP_Address;
import com.fdx.injector.ActivityTools.ping;
import com.fdx.injector.ActivityTools.scanports;
import com.fdx.injector.ActivityTools.subfinder;
import com.fdx.injector.activities.AboutActivity;
import com.fdx.injector.activities.BaseActivity;
import com.fdx.injector.adapter.LogsAdapter;
import com.fdx.injector.adapter.PageAdapter;
import com.fdx.injector.coreservice.PsiphonService;
import com.fdx.injector.coreservice.SOCKETService;
import com.fdx.injector.coreservice.WireGuardService;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.coreservice.logger.ConnectionStatus;
import com.fdx.injector.coreservice.logger.SkStatus;
import com.fdx.injector.coreservice.tunnel.TunnelManagerHelper;
import com.fdx.injector.coreservice.tunnel.TunnelUtils;
import com.fdx.injector.coreservice.tunnel.UDPListener;
import com.fdx.injector.coreservice.tunnel.UDPTunnel;
import com.fdx.injector.coreservice.tunnel.V2Tunnel;
import com.fdx.injector.coreservice.util.FileUtils;
import com.fdx.injector.coreservice.util.securepreferences.SecurePreferences;
import com.fdx.injector.fragment.FragmentDialogLock;
import com.fdx.injector.util.ResetDefault;
import com.fdx.injector.util.Utils;
import com.fdx.injector.view.ExportConfig;
import com.fdx.injector.view.ImportConfig;
import com.fdx.injector.view.PayloadGenerator;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import io.github.muddz.styleabletoast.StyleableToast;



public class MainActivity extends BaseActivity
		implements
		OnClickListener,
		SkStatus.StateListener {

	public static final int START_VPN_PROFILE = 32002;
	private static final String NOTIFICATION_PERMISSION = "android.permission.POST_NOTIFICATIONS";
	private static final int PERMISSION_REQUEST_CODE = 1;
	public static SharedPreferences mPrefs;
	public static SharedPreferences sShared;
	public ActionBarDrawerToggle actionBarDrawerToggle;
	public BottomSheetBehavior<View> bottomSheetBehavior;
	public View bottomView;

	private CardView psiphonmode;
	private Spinner mPsiphonProtocolSpinner;
	private Spinner mPsiphonRegionSpinner;
	private EditText mPsiphonAuthEditText;
	private EditText mPsiphonServerEntryEditText;
	private Settings mSettings;

	private CardView trojanmode;
	private EditText trojanRemarksEditText, trojanAddressEditText, trojanPortEditText, trojanPasswordEditText, trojanSniEditText;

	public CheckBox cbAutoRp;
	public CheckBox isUsePayloadOvpn;
	public CheckBox isSSLOvpn;
	public CheckBox cbDetaillog;
	public CheckBox cbDns;
	public CheckBox isUDPGW;
	public CheckBox cbPayload;
	public CheckBox cbSni;
	public CheckBox cbStatuslog;
	public CheckBox cbWakelock;
	public CoordinatorLayout coordinatorLayout;
	public DrawerLayout drawerLayout;
	private ViewPager viewPager;
	private LogsAdapter logsAdapter;
	private static UDPListener udpListener;
	private ConnectivityManager connectivityManager;
	private ConnectivityManager.NetworkCallback callback;
	private UDPTunnel udpTunnel;
	private V2Tunnel v2Tunnel;

	public EditText edIn;
	public EditText edOvpnUserPass;

	public FloatingActionButton mExportConfig, mImportCfg, mImportOvpn, mAddFab;
	public TextView addSaveConfig, mImportOvpnTv, mImportCfgTv;
	public Boolean isAllFabsVisible;

	public LinearLayout linearLayout;
	public LinearLayout linearLogLevel;
	public LinearLayout linearovpndetail;
	public LinearLayout llNote;



	//test
	public LinearLayout sshmode ;
	public CardView wgmode;
	public EditText wgConfigEditText;
	public LinearLayout ovpnmode;
	public TextInputLayout ipssh;
	public TextInputLayout ipvpn;
	public LinearLayout laysni;
	public LinearLayout layproxy;
	public LinearLayout layPayload;
	public CheckBox isProxy;
	public CheckBox isPayload;
	public CheckBox isUseSSL;
	private CardView ovpn2;
	private CardView v2v;
	private CardView slowdns;
	private CardView udpsse;

	public static boolean isHomeTab = true;






	public RecyclerView logList;
	public TextView logTv;
	public TextView logNote;
	public Settings mConfig;
	public Handler mHandler;
	public LogsAdapter mLogAdapter;
	public TextView mmk;
	public ProgressDialog pd;
	public SeekBar seekBar;
	public Button starterButton;
	public TabLayout tabs;
	public Toolbar toolbar_main;

	//test my new home
	private Spinner tunSpin;

	private MaterialCardView card_tools1;
	private MaterialCardView card_tools2;
	private MaterialCardView card_tools3;
	private MaterialCardView card_tools4;
	private MaterialCardView card_tools5;
	private MaterialCardView card_tools6;
	private MaterialCardView card_tools7;
	private MaterialTextView socketIp;
	private MaterialTextView socketnet;
	//ads
	private static final String TAG = "MainActivity";
	private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";
	private AdView adView;
	private FrameLayout adContainerView;
	private FrameLayout adContainerVieww;
	private AdView mAdView;
	private AdView mAdVieww;
	private AdView mAdViewww;
	private boolean initialLayoutComplete = false;
	private InterstitialAd mInterstitialAd;

	private Context mContext;
	public ViewPager vp;
	public Menu customMenu;
	public static boolean isLocked = true;
	public static final String[] tabTitle = { "HOME", "LOGS", "TOOLS"};
	public boolean mhideLog = false;
	public boolean JembutZ = false;
	boolean vpnStart = false;
	private OpenVPNThread vpnThread = new OpenVPNThread();
	private OpenVPNService vpnService = new OpenVPNService();

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.fdx.injector.MainActivity.2
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				MainActivity.this.setStatus(intent.getStringExtra("state"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	private NavigationView drawerNavigationView;

	public void showToast(String str) {
		StyleableToast.makeText(getApplicationContext(), str, R.style.mytoast).show();
	}
	public boolean disableEnable(boolean z) {
		this.edIn.setEnabled(z);
		this.tunSpin.setEnabled(z);
		this.edOvpnUserPass.setEnabled(z);
		this.cbPayload.setEnabled(z);
		this.cbSni.setEnabled(z);
		this.cbDns.setEnabled(z);
		this.isUDPGW.setEnabled(z);
		this.cbWakelock.setEnabled(z);
		this.cbAutoRp.setEnabled(z);
		this.cbStatuslog.setEnabled(z);
		this.cbDetaillog.setEnabled(z);
		this.udpsse.setEnabled(z);
		this.slowdns.setEnabled(z);
		this.v2v.setEnabled(z);
		this.ovpn2.setEnabled(z);
		this.isSSLOvpn.setEnabled(z);
		this.isUsePayloadOvpn.setEnabled(z);
		isLocked = z;
		return true;
	}

	public void showPd(Context context) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		this.pd = progressDialog;
		progressDialog.setMessage("Please wait");
		this.pd.setCancelable(false);
		this.pd.show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
				doLayout();
			}
		}, 1300L);
	}

	public void edLocked(EditText editText) {
		editText.setEnabled(isLocked);
	}

	public void isServiceRunning() {
		setStatus(vpnService.getStatus());
	}

	@SuppressWarnings("deprecation")
	public void setStatus(String connectionState) {
		final String cleanLogMessage = VpnStatus.getLastCleanLogMessage(getApplicationContext());
		if (connectionState != null)
			switch (connectionState) {
				case "CONNECTING":
					logTv.setText(vpnService.getStatus());
					break;
				case "WAIT":
					logTv.setText(vpnService.getStatus());
					break;
				case "AUTH":
					logTv.setText(cleanLogMessage);
					break;
				case "GET_CONFIG":
					logTv.setText(cleanLogMessage);
					break;
				case "ASSIGN_IP":
					logTv.setText(cleanLogMessage);
					break;
				case "ADD_ROUTES":
					logTv.setText(cleanLogMessage);
					break;
				case "CONNECTED":
					sShared.edit().putBoolean(Settings.CHZ_26, true).apply();
					disableEnable(false);
					ResetDefault.trimCache(this);
					vpnStart = true;
					status("connected");
					logTv.setText(cleanLogMessage);
					break;
				case "DISCONNECTED":
					sShared.edit().putBoolean(Settings.CHZ_26, false).apply();
					disableEnable(true);
					vpnStart = false;
					vpnService.setDefaultStatus();
					status("connect");
					logTv.setText("No process running");
					break;
				case "RECONNECTING":
					logTv.setText(vpnService.getStatus());
					status("connecting");
					break;
				case "EXITING":
					logTv.setText(vpnService.getStatus());
					break;
				case "RESOLVE":
					logTv.setText(vpnService.getStatus());
					break;
				case "TCP_CONNECT":
					ResetDefault.trimCache(this);
					logTv.setText(cleanLogMessage);
					break;
				case "AUTH_PENDING":
					logTv.setText(vpnService.getStatus());
					break;
				case "NONETWORK":
					logTv.setText(vpnService.getStatus());
					break;
			}
	}

	public void status(String str) {
		if (str.equals("connect")) {
			this.starterButton.setText(getApplicationContext().getString(R.string.connect));
			disableEnable(true);
		} else if (str.equals("connecting")) {
			disableEnable(false);
			this.starterButton.setText(getApplicationContext().getString(R.string.connecting));
		} else if (str.equals("connected")) {
			disableEnable(false);
			this.starterButton.setText(getApplicationContext().getString(R.string.disconnect));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
		adsPopUp();
		LocalBroadcastManager.getInstance(this).registerReceiver(this.broadcastReceiver,
				new IntentFilter("connectionState"));
		SkStatus.addStateListener(this);

		tools3();
		tools2();
	}

	@Override
	public void onPause() {
		super.onPause();
		doSaveData();
		if (adView != null) {
			adView.pause();
		}
		adsPopUp();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(this.broadcastReceiver);
		SkStatus.removeStateListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	// Fora Do OnCreate ☑️

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// sua permissão de notificações foi negada
				// seu aplicativo pode mostrar notificações
			} else {
				// mostrar permissões de notificações foi negado
				// Informa ao usuario que seu aplicativo no mostrará notificações
			}
		}
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	/*	if (!chetoszclass.z(getApplicationContext()).equals(chetoszclass.x())) {
			return;
		}*/
		mSettings = new Settings(this);
		this.mHandler = new Handler();
		Settings settings = new Settings(this);
		this.mConfig = settings;
		SecurePreferences prefsPrivate = settings.getPrefsPrivate();
		sShared = this.mConfig.getPrefsPrivate();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!prefsPrivate.getBoolean(Settings.CHZ_21, false)) {
			prefsPrivate.edit().putBoolean(Settings.CHZ_21, true).apply();
			prefsPrivate.edit().putBoolean(Settings.CHZ_26, false).apply();
			prefsPrivate.edit().putInt(Settings.SSH_HARED, 0).apply();
		}
		doLayout();
		//newhome();
		setupData();
		setupDatOVPN();
		isServiceRunning();
		VpnStatus.initLogCache(getCacheDir());
		FileUtils.requestForPermissionExternalStorage(this);
		ImportConfig impconf = new ImportConfig(this);
		ResetDefault resDef = new ResetDefault(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(NOTIFICATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{NOTIFICATION_PERMISSION}, PERMISSION_REQUEST_CODE);
		}





	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);

		customMenu = menu;

	/*	if (vp.getCurrentItem() == 1) {
			if (sShared.getInt(Settings.SSH_HARED, 0) == 1) {
				customMenu.findItem(R.id.ovpnConfig).setVisible(false);
				customMenu.findItem(R.id.slowDnsSet).setVisible(false);
                customMenu.findItem(R.id.pluginsettings).setVisible(false);
			} else {
				customMenu.findItem(R.id.ovpnConfig).setVisible(false);
				customMenu.findItem(R.id.slowDnsSet).setVisible(false);
                customMenu.findItem(R.id.pluginsettings).setVisible(false);
			}
			customMenu.findItem(R.id.mclearLog).setVisible(true);

		} else {
			if (sShared.getInt(Settings.SSH_HARED, 0) == 1) {
				customMenu.findItem(R.id.ovpnConfig).setVisible(true);
                customMenu.findItem(R.id.pluginsettings).setVisible(false);
				customMenu.findItem(R.id.slowDnsSet).setVisible(false);
			} else {
				customMenu.findItem(R.id.ovpnConfig).setVisible(false);
                customMenu.findItem(R.id.pluginsettings).setVisible(true);
				customMenu.findItem(R.id.slowDnsSet).setVisible(true);
			}
			customMenu.findItem(R.id.mclearLog).setVisible(false);

		}*/

		boolean isAutoClearChecked = sShared.getBoolean(Settings.AUTO_CLEAR_LOGS_KEY, false);
		MenuItem item = menu.findItem(R.id.AutoClearLog);
		item.setChecked(isAutoClearChecked);

		boolean isDataCompressChecked = MainApp.dsp.getBoolean(Settings.SSH_COMPRESSION, false);
		MenuItem item2 = menu.findItem(R.id.DataCompress);
		item2.setChecked(isDataCompressChecked);

		boolean isHotspotChecked = MainApp.sp.getBoolean(Settings.HOT_SPOT, false);
		MenuItem item3 = menu.findItem(R.id.hotspotTethring);
		item3.setChecked(isHotspotChecked);

		boolean isDisableTcpChecked = MainApp.sp.getBoolean(Settings.DISABLE_DELAY_KEY, false);
		MenuItem item4 = menu.findItem(R.id.disableTcpDelay);
		item4.setChecked(isDisableTcpChecked);

		return true;
	}

	public void Kuda() {
		DialogFragment fragLck = new FragmentDialogLock();
		fragLck.show(getSupportFragmentManager(), "Poni");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();
		//	int itemId = menuItem.getItemId();
		if (this.actionBarDrawerToggle.onOptionsItemSelected(menuItem)) {
			return true;
		}

		switch (menuItem.getItemId()) {

			case R.id.mclearLog:
				mLogAdapter.clearLog();
				break;



			case R.id.myExit:
				Process.killProcess(Process.myPid());
				//	sShared.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY).apply();
				break;

			case R.id.hapusKeDefault:
				if (!MainActivity.isLocked) {
					MainActivity.this.showToast("Vpn is Running");
					return false;
				}
				ResetDefault.resetToBasic();
				sShared.edit().remove(Settings.CP_PAYLOAD).apply();
				sShared.edit().remove(Settings.CP_SNI).apply();
				sShared.edit().remove(Settings.CP_SSH).apply();
				sShared.edit().remove(Settings.CP_DNSTT).apply();
				sShared.edit().remove(Settings.CP_UDP).apply();
				sShared.edit().remove(Settings.CP_V2RAY).apply();
				sShared.edit().remove(Settings.V2RAY_JSON).apply();
				doLayout();
				break;
			case R.id.AutoClearLog:
				menuItem.setChecked(!menuItem.isChecked());
				sShared.edit().putBoolean(Settings.AUTO_CLEAR_LOGS_KEY, menuItem.isChecked()).apply();
				break;
			case R.id.DataCompress:
				menuItem.setChecked(!menuItem.isChecked());
				MainApp.dsp.edit().putBoolean(Settings.SSH_COMPRESSION, menuItem.isChecked()).apply();
				break;
			case R.id.disableTcpDelay:
				menuItem.setChecked(!menuItem.isChecked());
				MainApp.sp.edit().putBoolean(Settings.DISABLE_DELAY_KEY, menuItem.isChecked()).apply();
				break;
			case R.id.hotspotTethring:
				if (ResetDefault.getSockIP() == null) {
					showToast("Turn on MOBILE Hotspot & Reconnect if you are connected");
					VpnStatus.logWarning(
							"<font color='#FF5733'>Turn on MOBILE Hotspot & Reconnect if you are connected</font>");
				}
				menuItem.setChecked(!menuItem.isChecked());
				sShared.edit().putBoolean(Settings.HOT_SPOT, menuItem.isChecked()).apply();
				break;
		}
		return super.onOptionsItemSelected(menuItem);
	}

	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		final SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();
		prefsPrivate.edit();
		int id = item.getItemId();
		switch (id) {
			case R.id.dnsOpen:
				drawerLayout.closeDrawers();
				addDnsDialog();
				break;

			case R.id.payGen:
				MainActivity.this.drawerLayout.closeDrawers();
				if (!MainActivity.isLocked) {
					showToast("Vpn is Running");
					break;
				} else {
					showPayloadGenerator();
					break;
				}
			case R.id.payloadOpen:
				drawerLayout.closeDrawers();
				addPayloadDialog();
				break;
			case R.id.sniOpen:
				drawerLayout.closeDrawers();
				addSniDialog();
				break;

			case R.id.udpgwOpen:
				drawerLayout.closeDrawers();
				addUdpgwDialog();
				break;
			case R.id.hwidOpen:
				drawerLayout.closeDrawers();
				ResetDefault.copyToClipboard(MainActivity.this, ResetDefault.getHWID());
				showToast("Hwid copied to clipboard");
				break;
			case R.id.aboutapp:
				drawerLayout.closeDrawers();
				startActivity(new Intent(MainActivity.this, AboutActivity.class));
				break;
		}
		if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
			drawerLayout.closeDrawers();
		}
		return true;
	}

	public void HmmClose() {
		if (sShared.getBoolean(Settings.AUTO_CLEAR_LOGS_KEY, false)) {
			mLogAdapter.clearLog();
		}
	}



	private void prepareVpn() {
		if (sShared.getInt(Settings.SSH_HARED, 0) == 1) {
			if (!vpnStart) {
				Intent intent = VpnService.prepare(this);
				if (intent != null) {
					startActivityForResult(intent, 111);
				} else
					HmmClose();
				startVpn();
				status("connecting");
			} else if (stopVpn()) {
				ResetDefault.trimCache(this);
			}
		}
	}



	/*private void startVpn() {
		try {
			startService(new Intent(this, SOCKETService.class).setAction("START"));
			vpnStart = true;
			logTv.setText("Connecting...");
		} catch (Exception e) {
		}
	}*/

	private void startVpn() {
		// قراءة نوع النفق الذي اختاره المستخدم
		int tunnelType = mConfig.getPrefsPrivate().getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);

		// استخدام switch لتشغيل الخدمة الصحيحة
		switch (tunnelType) {
			case Settings.bTUNNEL_TYPE_SSH_DIRECT:
			case Settings.bTUNNEL_TYPE_SSH_PROXY:
			case Settings.bTUNNEL_TYPE_SSH_SSL:
			case Settings.bTUNNEL_TYPE_SLOWDNS:
			case Settings.bTUNNEL_TYPE_UDP:
				// 1. إعداد بيانات SSH
				setupData();
				// 2. بدء الخدمة الرئيسية التي تشغل TunnelManagerThread
				TunnelManagerHelper.startSocksHttp(this);
				break;

			case Settings.bTUNNEL_TYPE_PSIPHON: // <<<==== أضف هذه الحالة
				Intent psiphonIntent = new Intent(this, PsiphonService.class);
				startService(psiphonIntent);
				break;

			case Settings.bTUNNEL_TYPE_OPENVPN:
				// 1. إعداد بيانات OVPN
				setupDatOVPN();
				// 2. بدء اتصال OpenVPN
				String profileUUID = mSettings.getOvpnProfileUUID();
				if (profileUUID == null) {
					showToast("Please import/select an OpenVPN profile first.");
					return;
				}
				VpnProfile ovpnProfile = ProfileManager.get(this, profileUUID);
				if (ovpnProfile == null) {
					showToast("Error: Saved OpenVPN profile not found.");
					mSettings.setOvpnProfileUUID(null);
					return;
				}
				VPNLaunchHelper.startOpenVpn(ovpnProfile, this);
				break;

			case Settings.bTUNNEL_TYPE_V2RAY:
				// بدء خدمة V2Ray عبر الدالة المساعدة
				//TunnelManagerHelper.startV2Ray(this);
				break;

			case Settings.bTUNNEL_TYPE_WIREGUARD:
				// بدء خدمة WireGuard الجديدة مباشرة
				Intent wgIntent = new Intent(this, WireGuardService.class);
				wgIntent.setAction(WireGuardService.ACTION_CONNECT);
				startService(wgIntent);
				break;

			default:
				showToast("Invalid tunnel type selected");
				break;
		}
	}

	public boolean stopVpn() {
		try {
			vpnThread.stop();
			startService(new Intent(this, SOCKETService.class).setAction("STOP"));
			vpnStart = false;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	void fabbb() {
		mAddFab = findViewById(R.id.add_fab);
		mExportConfig = findViewById(R.id.fab1);
		mImportOvpn = findViewById(R.id.fab2);
		mImportCfg = findViewById(R.id.fab3);

		addSaveConfig = findViewById(R.id.export_config_text);
		mImportOvpnTv = findViewById(R.id.import_ovpn_text);
		mImportCfgTv = findViewById(R.id.import_cfg_text);

		mExportConfig.setVisibility(View.GONE);
		mImportOvpn.setVisibility(View.GONE);
		mImportCfg.setVisibility(View.GONE);

		addSaveConfig.setVisibility(View.GONE);
		mImportOvpnTv.setVisibility(View.GONE);
		mImportCfgTv.setVisibility(View.GONE);

		isAllFabsVisible = false;
		mAddFab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!isAllFabsVisible) {
					mExportConfig.show();
					mImportCfg.show();
					addSaveConfig.setVisibility(View.VISIBLE);
					mImportCfgTv.setVisibility(View.VISIBLE);
					if (sShared.getInt(Settings.SSH_HARED, 0) == 1) {
						mImportOvpn.show();
						mImportOvpnTv.setVisibility(View.VISIBLE);
					}
					isAllFabsVisible = true;
				} else {
					mExportConfig.hide();
					mImportCfg.hide();
					addSaveConfig.setVisibility(View.GONE);
					mImportCfgTv.setVisibility(View.GONE);
					if (sShared.getInt(Settings.SSH_HARED, 0) == 1) {
						mImportOvpn.hide();
						mImportOvpnTv.setVisibility(View.GONE);
					}
					isAllFabsVisible = false;
				}
			}
		});
		mImportCfg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				filePickerConfig();
				mExportConfig.hide();
				mImportCfg.hide();
				addSaveConfig.setVisibility(View.GONE);
				mImportCfgTv.setVisibility(View.GONE);
				if (sShared.getInt(Settings.SSH_HARED, 0) == 1) {
					mImportOvpn.hide();
					mImportOvpnTv.setVisibility(View.GONE);
				}
				isAllFabsVisible = false;
			}
		});
		mImportOvpn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				startFilePicker();
				mExportConfig.hide();
				mImportCfg.hide();
				addSaveConfig.setVisibility(View.GONE);
				mImportCfgTv.setVisibility(View.GONE);
				if (sShared.getInt(Settings.SSH_HARED, 0) == 1) {
					mImportOvpn.hide();
					mImportOvpnTv.setVisibility(View.GONE);
				}
				isAllFabsVisible = false;
			}
		});
		mExportConfig.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showExportConfigDialog();
				mExportConfig.hide();
				mImportCfg.hide();
				addSaveConfig.setVisibility(View.GONE);
				mImportCfgTv.setVisibility(View.GONE);
				if (sShared.getInt(Settings.SSH_HARED, 0) == 1) {
					mImportOvpn.hide();
					mImportOvpnTv.setVisibility(View.GONE);
				}
				isAllFabsVisible = false;
			}
		});
	}

	public void allAboutConf() {
		llNote = findViewById(R.id.NoteLL);
		logNote = findViewById(R.id.logNote);

		if (sShared.getBoolean(Settings.CP_NOTE, false)) {
			llNote.setVisibility(View.VISIBLE);
			logNote.setText(Html.fromHtml(sShared.getString(Settings.ED_NOTE, "")));
		} else {
			llNote.setVisibility(View.GONE);
			logNote.setText("");
		}
	}
	public void hype(View v)
	{
		AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);

		builder1.setTitle(Html.fromHtml("<b>Join Telegram<b>"));
		builder1.setMessage(Html.fromHtml("Telegram channel: <br><a href=\"https://t.me/socket_tunnel_vpn/?ref=share\">https://t.me/socket_tunnel_vpn/?ref=share</a><br><br><br>Telegram Group: <br><a href=\"https://t.me/socket_tunnel_plus/?ref=share\">https://t.me/socket_tunnel_plus/?ref=share</a>"));

		builder1.setCancelable(false);
		builder1.setPositiveButton("ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog Alert1 = builder1.create();
		Alert1 .show();
		((TextView)Alert1.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

	}
	private void tools3() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (mWifi.isConnected())
		{
			socketIp.setText(TunnelUtils.getLocalIpAddress());
			//toolbar_main.setSubtitleTextAppearance(this, R.style.Toolbar_NetworkStateSubTitleText);

		} else if (mMobile.isConnected()) {

			TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			socketIp.setText(TunnelUtils.getLocalIpAddress());
			///	toolbar_main.setSubtitleTextAppearance(this, R.style.Toolbar_NetworkStateSubTitleText);

		} else {
			socketIp.setText("0.0.0.0");
			///	toolbar_main.setSubtitleTextAppearance(this, R.style.Toolbar_NetworkStateSubTitleText);
		}
	}

	private void tools2() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (mWifi.isConnected())
		{

			socketnet.setText("WIFI");
			//conType.setText(this, R.style.Toolbar_NetworkStateSubTitleText);

		} else if (mMobile.isConnected()) {

			TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			socketnet.setText(manager.getNetworkOperatorName());
			///toolbar_main.setSubtitleTextAppearance(this, R.style.Toolbar_NetworkStateSubTitleText);

		} else {
			socketnet.setText("Not Connected internet");
			///toolbar_main.setSubtitleTextAppearance(this, R.style.Toolbar_NetworkStateSubTitleText);
		}
	}

	public void loadInterstitialAds() {
		AdRequest adRequest = new AdRequest.Builder().build();
		InterstitialAd.load(
				this,
				("ca-app-pub-3940256099942544/1033173712"),
				adRequest,
				new InterstitialAdLoadCallback() {
					@Override
					public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
						// The mInterstitialAd reference will be null until
						// an ad is loaded.
						MainActivity.this.mInterstitialAd = interstitialAd;
						// Log.i(TAG, "onAdLoaded");
						// Toast.makeText(MyActivity.this, "onAdLoaded()",
						// Toast.LENGTH_SHORT).show();
						mInterstitialAd.setFullScreenContentCallback(
								new FullScreenContentCallback() {
									@Override
									public void onAdDismissedFullScreenContent() {
										// Called when fullscreen content is dismissed.
										// Make sure to set your reference to null so you don't
										// show it a second time.
										MainActivity.this.mInterstitialAd = null;
										// Log.d("TAG", "The ad was dismissed.");
									}

									@Override
									public void onAdFailedToShowFullScreenContent(AdError adError) {
										// Called when fullscreen content failed to show.
										// Make sure to set your reference to null so you don't
										// show it a second time.
										MainActivity.this.mInterstitialAd = null;
										// Log.d("TAG", "The ad failed to show.");
									}

									@Override
									public void onAdShowedFullScreenContent() {
										// Called when fullscreen content is shown.
										// Log.d("TAG", "The ad was shown.");
									}
								});
					}

					@Override
					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
						// Handle the error
						// Log.i(TAG, loadAdError.getMessage());
						mInterstitialAd = null;

						// String error = String.format("domain: %s, code: %d, message: %s",
						// loadAdError.getDomain(), loadAdError.getCode(),
						// loadAdError.getMessage());
						// Toast.makeText(MyActivity.this, "onAdFailedToLoad() with error: " +
						// error, Toast.LENGTH_SHORT).show();
					}
				});
	}

	private void adsPopUp() {
		if (mInterstitialAd != null) {
			mInterstitialAd.show(this);
		} else {
			startGame();
		}
	}

	private void startGame() {
		if (mInterstitialAd == null) {
			loadInterstitialAds();

		}
	}

	private void loadBanner() {
		// Create a new ad view.
		adView = new AdView(this);
		adView.setAdUnitId(AD_UNIT_ID);
		adView.setAdSize(getAdSize());

		// Replace ad container with new ad view.
		adContainerView.addView(adView);

		// Start loading the ad in the background.
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}


	// Determine the screen width (less decorations) to use for the ad width.
	private AdSize getAdSize() {
		WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
		Rect bounds = windowMetrics.getBounds();

		float adWidthPixels = adContainerView.getWidth();

		// If the ad hasn't been laid out, default to the full screen width.
		if (adWidthPixels == 0f) {
			adWidthPixels = bounds.width();
		}

		float density = getResources().getDisplayMetrics().density;
		int adWidth = (int) (adWidthPixels / density);

		return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
	}



	private synchronized void doSaveData() {
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		SharedPreferences.Editor edit = prefs.edit();
		mConfig.setWgConfig(wgConfigEditText.getText().toString());
		edit.apply();
	}

	public void ovpne(View v)
	{
		setCERTIFICATE();
	}

	public void v2r(View v)
	{
		setV2RAYCONFIG();
	}

	public void udpss(View v)
	{
		addudpDialog();
	}

	public void dnstt(View v)
	{
		addSlowDnsDialog();
	}



	public void doLayout() {
		setContentView(R.layout.activity_main_drawer);
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		final SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();
		prefsPrivate.edit();
		drawerNavigationView = (NavigationView) findViewById(R.id.shitstuff);
		View headerView = drawerNavigationView.getHeaderView(0);
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String format = String.format("v%s (%s)", packageInfo.versionName,
					Integer.valueOf(((PackageInfo) packageInfo).versionCode));
			TextView textView = (TextView) headerView.findViewById(R.id.app_vVer);
			this.mmk = textView;
			textView.setText(format);
			TextView powered = headerView.findViewById(R.id.tvPowered);
			if (!prefsPrivate.getString(Settings.ED_POWERED, "").isEmpty()) {
				String getI = prefsPrivate.getString(Settings.ED_POWERED, "");
				powered.setText(Html.fromHtml("2022┬й Powered by <font color=\"#00ff1b\">" + getI + "</font>"));
			}
		} catch (PackageManager.NameNotFoundException e) {
		}
		// Initialize the Mobile Ads SDK.
		MobileAds.initialize(this, new OnInitializationCompleteListener() {
			@Override
			public void onInitializationComplete(InitializationStatus initializationStatus) { }
		});
		BottomNavigationView navigationView = findViewById(R.id.navigation);
		PageAdapter pageAdapter = new PageAdapter(this);
		viewPager = findViewById(R.id.viewPager);
		viewPager.setAdapter(pageAdapter);
		viewPager.setOffscreenPageLimit(2);
		viewPager.addOnPageChangeListener(
				new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
						Menu menu = navigationView.getMenu();
						if (position == 0) {
							menu.findItem(R.id.homeMenu).setChecked(true);
							toolbar_main.setTitle(R.string.app_name);
						} else {
							menu.findItem(R.id.logMenu).setChecked(true);
							toolbar_main.setTitle("Logs");
						} if (position == 2) {
							menu.findItem(R.id.tools).setChecked(true);
							toolbar_main.setTitle("Tools");
						}
					}

					@Override
					public void onPageSelected(int position) {
						if (position == 0) {
							isHomeTab = true;
						} else if (position == 1) {

							isHomeTab = false;
						} else if (position == 2) {

							isHomeTab = false;
						}
					}

					@Override
					public void onPageScrollStateChanged(int state) {}
				});
		navigationView.setOnNavigationItemSelectedListener(
				new BottomNavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(@NonNull MenuItem item) {
						switch (item.getItemId()) {
							case R.id.homeMenu:
								viewPager.setCurrentItem(0);
								return true;
							case R.id.logMenu:
								viewPager.setCurrentItem(1);
								return true;
							case R.id.tools:
								viewPager.setCurrentItem(2);
								return true;
						}
						return false;
					}
				});
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		logsAdapter = new LogsAdapter(layoutManager, this);
		RecyclerView recyclerView = findViewById(R.id.recyclerLog);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(logsAdapter);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
				&& checkSelfPermission(NOTIFICATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] {NOTIFICATION_PERMISSION}, PERMISSION_REQUEST_CODE);
		}
		adContainerView = findViewById(R.id.ad_view_container);
		adView = new AdView(this);
		adContainerView.addView(adView);
		// Since we're loading the banner based on the adContainerView size, we need
		// to wait until this view is laid out before we can get the width.
		adContainerView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (!initialLayoutComplete) {
							initialLayoutComplete = true;
							loadBanner();
						}
					}
				});
		loadInterstitialAds();
		adsPopUp();

       /* mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdVieww = findViewById(R.id.adVieww);
        AdRequest adRequestt = new AdRequest.Builder().build();
        mAdVieww.loadAd(adRequestt);*/




		// test

		SharedPreferences.Editor edit = prefsPrivate.edit();
		prefsPrivate.edit();


		// === كود Psiphon الجديد ===
		psiphonmode = findViewById(R.id.psiphonmode);
		mPsiphonProtocolSpinner = findViewById(R.id.psiphonProtocolSpinner);
		mPsiphonRegionSpinner = findViewById(R.id.psiphonRegionSpinner);
		mPsiphonAuthEditText = findViewById(R.id.psiphonAuthEditText);
		mPsiphonServerEntryEditText = findViewById(R.id.psiphonServerEntryEditText);

// --- إعداد قائمة البروتوكولات ---
		final String[] protocolNames = {"Default", "OSSH", "HTTP", "CONJURE", "AMPHORAE"};
		final String[] protocolIDs = {"", "UNFRONTED-MEEK-OSSH", "FRONTED-MEEK-HTTP-OSSH", "CONJURE", "AMPHORAE"};
		ArrayAdapter<String> protocolAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, protocolNames);
		protocolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mPsiphonProtocolSpinner.setAdapter(protocolAdapter);
		mPsiphonProtocolSpinner.setSelection(sShared.getInt("psiphon_protocol_pos", 0));
		mPsiphonProtocolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
				sShared.edit().putString(Settings.PSIPHON_PROTOCOL_KEY, protocolIDs[pos]).apply();
				sShared.edit().putInt("psiphon_protocol_pos", pos).apply();
			}
			@Override public void onNothingSelected(AdapterView<?> p) {}
		});

// --- إعداد قائمة الدول ---
		final String[] regionCodes = {"", "AT", "BE", "BG", "BR", "CA", "CH", "CZ", "DE", "DK", "ES", "FI", "FR", "GB", "HU", "IE", "IN", "IT", "JP", "NL", "NO", "PL", "RO", "RS", "SE", "SG", "SK", "UA", "US"};
		final String[] regionNames = {"Best Performance", "Austria", "Belgium", "Bulgaria", "Brazil", "Canada", "Switzerland", "Czech Republic", "Germany", "Denmark", "Spain", "Finland", "France", "United Kingdom", "Hungary", "Ireland", "India", "Italy", "Japan", "Netherlands", "Norway", "Poland", "Romania", "Serbia", "Sweden", "Singapore", "Slovakia", "Ukraine", "United States"};
		ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, regionNames);
		regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mPsiphonRegionSpinner.setAdapter(regionAdapter);
		mPsiphonRegionSpinner.setSelection(sShared.getInt("psiphon_region_pos", 0));
		mPsiphonRegionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
				sShared.edit().putString(Settings.PSIPHON_REGION_KEY, regionCodes[pos]).apply();
				sShared.edit().putInt("psiphon_region_pos", pos).apply();
			}
			@Override public void onNothingSelected(AdapterView<?> p) {}
		});

// --- إعداد حقل Authorizations ---
		mPsiphonAuthEditText.setText(sShared.getString(Settings.PSIPHON_AUTHORIZATIONS_KEY, ""));
		mPsiphonAuthEditText.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
			@Override public void onTextChanged(CharSequence s, int st, int b, int c) {
				sShared.edit().putString(Settings.PSIPHON_AUTHORIZATIONS_KEY, s.toString()).apply();
			}
			@Override public void afterTextChanged(Editable s) {}
		});

// --- إعداد حقل Target Server Entry ---
		mPsiphonServerEntryEditText.setText(sShared.getString(Settings.PSIPHON_SERVER_ENTRY_KEY, ""));
		mPsiphonServerEntryEditText.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
			@Override public void onTextChanged(CharSequence s, int st, int b, int c) {
				sShared.edit().putString(Settings.PSIPHON_SERVER_ENTRY_KEY, s.toString()).apply();
			}
			@Override public void afterTextChanged(Editable s) {}
		});


		sshmode = (LinearLayout) findViewById(R.id.sshmode);
		ovpnmode = (LinearLayout) findViewById(R.id.ovpnmode);

		v2v = (CardView) findViewById(R.id.v2v);
		ovpn2 = (CardView) findViewById(R.id.ovpn2);
		udpsse = (CardView) findViewById(R.id.udpsse);
		slowdns = (CardView) findViewById(R.id.slowdns);

		ipssh = (TextInputLayout) findViewById(R.id.txipssh);
		ipvpn = (TextInputLayout) findViewById(R.id.txipvpn);
		wgmode = (CardView) findViewById(R.id.wgmode);
		wgConfigEditText = findViewById(R.id.wgConfigEditText);
		trojanmode = findViewById(R.id.trojanmode);
		// --- بدء برمجة واجهة Trojan ---
		trojanRemarksEditText = findViewById(R.id.trojanRemarksEditText);
		trojanAddressEditText = findViewById(R.id.trojanAddressEditText);
		trojanPortEditText = findViewById(R.id.trojanPortEditText);
		trojanPasswordEditText = findViewById(R.id.trojanPasswordEditText);
		trojanSniEditText = findViewById(R.id.trojanSniEditText);

// --- تحميل الإعدادات المحفوظة ---
		trojanRemarksEditText.setText(sShared.getString(Settings.TROJAN_REMARKS_KEY, ""));
		trojanAddressEditText.setText(sShared.getString(Settings.TROJAN_ADDRESS_KEY, ""));
		trojanPortEditText.setText(sShared.getString(Settings.TROJAN_PORT_KEY, "443"));
		trojanPasswordEditText.setText(sShared.getString(Settings.TROJAN_PASSWORD_KEY, ""));
		trojanSniEditText.setText(sShared.getString(Settings.TROJAN_SNI_KEY, ""));

// --- حفظ الإعدادات عند تغييرها (نستخدم الدالة المساعدة التي أنشأناها سابقا) ---
		addTextWatcher(trojanRemarksEditText, Settings.TROJAN_REMARKS_KEY);
		addTextWatcher(trojanAddressEditText, Settings.TROJAN_ADDRESS_KEY);
		addTextWatcher(trojanPortEditText, Settings.TROJAN_PORT_KEY);
		addTextWatcher(trojanPasswordEditText, Settings.TROJAN_PASSWORD_KEY);
		addTextWatcher(trojanSniEditText, Settings.TROJAN_SNI_KEY);
// --- انتهاء برمجة واجهة Trojan ---

		tunSpin = (Spinner) findViewById(R.id.serverSpinner);
		List<String> tunAdapter = new ArrayList<String>();
		tunAdapter.add("Custom SSH Client");
		tunAdapter.add("OpenVPN for Android");
		tunAdapter.add("DNSTT Via SSH");
		tunAdapter.add("UDP Hysteria");
		tunAdapter.add("V2Ray (json)");
		tunAdapter.add("WireGuard");
		tunAdapter.add("Psiphon Tunnel");
		tunAdapter.add("Trojan");



		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tunAdapter);
		tunSpin.setAdapter(dataAdapter);
		tunSpin.setSelection(prefsPrivate.getInt("SavePost", 0));
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tunSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
				prefsPrivate.edit().putInt("SavePost", p3).apply();
				//   SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();
				SharedPreferences.Editor edit = prefsPrivate.edit();
				int i = prefsPrivate.getInt(Settings.CB_TUNNELTYPE, 0);
				String[] split = edIn.getText().toString().split("[:@]");
				String[] split2 = prefsPrivate.getString(Settings.PROXY_IP_PORT, "").split("[:]");
				String string = prefsPrivate.getString(Settings.PROXY_IP_PORT, "");
				String string2 = prefsPrivate.getString(Settings.CUSTOM_SNI, "");
				String replace = prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "").replace("[host]", split[0]);
				int tunnelType;

				if (p3 == 0) { // SSH
					sshmode.setVisibility(View.VISIBLE);
					ipssh.setVisibility(View.VISIBLE);

					ovpnmode.setVisibility(View.GONE);
					ipvpn.setVisibility(View.GONE);
					v2v.setVisibility(View.GONE);
					ovpn2.setVisibility(View.GONE);
					slowdns.setVisibility(View.GONE);
					udpsse.setVisibility(View.GONE);
					wgmode.setVisibility(View.GONE);

					// هنا نحدد نوع النفق بناءً على القائمة المنسدلة الأخرى داخل وضع SSH
					tunnelType = Settings.bTUNNEL_TYPE_SSH_DIRECT; // أو أي قيمة افتراضية لوضع SSH
				} else if (p3 == 1) { // OpenVPN
					ovpnmode.setVisibility(View.VISIBLE);
					linearovpndetail.setVisibility(View.VISIBLE);
					ipvpn.setVisibility(View.VISIBLE);
					ovpn2.setVisibility(View.VISIBLE);

					sshmode.setVisibility(View.GONE);
					ipssh.setVisibility(View.GONE);
					v2v.setVisibility(View.GONE);
					slowdns.setVisibility(View.GONE);
					udpsse.setVisibility(View.GONE);
					wgmode.setVisibility(View.GONE);

					tunnelType = Settings.bTUNNEL_TYPE_OPENVPN; // القيمة الخاصة بـ OpenVPN
				} else if (p3 == 2) { // DNSTT
					slowdns.setVisibility(View.VISIBLE);
					ipssh.setVisibility(View.VISIBLE);

					sshmode.setVisibility(View.GONE);
					ovpnmode.setVisibility(View.GONE);
					ipvpn.setVisibility(View.GONE);
					v2v.setVisibility(View.GONE);
					ovpn2.setVisibility(View.GONE);
					udpsse.setVisibility(View.GONE);
					wgmode.setVisibility(View.GONE);

					tunnelType = Settings.bTUNNEL_TYPE_SLOWDNS;
				} else if (p3 == 3) { // UDP Hysteria
					udpsse.setVisibility(View.VISIBLE);
					ipssh.setVisibility(View.VISIBLE);

					sshmode.setVisibility(View.GONE);
					ovpnmode.setVisibility(View.GONE);
					ipvpn.setVisibility(View.GONE);
					v2v.setVisibility(View.GONE);
					ovpn2.setVisibility(View.GONE);
					slowdns.setVisibility(View.GONE);
					wgmode.setVisibility(View.GONE);

					tunnelType = Settings.bTUNNEL_TYPE_UDP;
				} else if (p3 == 4) { // V2Ray
					v2v.setVisibility(View.VISIBLE);

					sshmode.setVisibility(View.GONE);
					ovpnmode.setVisibility(View.GONE);
					ipssh.setVisibility(View.GONE);
					ipvpn.setVisibility(View.GONE);
					ovpn2.setVisibility(View.GONE);
					slowdns.setVisibility(View.GONE);
					udpsse.setVisibility(View.GONE);
					wgmode.setVisibility(View.GONE);

					tunnelType = Settings.bTUNNEL_TYPE_V2RAY;
				} else if (p3 == 5) { // WireGuard
					wgmode.setVisibility(View.VISIBLE);

					sshmode.setVisibility(View.GONE);
					ovpnmode.setVisibility(View.GONE);
					ipssh.setVisibility(View.GONE);
					ipvpn.setVisibility(View.GONE);
					v2v.setVisibility(View.GONE);
					ovpn2.setVisibility(View.GONE);
					slowdns.setVisibility(View.GONE);
					udpsse.setVisibility(View.GONE);

					tunnelType = Settings.bTUNNEL_TYPE_WIREGUARD;

				} else if (p3 == 6) { // Psiphon  <<<==== أضف هذه الكتلة
					psiphonmode.setVisibility(View.VISIBLE);

					sshmode.setVisibility(View.GONE);
					ovpnmode.setVisibility(View.GONE);
					ipssh.setVisibility(View.GONE);
					ipvpn.setVisibility(View.GONE);
					v2v.setVisibility(View.GONE);
					ovpn2.setVisibility(View.GONE);
					slowdns.setVisibility(View.GONE);
					udpsse.setVisibility(View.GONE);
					wgmode.setVisibility(View.GONE);

					tunnelType = Settings.bTUNNEL_TYPE_PSIPHON;
				} else if (p3 == 7) { // Trojan <<<==== أضف هذه الكتلة
					trojanmode.setVisibility(View.VISIBLE);

					sshmode.setVisibility(View.GONE);
					ovpnmode.setVisibility(View.GONE);
					ipssh.setVisibility(View.GONE);
					ipvpn.setVisibility(View.GONE);
					v2v.setVisibility(View.GONE);
					ovpn2.setVisibility(View.GONE);
					slowdns.setVisibility(View.GONE);
					udpsse.setVisibility(View.GONE);
					wgmode.setVisibility(View.GONE);

					tunnelType = Settings.bTUNNEL_TYPE_TROJAN;

			} else {
					// حالة افتراضية
					tunnelType = Settings.bTUNNEL_TYPE_SSH_DIRECT;
				}

				// نحفظ القيمة دائماً في نفس المكان الموثوق
				// السطر الصحيح والمعدل
				prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, tunnelType).commit();

				doSaveData();
				doUpdateLayout();
			}

			@Override
			public void onNothingSelected(AdapterView<?> p1) {
			}
		});


		//test


		mAdViewww = findViewById(R.id.adViewww);
		AdRequest adRequesttt = new AdRequest.Builder().build();
		mAdViewww.loadAd(adRequesttt);
		// mAdViewww.setAdUnitId("ca-app-pub-3940256099942544/6300978111");



		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
		this.toolbar_main = toolbar;
		setSupportActionBar(toolbar);
		this.drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, this.drawerLayout,
				R.string.nav_open, R.string.nav_close);
		this.actionBarDrawerToggle = actionBarDrawerToggle;
		this.drawerLayout.addDrawerListener(actionBarDrawerToggle);
		this.actionBarDrawerToggle.syncState();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		fabbb();
		allAboutConf();
		linearLayout = (LinearLayout) findViewById(R.id.statusConn);
		linearovpndetail = (LinearLayout) findViewById(R.id.ovpndetail);
		logTv = (TextView) findViewById(R.id.logTv);

		edOvpnUserPass = findViewById(R.id.edInputVpn);
		edOvpnUserPass.setText(prefsPrivate.getString(Settings.USUARIO_OVPN, ""));
		edOvpnUserPass.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				prefsPrivate.edit()
						.putString(Settings.USUARIO_OVPN, MainActivity.this.edOvpnUserPass.getText().toString()).apply();
			}

			@Override
			public void afterTextChanged(Editable editable) {
				prefsPrivate.edit()
						.putString(Settings.USUARIO_OVPN, MainActivity.this.edOvpnUserPass.getText().toString()).apply();
			}
		});

		socketIp = (MaterialTextView) findViewById(R.id.socketIp);
		socketnet = (MaterialTextView) findViewById(R.id.socketnet);
		card_tools1 = (MaterialCardView)findViewById(R.id.cardtools1);
		card_tools2 = (MaterialCardView)findViewById(R.id.cardtools2);
		card_tools3 = (MaterialCardView)findViewById(R.id.cardtools3);
		card_tools4 = (MaterialCardView)findViewById(R.id.cardtools4);
		card_tools5 = (MaterialCardView)findViewById(R.id.cardtools5);
		card_tools6 = (MaterialCardView)findViewById(R.id.cardtools6);
		card_tools7 = (MaterialCardView)findViewById(R.id.cardtools7);


		card_tools1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View p1) {
				Intent MainActivityWifi = new Intent(MainActivity.this, MainActivityWifi.class);
				startActivity(MainActivityWifi);
			}
		});


		card_tools2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View p1) {
				Intent HostChecker = new Intent(MainActivity.this, HostChecker.class);
				startActivity(HostChecker);
			}
		});


		card_tools4.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View p1) {
				Intent ping = new Intent(MainActivity.this, ping.class);
				startActivity(ping);
			}
		});


		card_tools5.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View p1) {
				Intent scanports = new Intent(MainActivity.this, scanports.class);
				startActivity(scanports);
			}
		});

		card_tools3.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View p1) {
				Intent IpHunter = new Intent(MainActivity.this, IpHunter.class);
				startActivity(IpHunter);
			}
		});

		card_tools6.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View p1) {
				Intent MyIP_Address = new Intent(MainActivity.this, MyIP_Address.class);
				startActivity(MyIP_Address);
			}
		});

		card_tools7.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View p1) {
				Intent activity_subdomainfinder = new Intent(MainActivity.this, subfinder.class);
				startActivity(activity_subdomainfinder);
			}
		});
		isUsePayloadOvpn = findViewById(R.id.isUsePayloadOvpn);
		isUsePayloadOvpn.setChecked(sShared.getBoolean(Settings.CB_PAYLOLOADOVPN, false));

		isSSLOvpn = findViewById(R.id.isSSLOvpn);
		isSSLOvpn.setChecked(sShared.getBoolean(Settings.CB_SNIOVPN, false));

		cbPayload = findViewById(R.id.isUsePayload);
		cbPayload.setChecked(sShared.getBoolean(Settings.CB_PAYLOLOAD, false));

		cbSni = findViewById(R.id.isSSL);
		cbSni.setChecked(prefsPrivate.getBoolean(Settings.CB_SNI, false));



		starterButton = findViewById(R.id.btnStart);
		starterButton.setOnClickListener(this);

		int i = prefsPrivate.getInt(Settings.LOG_LEVE, 1);
		linearLogLevel = (LinearLayout) findViewById(R.id.logLevelLL);
		seekBar = (SeekBar) findViewById(R.id.LogLevelSlider);
		seekBar.setMax(3);
		seekBar.setProgress(i - 1);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar2, int i2, boolean z) {
				int i3 = i2 + 1;
				MainActivity.this.mLogAdapter.setLogLevel(i3);
				prefsPrivate.edit().putInt(Settings.LOG_LEVE, i3).apply();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar2) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar2) {
			}
		});




		edIn = findViewById(R.id.edInput);
		edIn.setText(prefsPrivate.getString("SavedSerperString", ""));

		edIn.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
				prefsPrivate.edit().putString("SavedSerperString", MainActivity.this.edIn.getText().toString()).apply();
			}

			@Override
			public void afterTextChanged(Editable editable) {
				prefsPrivate.edit().putString("SavedSerperString", MainActivity.this.edIn.getText().toString()).apply();
			}
		});
		this.coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);


		cbDns = findViewById(R.id.isDnsEnable);
		cbDns.setChecked(defaultSharedPreferences.getBoolean(Settings.DNSFORWARD_KEY, false));

		isUDPGW = findViewById(R.id.isUDPGW);
		isUDPGW.setChecked(defaultSharedPreferences.getBoolean(Settings.UDPFORWARD_KEY, false));

		cbWakelock = findViewById(R.id.isWakeLock);
		cbWakelock.setChecked(defaultSharedPreferences.getBoolean(Settings.WAKELOCK_KEY, false));

		cbAutoRp = findViewById(R.id.isAutoReplace);
		cbAutoRp.setChecked(prefsPrivate.getBoolean(Settings.AUTO_REPLACE, false));

		cbDetaillog = findViewById(R.id.isDetailLog);
		cbDetaillog.setChecked(prefsPrivate.getBoolean(Settings.DETAI_LOG, false));

		cbStatuslog = findViewById(R.id.isShowStats);
		cbStatuslog.setChecked(prefsPrivate.getBoolean(Settings.SHOW_STATS, false));

		isUsePayloadOvpn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
				if (z) {

					if (prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "").isEmpty()) {
						MainActivity.this.addPayloadDialog();
					}
					prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOADOVPN, true).apply();
					return;
				}
				prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOADOVPN, false).apply();
			}
		});
		isSSLOvpn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
				if (z) {

					if (prefsPrivate.getString(Settings.CUSTOM_SNI, "").isEmpty()) {
						MainActivity.this.addSniDialog();
					}
					prefsPrivate.edit().putBoolean(Settings.CB_SNIOVPN, true).apply();
					return;
				}
				prefsPrivate.edit().putBoolean(Settings.CB_SNIOVPN, false).apply();
			}
		});


		cbPayload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
				if (z) {

					if (prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "").isEmpty()) {
						MainActivity.this.addPayloadDialog();
					}
					prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOAD, true).apply();
					return;
				}
				prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOAD, false).apply();
			}
		});
		cbSni.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
				if (z) {

					if (prefsPrivate.getString(Settings.CUSTOM_SNI, "").isEmpty()) {
						MainActivity.this.addSniDialog();
					}
					prefsPrivate.edit().putBoolean(Settings.CB_SNI, true).apply();
					return;
				}
				prefsPrivate.edit().putBoolean(Settings.CB_SNI, false).apply();
			}
		});
		cbDns.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
				if (z) {
					defaultSharedPreferences.edit().putBoolean(Settings.DNSFORWARD_KEY, true).apply();
				} else {
					defaultSharedPreferences.edit().putBoolean(Settings.DNSFORWARD_KEY, false).apply();
				}
			}
		});

		isUDPGW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
				if (z) {
					defaultSharedPreferences.edit().putBoolean(Settings.UDPFORWARD_KEY, true).apply();
				} else {
					defaultSharedPreferences.edit().putBoolean(Settings.UDPFORWARD_KEY, false).apply();
				}
			}
		});

		cbWakelock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
				if (z) {
					defaultSharedPreferences.edit().putBoolean(Settings.WAKELOCK_KEY, true).apply();
				} else {
					defaultSharedPreferences.edit().putBoolean(Settings.WAKELOCK_KEY, false).apply();
				}
			}
		});
		cbAutoRp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
				if (z) {
					prefsPrivate.edit().putBoolean(Settings.AUTO_REPLACE, true).apply();
				} else {
					prefsPrivate.edit().putBoolean(Settings.AUTO_REPLACE, false).apply();
				}
			}
		});
		cbDetaillog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
				if (z) {
					if (prefsPrivate.getInt(Settings.SSH_HARED, 0) == 1) {
						MainActivity.this.linearLogLevel.setVisibility(View.VISIBLE);
					}
					prefsPrivate.edit().putBoolean(Settings.DETAI_LOG, true).apply();
					return;
				}
				if (prefsPrivate.getInt(Settings.SSH_HARED, 0) == 1) {
					MainActivity.this.linearLogLevel.setVisibility(View.GONE);
				}
				prefsPrivate.edit().putBoolean(Settings.DETAI_LOG, false).apply();
			}
		});
		cbStatuslog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
				if (z) {
					if (prefsPrivate.getInt(Settings.SSH_HARED, 0) == 1) {
						MainActivity.this.linearLayout.setVisibility(View.VISIBLE);
					}
					prefsPrivate.edit().putBoolean(Settings.SHOW_STATS, true).apply();
					return;
				}
				if (prefsPrivate.getInt(Settings.SSH_HARED, 0) == 1) {
					MainActivity.this.linearLayout.setVisibility(View.GONE);
				}
				prefsPrivate.edit().putBoolean(Settings.SHOW_STATS, false).apply();
			}
		});
		if (sShared.getInt(Settings.SSH_HARED, 0) == 0) {
			if (prefsPrivate.getBoolean(Settings.CP_SSH, false)) {
				this.ipssh.setVisibility(View.VISIBLE);
			} else {
				this.ipssh.setVisibility(View.GONE);
			}

			this.ipvpn.setVisibility(View.VISIBLE);
			if (this.cbDetaillog.isChecked()) {
				this.linearLogLevel.setVisibility(View.VISIBLE);
			}
			if (this.cbStatuslog.isChecked()) {
				this.linearLayout.setVisibility(View.VISIBLE);
				return;
			}
			return;
		}
		if (prefsPrivate.getBoolean(Settings.CP_SSH, false)) {
			this.ipvpn.setVisibility(View.VISIBLE);
		} else {
			this.ipvpn.setVisibility(View.VISIBLE);
		}


		this.ipssh.setVisibility(View.VISIBLE);
		if (this.cbDetaillog.isChecked()) {
			this.linearLogLevel.setVisibility(View.GONE);
		}
		if (this.cbStatuslog.isChecked()) {
			this.linearLayout.setVisibility(View.GONE);
		}
	}

	private void showExportConfigDialog() {
		if (sShared.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			Kuda();
			return;
		}
		ExportConfig export = new ExportConfig(this);
		export.setGenerateListener("Save", new ExportConfig.OnGenerateListener() {
			@Override
			public void onGenerate(String v) {
				createFile(sShared.getString(Settings.NAME_CONFIG, "") + ".sip");
			}
		});
		export.show();
	}

	private void addTextWatcher(EditText editText, final String key) {
		editText.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				sShared.edit().putString(key, s.toString()).apply();
			}
			@Override public void afterTextChanged(Editable s) {}
		});
	}
	private boolean startFilePicker() {

		if (!MainActivity.isLocked) {
			MainActivity.this.showToast("Vpn is Running");
			return false;
		}

		Intent i = Utils.getFilePickerIntent(this, Utils.FileType.OVPN_CONFIG);
		if (i != null) {
			startActivityForResult(i, 677);
			return true;
		} else
			return false;
	}

	private boolean filePickerConfig() {

		if (!MainActivity.isLocked) {
			MainActivity.this.showToast("Vpn is Running");
			return false;
		}

		Intent i = Utils.getFilePickerIntent(this, Utils.FileType.OVPN_CONFIG);
		if (i != null) {
			startActivityForResult(i, 666);
			return true;
		} else
			return false;
	}

	public void createFile(String name) {
		Intent intent = new Intent("android.intent.action.CREATE_DOCUMENT");
		intent.addCategory("android.intent.category.OPENABLE");
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_TITLE, name);
		startActivityForResult(intent, 678);
	}

	public void doUpdateLayout() {
		wgConfigEditText.setText(mConfig.getWgConfig());
		final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		final SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();

		if (sShared.getInt(Settings.SSH_HARED, 0) == 0) {
			setStarterButton(this.starterButton, this);
		}
		int tunnelType = prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);
		switch (tunnelType) {

			case Settings.bTUNNEL_TYPE_SSH:
				prefsPrivate.edit().putInt(Settings.SSH_HARED, 0).apply();
				prefsPrivate.edit().putInt(Settings.OVPN_HARED, 0).apply();
				tunSpin.setSelection(0);
				break;


			case Settings.bTUNNEL_TYPE_SLOWDNS:
				tunSpin.setSelection(2);
				break;

			case Settings.bTUNNEL_TYPE_UDP:
				tunSpin.setSelection(3);
				break;

			case Settings.bTUNNEL_TYPE_V2RAY:
				tunSpin.setSelection(4);
				break;
		}
	}


	public void startOrStopTunnel(Context context) {
		// إذا كان هناك اتصال يعمل، قم بإيقافه
		if (SkStatus.isTunnelActive()) {
			TunnelManagerHelper.stopSocksHttp(context);
			return;
		}

		// إذا لم يكن هناك اتصال، قم بطلب إذن تشغيل VPN من الأندرويد
		Intent intent = VpnService.prepare(context);

		if (intent != null) {
			// إذا كان النظام يحتاج لإظهار نافذة طلب الإذن للمستخدم
			VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
					de.blinkt.openvpn.core.ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
			try {
				// استخدم الكود 32002 الموجود لديك
				startActivityForResult(intent, START_VPN_PROFILE);
			} catch (Exception e) {
				VpnStatus.logError("VPN permission activity not found: " + e.getMessage());
			}
		} else {
			// إذا كان الإذن ممنوحاً بالفعل، ابدأ الاتصال مباشرة
			onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
		}
	}


	/*public void startOrStopTunnel(Activity activity) {
		if (SkStatus.isTunnelActive()) {
			TunnelManagerHelper.stopSocksHttp(activity);
			return;
		}
		HmmClose();
		launchVPN();
	}*/

	public void cbOnclick(View view) {
		SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();
		if (this.cbPayload.isChecked() && this.cbSni.isChecked()) {
			prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOAD, true).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_SNI, true).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_DNSTT, false).apply();
			prefsPrivate.edit().putInt(Settings.CB_TUNNELTYPE, 3).apply();
		} else if (this.cbPayload.isChecked() && !this.cbSni.isChecked()) {
			prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOAD, true).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_SNI, false).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_DNSTT, false).apply();
			prefsPrivate.edit().putInt(Settings.CB_TUNNELTYPE, 1).apply();
		} else if (this.cbSni.isChecked() && !cbPayload.isChecked()) {
			prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOAD, false).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_SNI, true).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_DNSTT, false).apply();
			prefsPrivate.edit().putInt(Settings.CB_TUNNELTYPE, 2).apply();
		} else if (!cbSni.isChecked() && !cbPayload.isChecked()) {
			prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOAD, false).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_SNI, false).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_DNSTT, false).apply();
			prefsPrivate.edit().putInt(Settings.CB_TUNNELTYPE, 1).apply();
		}
	}

	public void setupData() {
		try {
			SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();
			SharedPreferences.Editor edit = prefsPrivate.edit();
			int i = prefsPrivate.getInt(Settings.CB_TUNNELTYPE, 0);
			String[] split = this.edIn.getText().toString().split("[:@]");
			String[] split2 = prefsPrivate.getString(Settings.PROXY_IP_PORT, "").split("[:]");
			String string = prefsPrivate.getString(Settings.PROXY_IP_PORT, "");
			String string2 = prefsPrivate.getString(Settings.CUSTOM_SNI, "");
			String replace = prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "").replace("[host]", split[0]);

			if (i == 1) {
				if (cbPayload.isChecked() && !prefsPrivate.getString(Settings.PROXY_IP_PORT, "").isEmpty()
						&& !prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "").isEmpty()) {
					edit.putString(Settings.SERVIDOR_KEY, split[0]);
					edit.putString(Settings.SERVIDOR_PORTA_KEY, split[1]);
					edit.putString(Settings.USUARIO_KEY, split[2]);
					edit.putString(Settings.SENHA_KEY, split[3]);
					prefsPrivate.edit().putString(Settings.PROXY_IP_KEY, split2[0]).apply();
					prefsPrivate.edit().putString(Settings.PROXY_PORTA_KEY, split2[1]).apply();
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
					prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, replace).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY).apply();
				} else if (cbPayload.isChecked() && !prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "").isEmpty()) {
					edit.putString(Settings.SERVIDOR_KEY, split[0]);
					edit.putString(Settings.SERVIDOR_PORTA_KEY, split[1]);
					edit.putString(Settings.USUARIO_KEY, split[2]);
					edit.putString(Settings.SENHA_KEY, split[3]);
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
					prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, replace).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT).apply();
				} else if (cbPayload.isChecked() && !prefsPrivate.getString(Settings.PROXY_IP_PORT, "").isEmpty()) {
					prefsPrivate.edit().putString(Settings.PROXY_IP_KEY, split2[0]).apply();
					prefsPrivate.edit().putString(Settings.PROXY_PORTA_KEY, split2[1]).apply();
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY).apply();
				} else {
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT).apply();
					edit.putString(Settings.SERVIDOR_KEY, split[0]);
					edit.putString(Settings.SERVIDOR_PORTA_KEY, split[1]);
					edit.putString(Settings.USUARIO_KEY, split[2]);
					edit.putString(Settings.SENHA_KEY, split[3]);
				}
				prefsPrivate.edit().putString("SavedSerperString", this.edIn.getText().toString()).apply();
			} else if (i == 2) {
				prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true).apply();
				prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_SSL).apply();
				String string3 = prefsPrivate.getString(Settings.CUSTOM_SNI, "");
				edit.putString(Settings.USUARIO_KEY, split[2]);
				edit.putString(Settings.SENHA_KEY, split[3]);
				edit.putString(Settings.SERVIDOR_KEY, split[0]);
				edit.putString(Settings.SERVIDOR_PORTA_KEY, split[1]);
				edit.putString(Settings.CUSTOM_SNI, string3);
				prefsPrivate.edit().putString("SavedSerperString", this.edIn.getText().toString()).apply();
			} else if (i == 3) {
				if (!string.isEmpty()) {
					showToast("ssl + payload + proxy");
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSL_RP).apply();
					prefsPrivate.edit().putString(Settings.PROXY_IP_KEY, split2[0]).apply();
					prefsPrivate.edit().putString(Settings.PROXY_PORTA_KEY, split2[1]).apply();
					prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "")).apply();
				} else {
					showToast("ssl + payload");
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_PAY_SSL).apply();
					prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, replace).apply();
				}
				edit.putString(Settings.SERVIDOR_KEY, string2);
				edit.putString(Settings.SERVIDOR_PORTA_KEY, split[1]);
				edit.putString(Settings.USUARIO_KEY, split[2]);
				edit.putString(Settings.SENHA_KEY, split[3]);
				edit.putString(Settings.CUSTOM_SNI, string2);

				prefsPrivate.edit().putString("SavedSerperString", this.edIn.getText().toString()).apply();
			}
			edit.apply();
		} catch (Exception e) {
		}
	}

	public void cbOnclickOVPN(View view) {
		SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();
		if (this.isUsePayloadOvpn.isChecked() && this.isSSLOvpn.isChecked()) {
			prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOADOVPN, true).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_SNIOVPN, true).apply();
			prefsPrivate.edit().putInt(Settings.CB_TUNNELTYPE, 3).apply();

		} else if (this.isUsePayloadOvpn.isChecked() && !this.isSSLOvpn.isChecked()) {
			prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOADOVPN, true).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_SNIOVPN, false).apply();
			prefsPrivate.edit().putInt(Settings.CB_TUNNELTYPE, 1).apply();

		} else if (this.isSSLOvpn.isChecked() && !isUsePayloadOvpn.isChecked()) {
			prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOADOVPN, false).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_SNIOVPN, true).apply();
			prefsPrivate.edit().putInt(Settings.CB_TUNNELTYPE, 2).apply();

		} else if (!isSSLOvpn.isChecked() && !isUsePayloadOvpn.isChecked()) {
			prefsPrivate.edit().putBoolean(Settings.CB_PAYLOLOADOVPN, false).apply();
			prefsPrivate.edit().putBoolean(Settings.CB_SNIOVPN, false).apply();
			prefsPrivate.edit().putInt(Settings.CB_TUNNELTYPE, 1).apply();
		}
	}

	public void setupDatOVPN() {
		try {
			SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();
			SharedPreferences.Editor edit = prefsPrivate.edit();
			int ovpn = prefsPrivate.getInt(Settings.CB_TUNNELTYPE, 0);
			String[] split = this.edIn.getText().toString().split("[:@]");
			String[] split2 = prefsPrivate.getString(Settings.PROXY_IP_PORT, "").split("[:]");
			String string = prefsPrivate.getString(Settings.PROXY_IP_PORT, "");
			String string2 = prefsPrivate.getString(Settings.CUSTOM_SNI, "");
			String replace = prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "").replace("[host]", split[0]);

			if (ovpn == 1) {
				if (cbPayload.isChecked() && !prefsPrivate.getString(Settings.PROXY_IP_PORT, "").isEmpty()
						&& !prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "").isEmpty()) {
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
					prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, replace).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_OVPN_PROXY).apply();

				} else if (cbPayload.isChecked() && !prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "").isEmpty()) {
					edit.putString(Settings.SERVIDOR_KEY, split[0]);
					edit.putString(Settings.SERVIDOR_PORTA_KEY, split[1]);
					edit.putString(Settings.USUARIO_KEY, split[2]);
					edit.putString(Settings.SENHA_KEY, split[3]);
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
					prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, replace).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT).apply();

				} else if (cbPayload.isChecked() && !prefsPrivate.getString(Settings.PROXY_IP_PORT, "").isEmpty()) {
					prefsPrivate.edit().putString(Settings.PROXY_IP_KEY, split2[0]).apply();
					prefsPrivate.edit().putString(Settings.PROXY_PORTA_KEY, split2[1]).apply();
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_OVPN_PROXY).apply();

				} else {
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT).apply();
					edit.putString(Settings.SERVIDOR_KEY, split[0]);
					edit.putString(Settings.SERVIDOR_PORTA_KEY, split[1]);
					edit.putString(Settings.USUARIO_KEY, split[2]);
					edit.putString(Settings.SENHA_KEY, split[3]);
				}

			} else if (ovpn == 2) {
				prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true).apply();
				prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_OVPN_SSL).apply();
				String string3 = prefsPrivate.getString(Settings.CUSTOM_SNI, "");
				edit.putString(Settings.CUSTOM_SNI, string3);

			} else if (ovpn == 3) {
				if (!string.isEmpty()) {
					showToast("ssl + payload + proxy");
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSL_RP).apply();
					prefsPrivate.edit().putString(Settings.PROXY_IP_KEY, split2[0]).apply();
					prefsPrivate.edit().putString(Settings.PROXY_PORTA_KEY, split2[1]).apply();
					prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, "")).apply();
				} else {
					showToast("ssl + payload");
					prefsPrivate.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
					prefsPrivate.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_PAY_SSL).apply();
					prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, replace).apply();
				}

				edit.putString(Settings.USUARIO_KEY, split[2]);
				edit.putString(Settings.SENHA_KEY, split[3]);
				edit.putString(Settings.CUSTOM_SNI, string2);
			}
			edit.apply();
		} catch (Exception e) {
		}
	}

	@Override
	public void onClick(View view) {
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();
		switch (view.getId()) {




			case R.id.btnStart:
				// التأكد من صلاحية الإعدادات
				if (ImportConfig.isValidadeExpirou(sShared.getLong(Settings.CONFIG_VALIDADE_KEY, 0))) {
					ResetDefault.resetToBasic();
					showPd(MainActivity.this);
					return;
				}

				// الآن نستدعي دالة البدء مباشرة بدون أي إعداد مسبق هنا
				startOrStopTunnel(this);

				return;
		}
			/*if (sShared.getInt(Settings.SSH_HARED, 0) == 0) {
					setupData();
					setupDatOVPN();
					startOrStopTunnel(this);
					return;
				}
				if (edOvpnUserPass.getText().toString().isEmpty()) {
					showToast("Username & pass empty");
					return;
				}
				try {
					prepareVpn();
					return;
				} catch (Exception e) {
					return;
				}
			default:
				return;
		}*/
	}


	public void showLogWindow() {
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("com.fdx.injector:openLogs"));
	}

	@SuppressLint("WrongConstant")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();
		if (requestCode == START_VPN_PROFILE) {
			if (resultCode == Activity.RESULT_OK) {
				// المستخدم منح الإذن، ابدأ الاتصال الآن عبر الموزع
				startVpn();
			} else {
				// المستخدم رفض الإذن
				VpnStatus.updateStateString("USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled,
						de.blinkt.openvpn.core.ConnectionStatus.LEVEL_NOTCONNECTED);
				Toast.makeText(this, "Permission to use VPN was denied.", Toast.LENGTH_SHORT).show();
			}
			return; // مهم جداً أن نخرج من هنا
		}
		if (requestCode == 32002) {
			if (resultCode == Activity.RESULT_OK) {
				SecurePreferences prefsPrivate2 = this.mConfig.getPrefsPrivate();
				if (!TunnelUtils.isNetworkOnline(this)) {
					SkStatus.updateStateString("USER_VPN_PASSWORD_CANCELLED", "",
							R.string.state_user_vpn_password_cancelled, ConnectionStatus.LEVEL_NOTCONNECTED);
					Toast.makeText(this, (int) R.string.error_internet_off, 0).show();
				} else if (prefsPrivate2.getInt(Settings.TUNNELTYPE_KEY, 1) == 2
						&& (this.mConfig.getPrivString(Settings.PROXY_IP_KEY).isEmpty()
						|| this.mConfig.getPrivString(Settings.PROXY_PORTA_KEY).isEmpty())) {
					SkStatus.updateStateString("USER_VPN_PASSWORD_CANCELLED", "",
							R.string.state_user_vpn_password_cancelled, ConnectionStatus.LEVEL_NOTCONNECTED);
					Toast.makeText(this, (int) R.string.error_proxy_invalid, 0).show();
				} else if (!prefsPrivate2.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true)
						&& this.mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY).isEmpty()) {
					SkStatus.updateStateString("USER_VPN_PASSWORD_CANCELLED", "",
							R.string.state_user_vpn_password_cancelled, ConnectionStatus.LEVEL_NOTCONNECTED);
					Toast.makeText(this, (int) R.string.error_empty_payload, 0).show();
				} else if (prefsPrivate.getBoolean("SavedSetup", false)
						&& this.mConfig.getPrivString(Settings.CUSTOM_SNI).isEmpty()) {
					SkStatus.updateStateString("USER_VPN_PASSWORD_CANCELLED", "",
							R.string.state_user_vpn_password_cancelled, ConnectionStatus.LEVEL_NOTCONNECTED);
					Toast.makeText(this, "SNI cannot be empty!", 0).show();
				} else if (this.mConfig.getPrivString(Settings.SERVIDOR_KEY).isEmpty()
						|| this.mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY).isEmpty()) {
					SkStatus.updateStateString("USER_VPN_PASSWORD_CANCELLED", "",
							R.string.state_user_vpn_password_cancelled, ConnectionStatus.LEVEL_NOTCONNECTED);
					Toast.makeText(this, (int) R.string.error_empty_settings, Toast.LENGTH_SHORT).show();
				} else {
					if (!this.mhideLog) {
						showLogWindow();
					}
					TunnelManagerHelper.startSocksHttp(this);
				}
			} else if (resultCode == 0) {
				SkStatus.updateStateString("USER_VPN_PERMISSION_CANCELLED", "",
						R.string.state_user_vpn_permission_cancelled, ConnectionStatus.LEVEL_NOTCONNECTED);
				if (Build.VERSION.SDK_INT >= 24) {
					VpnStatus.logError("error on Nougat");
				}
			}
		}
		if (requestCode == 677 && resultCode == Activity.RESULT_OK) {
			try {
				try {
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(getContentResolver().openInputStream(data.getData())));
					String str = "";
					while (true) {
						String readLine = bufferedReader.readLine();
						if (readLine == null) {
							break;
						}
						str = str + readLine + "\n";
					}
					bufferedReader.readLine();
					try {
						ConfigParser configParser = new ConfigParser();
						configParser.parseConfig(new StringReader(str));
						VpnProfile convertProfile = configParser.convertProfile();
						if (convertProfile.mUsername.toString().isEmpty()
								|| convertProfile.mPassword.toString().isEmpty()) {
							this.edOvpnUserPass.setText(convertProfile.mUsername + "" + convertProfile.mPassword);
							//	sShared.edit().remove(Settings.USUARIO_OVPN).apply();
							sShared.edit().putString(Settings.OVPN_CONFIG, str).apply();
						} else {
							this.edOvpnUserPass.setText(convertProfile.mUsername + ":" + convertProfile.mPassword);
							sShared.edit().putString(Settings.USUARIO_OVPN, this.edOvpnUserPass.getText().toString())
									.apply();
							sShared.edit().putString(Settings.OVPN_CONFIG, str).apply();
						}
					} catch (Exception e) {
						Log.e("Parser", "" + e);
					}
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			} catch (Exception e3) {
				e3.printStackTrace();
			}
		}
		if (requestCode == 678 && resultCode == Activity.RESULT_OK) {
			if (data != null && data.getData() != null) {
				writeInFile(data.getData(), sShared.getString(Settings.S_SAVE, ""));
			}
		}
		if (requestCode == 666 && resultCode == Activity.RESULT_OK) {
			try {
				if (!ImportConfig.convertInputAndSave(getContentResolver().openInputStream(data.getData()),
						MainActivity.this)) {
					ResetDefault.resetToBasic();
					throw new IOException("Couldn't save configuration");
				}
				if (sShared.getBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false)) {
					addPwDialog();
				} else {
					long mValidade = sShared.getLong(Settings.CONFIG_VALIDADE_KEY, 0);
					if (mValidade > 0) {
						VpnStatus.logWarning(R.string.log_settings_valid,
								DateFormat.getDateFormat(MainActivity.this).format(mValidade));
					}
					showPd(MainActivity.this);
					showToast("Success Import Config");
				}
			} catch (IOException e) {
				ResetDefault.resetToBasic();
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void writeInFile(@NonNull Uri uri, @NonNull String text) {
		OutputStream outputStream;
		try {
			outputStream = getContentResolver().openOutputStream(uri);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
			bw.write(text);
			bw.flush();
			bw.close();
			showToast("Save successfully");
			sShared.edit().remove(Settings.S_SAVE).apply();
		} catch (IOException e) {
			e.printStackTrace();
			showToast("" + e.getMessage());
		}

	}

	private void launchVPN() {
		Intent prepare = VpnService.prepare(this);
		if (this.mConfig.getAutoClearLog()) {
			SkStatus.clearLog();
		}
		if (prepare != null) {
			SkStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
					ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
			try {
				startActivityForResult(prepare, START_VPN_PROFILE);
			} catch (ActivityNotFoundException e) {
				VpnStatus.logError((int) R.string.no_vpn_support_image);
			}
		} else {
			onActivityResult(START_VPN_PROFILE, -1, null);
		}
	}

	public void setStarterButton(Button button, Activity activity) {
		String lastState = SkStatus.getLastState();
		boolean isTunnelActive = SkStatus.isTunnelActive();
		if (button != null) {
			boolean equals = SkStatus.SSH_INICIANDO.equals(lastState);
			int i = R.string.disconnect;
			if (equals) {
				disableEnable(false);
				//	button.setEnabled(false);
			} else if (SkStatus.SSH_PARANDO.equals(lastState)) {
				i = R.string.state_stopping;
				//	button.setEnabled(false);
			} else {
				if (!isTunnelActive) {
					i = R.string.connect;
				}
				button.setEnabled(true);
			}
			button.setText(i);
		}
	}

	@Override
	public void updateState(String str, String str2, int i, final ConnectionStatus connectionStatus, Intent intent) {
		if (sShared.getInt(Settings.SSH_HARED, 0) == 0) {
			this.mHandler.post(new Runnable() {
				@Override
				public void run() {
					MainActivity.this.doUpdateLayout();
					setupData();
					setupDatOVPN();
					if (SkStatus.isTunnelActive()) {
						if (connectionStatus.equals(ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED)) {
							starterButton.setText("Connecting");
							disableEnable(false);
						}
						if (connectionStatus.equals(ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET)) {
							starterButton.setText("Connecting");
							disableEnable(false);
						}
					}
					if (connectionStatus.equals(ConnectionStatus.LEVEL_CONNECTED)) {
						disableEnable(false);
					} else {
						disableEnable(true);
					}
				}
			});
		}
	}

	public void showPayloadGenerator() {
		if (sShared.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			Kuda();
			return;
		}
		final SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();
		PayloadGenerator payloadGenerator = new PayloadGenerator(this);
		payloadGenerator.setGenerateListener("Save", new PayloadGenerator.OnGenerateListener() {
			@Override
			public void onGenerate(String str) {
				prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, str).apply();
				prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, str).apply();
			}
		});
		payloadGenerator.show();
	}

	public void addPwDialog() {
		View inflate = LayoutInflater.from(this).inflate(R.layout.socket_p, (ViewGroup) null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(inflate);
		Button button = (Button) inflate.findViewById(R.id.btnSavePs);

		final EditText editText = (EditText) inflate.findViewById(R.id.psInput);
		builder.setCancelable(false);

		final AlertDialog create = builder.create();
		create.show();
		create.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 1.0d), -2);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!editText.getText().toString().isEmpty()) {
					if (editText.getText().toString().equals(sShared.getString(Settings.I_PASSWORD, ""))) {
						long mValidade = sShared.getLong(Settings.CONFIG_VALIDADE_KEY, 0);
						if (mValidade > 0) {
							VpnStatus.logWarning(R.string.log_settings_valid,
									DateFormat.getDateFormat(MainActivity.this).format(mValidade));
						}
						showToast("Success Import Config");
						showPd(MainActivity.this);
					} else {
						ResetDefault.resetToBasic();
						doLayout();
						showToast("Wrong Password Config");
					}
				} else {
					ResetDefault.resetToBasic();
					doLayout();
					sShared.edit().remove(Settings.CONFIG_INPUT_PASSWORD_KEY).apply();
					showToast("Password Empty!");
				}
				create.dismiss();
			}
		});
		((Button) inflate.findViewById(R.id.btnCancelPs)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				create.dismiss();
				ResetDefault.resetToBasic();
				doLayout();
				showToast("Cancel Import Config");
			}
		});
	}

	public void addPayloadDialog() {
		final SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();

		if (prefsPrivate.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)
				&& prefsPrivate.getBoolean(Settings.CP_PAYLOAD, false)) {
			Kuda();
			return;
		}

		View inflate = LayoutInflater.from(this).inflate(R.layout.pref_payload, (ViewGroup) null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(inflate);
		Button button = (Button) inflate.findViewById(R.id.btnCancelPayload);
		Button button2 = (Button) inflate.findViewById(R.id.btnSavePayload);
		button2.setEnabled(isLocked);
		final EditText editText = (EditText) inflate.findViewById(R.id.cusPayload);
		editText.setText(prefsPrivate.getString(Settings.CUSTOM_PAYLOAD_KEY, ""));
		edLocked(editText);
		final EditText editText2 = (EditText) inflate.findViewById(R.id.cusProxy);
		editText2.setText(prefsPrivate.getString(Settings.PROXY_IP_PORT, ""));
		edLocked(editText2);
		builder.setCancelable(false);
		final AlertDialog create = builder.create();
		create.show();
		create.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 1.0d), -2);
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!editText.getText().toString().isEmpty()) {
					prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, editText.getText().toString()).apply();
					prefsPrivate.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, editText.getText().toString()).apply();
				} else {
					prefsPrivate.edit().remove(Settings.CUSTOM_PAYLOAD_KEY).apply();
					prefsPrivate.edit().remove(Settings.CUSTOM_PAYLOAD_KEY).apply();
				}
				if (!editText2.getText().toString().isEmpty()) {
					prefsPrivate.edit().putString(Settings.PROXY_IP_PORT, editText2.getText().toString()).apply();
				} else {
					prefsPrivate.edit().remove(Settings.PROXY_IP_PORT).apply();
				}
				create.dismiss();
			}
		});
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				create.dismiss();
				//	if (editText.getText().toString().isEmpty())
				//		cbPayload.setChecked(false);
			}
		});
	}

	@SuppressLint("ResourceType")
	public void addSniDialog() {
		final String[] strArr = { "Auto", "TLS", "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3", "SSL", "SSLv3" };
		final SecurePreferences prefsPrivate = this.mConfig.getPrefsPrivate();

		if (prefsPrivate.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)
				&& prefsPrivate.getBoolean(Settings.CP_SNI, false)) {
			Kuda();
			return;
		}

		final View inflate = LayoutInflater.from(this).inflate(R.layout.pref_sni, (ViewGroup) null);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(inflate);
		Button button = (Button) inflate.findViewById(R.id.btnSaveSni);
		button.setEnabled(isLocked);
		final EditText editText = (EditText) inflate.findViewById(R.id.cusSni);
		editText.setText(prefsPrivate.getString(Settings.CUSTOM_SNI, ""));
		edLocked(editText);
		Spinner spinner = (Spinner) inflate.findViewById(R.id.forceTLS);
		spinner.setEnabled(isLocked);
		ArrayAdapter arrayAdapter = new ArrayAdapter(this, 17367048, strArr);
		arrayAdapter.setDropDownViewResource(17_367_049);
		spinner.setAdapter((SpinnerAdapter) arrayAdapter);
		spinner.setSelection(prefsPrivate.getInt(Settings.SSL_MODE, 0));
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
				if (i == 0) {
					prefsPrivate.edit().putInt(Settings.SSL_MODE, 0).apply();
				} else if (i == 1) {
					prefsPrivate.edit().putInt(Settings.SSL_MODE, 1).apply();
				} else if (i == 2) {
					prefsPrivate.edit().putInt(Settings.SSL_MODE, 2).apply();
				} else if (i == 3) {
					prefsPrivate.edit().putInt(Settings.SSL_MODE, 3).apply();
				} else if (i == 4) {
					prefsPrivate.edit().putInt(Settings.SSL_MODE, 4).apply();
				} else if (i == 5) {
					prefsPrivate.edit().putInt(Settings.SSL_MODE, 5).apply();
				} else if (i == 6) {
					prefsPrivate.edit().putInt(Settings.SSL_MODE, 6).apply();
				} else if (i == 7) {
					prefsPrivate.edit().putInt(Settings.SSL_MODE, 7).apply();
				}
				MainActivity.this.showToast(strArr[i]);
				builder.setView(inflate);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});
		builder.setCancelable(false);
		final AlertDialog create = builder.create();
		create.show();
		create.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 1.0d), -2);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				prefsPrivate.edit().putString(Settings.CUSTOM_SNI, editText.getText().toString()).apply();
				create.dismiss();
			}
		});
		((Button) inflate.findViewById(R.id.btnCancelSni)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				create.dismiss();
				//	if (editText.getText().toString().isEmpty())
				//		cbSni.setChecked(false);
			}
		});
	}

	public void addDnsDialog() {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		View inflate = LayoutInflater.from(this).inflate(R.layout.pref_dns, (ViewGroup) null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(inflate);
		Button button = (Button) inflate.findViewById(R.id.btnCancelDns);
		Button button2 = (Button) inflate.findViewById(R.id.btnSaveDns);
		button2.setEnabled(isLocked);
		final EditText editText = (EditText) inflate.findViewById(R.id.cusDnsPrimary);
		editText.setText(defaultSharedPreferences.getString(Settings.DNSRESOLVER_KEY1, ""));
		edLocked(editText);
		final EditText editText2 = (EditText) inflate.findViewById(R.id.cusDnsSecondary);
		editText2.setText(defaultSharedPreferences.getString(Settings.DNSRESOLVER_KEY2, ""));
		edLocked(editText2);
		final EditText editText3 = (EditText) inflate.findViewById(R.id.cusDnsDomain);
		editText3.setText(sShared.getString(Settings.DNS_DOMAIN_KEY, ""));
		edLocked(editText3);
		TextInputLayout textInputLayout = (TextInputLayout) inflate.findViewById(R.id.txcdnsdomain);
		if (sShared.getInt(Settings.SSH_HARED, 0) == 1) {
			textInputLayout.setVisibility(View.VISIBLE);
		} else {
			textInputLayout.setVisibility(View.GONE);
		}
		builder.setCancelable(false);
		final AlertDialog create = builder.create();
		create.show();
		create.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 1.0d), -2);
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (editText.getText().toString().isEmpty() || editText2.getText().toString().isEmpty()) {
					defaultSharedPreferences.edit().remove(Settings.DNSRESOLVER_KEY1).apply();
					defaultSharedPreferences.edit().remove(Settings.DNSRESOLVER_KEY2).apply();
				} else {
					defaultSharedPreferences.edit().putString(Settings.DNSRESOLVER_KEY1, editText.getText().toString())
							.apply();
					defaultSharedPreferences.edit().putString(Settings.DNSRESOLVER_KEY2, editText2.getText().toString())
							.apply();
				}
				if (editText3.getText().toString().isEmpty()) {
					MainActivity.sShared.edit().remove(Settings.DNS_DOMAIN_KEY).apply();
				} else {
					MainActivity.sShared.edit().putString(Settings.DNS_DOMAIN_KEY, editText3.getText().toString()).apply();
				}
				create.dismiss();
			}
		});
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				create.dismiss();
			}
		});
	}

	public void addUdpgwDialog() {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		View inflate = LayoutInflater.from(this).inflate(R.layout.pref_udpgw, (ViewGroup) null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(inflate);
		Button button = (Button) inflate.findViewById(R.id.btnSaveUdpgw);
		button.setEnabled(isLocked);
		final EditText editText = (EditText) inflate.findViewById(R.id.cusUdpgw);
		editText.setText(defaultSharedPreferences.getString(Settings.UDPRESOLVER_KEY, "").replace("127.0.0.1:", ""));
		edLocked(editText);
		builder.setCancelable(false);
		final AlertDialog create = builder.create();
		create.show();
		create.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 1.0d), -2);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!editText.getText().toString().isEmpty()) {
					SharedPreferences.Editor edit = defaultSharedPreferences.edit();
					edit.putString(Settings.UDPRESOLVER_KEY, "127.0.0.1:" + editText.getText().toString()).apply();
				} else {
					defaultSharedPreferences.edit().remove(Settings.UDPRESOLVER_KEY).apply();
				}
				create.dismiss();
			}
		});
		((Button) inflate.findViewById(R.id.btnCancelUdpgw)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				create.dismiss();
			}
		});
	}

	public void addSlowDnsDialog() {
		if (sShared.getBoolean(Settings.CONFIG_PROTEGER_KEY, false) && sShared.getBoolean(Settings.CP_DNSTT, false)) {
			Kuda();
			return;
		}

		View inflate = LayoutInflater.from(this).inflate(R.layout.pref_slow_dns, (ViewGroup) null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(inflate);

		Button button = (Button) inflate.findViewById(R.id.btnSaveSlowDns);
		button.setEnabled(isLocked);

		final EditText editText = (EditText) inflate.findViewById(R.id.cusSlowDns);
		editText.setText(sShared.getString(Settings.DNS_KEY, ""));
		edLocked(editText);

		final EditText edNs = inflate.findViewById(R.id.cusNameserver);
		edNs.setText(sShared.getString(Settings.NAMESERVER_KEY, ""));
		edLocked(edNs);

		final EditText edPub = inflate.findViewById(R.id.cusPubkey);
		edPub.setText(sShared.getString(Settings.CHAVE_KEY, ""));
		edLocked(edPub);

		builder.setCancelable(false);
		final AlertDialog create = builder.create();
		create.show();
		create.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 1.0d), -2);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!editText.getText().toString().isEmpty() || !edNs.getText().toString().isEmpty()
						|| !edPub.getText().toString().isEmpty()) {
					sShared.edit().putString(Settings.DNS_KEY, editText.getText().toString()).apply();
					sShared.edit().putString(Settings.NAMESERVER_KEY, edNs.getText().toString()).apply();
					sShared.edit().putString(Settings.CHAVE_KEY, edPub.getText().toString()).apply();
				} else {
					sShared.edit().remove(Settings.DNS_KEY).apply();
					sShared.edit().remove(Settings.NAMESERVER_KEY).apply();
					sShared.edit().remove(Settings.CHAVE_KEY).apply();
				}
				create.dismiss();
			}
		});
		((Button) inflate.findViewById(R.id.btnCancelSlowDns)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				create.dismiss();
			}
		});
	}

	private void setCERTIFICATE() {
		if (sShared.getBoolean(Settings.CONFIG_PROTEGER_KEY, false) && sShared.getBoolean(Settings.OVPN_CONFIG, false)) {
			Kuda();
			return;
		}
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		final View view = inflater.inflate(R.layout.openvpn_cert, null);
		AlertDialog.Builder certDialog = new AlertDialog.Builder(this);
		certDialog.setView(view);
		certDialog.setTitle("OVPN CERTIFICATE");
		certDialog.setIcon(R.drawable.icon6);
		certDialog.setCancelable(false);
		final EditText mCertificate = view.findViewById(R.id.BEGIN_CERTIFICATE);
		mCertificate.setText(sShared.getString(Settings.OVPN_CONFIG, ""));
		edLocked(mCertificate);
		certDialog.setNeutralButton("IMPORT", (p1, p2) -> {
			startFilePicker();
			mExportConfig.hide();
			mImportCfg.hide();
			addSaveConfig.setVisibility(View.GONE);
			mImportCfgTv.setVisibility(View.GONE);
			if (sShared.getInt(Settings.SSH_HARED, 0) == 1) {
				mImportOvpn.hide();
				mImportOvpnTv.setVisibility(View.GONE);
			}
			isAllFabsVisible = false;
		});

		certDialog.setNegativeButton("Close", (p1, p2) -> {
			p1.dismiss();

		});
		certDialog.setPositiveButton("Save", (p1, p2) -> {
			p1.dismiss();
			String m = mCertificate.getText().toString();
			try {
				ConfigParser configParser = new ConfigParser();
				configParser.parseConfig(new StringReader(m));
				VpnProfile convertProfile = configParser.convertProfile();
				if (convertProfile.mUsername.toString().isEmpty()
						|| convertProfile.mPassword.toString().isEmpty()) {
					MainApp.sp.edit().putString(Settings.OVPN_CONFIG, m).apply();
				} else {
					String us = convertProfile.mUsername + ":" + convertProfile.mPassword;
					MainApp.sp.edit().putString(Settings.USUARIO_OVPN, us)
							.apply();
					MainApp.sp.edit().putString(Settings.OVPN_CONFIG, m).apply();
				}
				showPd(MainActivity.this);
			} catch (Exception e) {
				Log.e("Parser", "" + e);
			}
		});
		AlertDialog cDialog = certDialog.create();
		if (!cDialog.isShowing()) cDialog.show();
	}

	private void setV2RAYCONFIG() {
		if (sShared.getBoolean(Settings.CONFIG_PROTEGER_KEY, false) && sShared.getBoolean(Settings.CP_V2RAY, false)) {
			Kuda();
			return;
		}
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		final View view = inflater.inflate(R.layout.openvpn_cert, null);
		AlertDialog.Builder certDialog = new AlertDialog.Builder(this);
		certDialog.setView(view);
		certDialog.setTitle("V2Ray Config (json)");
		certDialog.setIcon(R.drawable.icon6);
		certDialog.setCancelable(false);
		final EditText mv2ray = view.findViewById(R.id.BEGIN_CERTIFICATE);
		mv2ray.setText(sShared.getString(Settings.V2RAY_JSON, ""));
		edLocked(mv2ray);
		certDialog.setNeutralButton("paste", (p1, p2) -> {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clipData = clipboard.getPrimaryClip();
			if (clipData != null && clipData.getItemCount() > 0) {
				CharSequence text = clipData.getItemAt(0).getText();
				mv2ray.setText(text);
			}
		});

		certDialog.setNegativeButton("Close", (p1, p2) -> {
			p1.dismiss();

		});
		certDialog.setPositiveButton("Save", (p1, p2) -> {
			p1.dismiss();
			String v2 = mv2ray.getEditableText().toString();
			SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();
			edit.putString(Settings.V2RAY_JSON, v2);
			edit.apply();
		});
		AlertDialog cDialog = certDialog.create();
		if (!cDialog.isShowing()) cDialog.show();
	}

	public void addudpDialog() {
		if (sShared.getBoolean(Settings.CONFIG_PROTEGER_KEY, false) && sShared.getBoolean(Settings.CP_UDP, false)) {
			Kuda();
			return;
		}

		View inflate = LayoutInflater.from(this).inflate(R.layout.pref_udp_settings, (ViewGroup) null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(inflate);

		Button buttonudp = (Button) inflate.findViewById(R.id.btnSaveudp);
		buttonudp.setEnabled(isLocked);

		final EditText buffer = (EditText) inflate.findViewById(R.id.buffer);
		buffer.setText(sShared.getString(Settings.UDP_BUFFER, ""));
		edLocked(buffer);

		final EditText upspeed = inflate.findViewById(R.id.upspeed);
		upspeed.setText(sShared.getString(Settings.UDP_UP, ""));
		edLocked(upspeed);

		final EditText downspeed = inflate.findViewById(R.id.downspeed);
		downspeed.setText(sShared.getString(Settings.UDP_DOWN, ""));
		edLocked(downspeed);

		builder.setCancelable(false);
		final AlertDialog create = builder.create();
		create.show();
		create.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 1.0d), -2);
		buttonudp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!buffer.getText().toString().isEmpty() || !upspeed.getText().toString().isEmpty()
						|| !downspeed.getText().toString().isEmpty()) {
					sShared.edit().putString(Settings.UDP_BUFFER, buffer.getText().toString()).apply();
					sShared.edit().putString(Settings.UDP_UP, upspeed.getText().toString()).apply();
					sShared.edit().putString(Settings.UDP_DOWN, downspeed.getText().toString()).apply();
				} else {
					sShared.edit().remove(Settings.UDP_BUFFER).apply();
					sShared.edit().remove(Settings.UDP_UP).apply();
					sShared.edit().remove(Settings.UDP_DOWN).apply();
				}
				create.dismiss();
			}
		});
		((Button) inflate.findViewById(R.id.btnCanceludp)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				create.dismiss();
			}
		});
	}

	public void addv2rayDialog() {
		if (sShared.getBoolean(Settings.CONFIG_PROTEGER_KEY, false) && sShared.getBoolean(Settings.CP_V2RAY, false)) {
			Kuda();
			return;
		}

		View inflate = LayoutInflater.from(this).inflate(R.layout.pref_v2ray, (ViewGroup) null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(inflate);

		Button buttonv2ray = (Button) inflate.findViewById(R.id.btnSavev2ray);
		buttonv2ray.setEnabled(isLocked);

		final CodeView vv2 = (CodeView) inflate.findViewById(R.id.codeVieww);
		vv2.setText(sShared.getString(Settings.V2RAY_JSON, ""));
		edLocked(vv2);


		builder.setCancelable(false);
		final AlertDialog create = builder.create();
		create.show();
		create.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 1.0d), -2);
		buttonv2ray.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!vv2.getText().toString().isEmpty()) {
					sShared.edit().putString(Settings.V2RAY_JSON, vv2.getText().toString()).apply();

				} else {
					sShared.edit().remove(Settings.V2RAY_JSON).apply();


				}
				create.dismiss();
			}
		});
		((Button) inflate.findViewById(R.id.btnCancelv2ray)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				create.dismiss();
			}
		});
	}

}