package com.fdx.injector.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.amrdeveloper.codeview.CodeView;
import com.fdx.injector.MainApp;
import com.fdx.injector.R;
import com.fdx.injector.coreservice.config.Settings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.StringReader;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import io.github.muddz.styleabletoast.StyleableToast;

public class OvpnActivity extends BaseActivity {

	private CodeView codeView;
	private LanguageManager languageManager;
	private FloatingActionButton fab;
	private Handler mHandler;

	private LanguageName currentLanguage = LanguageName.PYTHON;
	private ThemeName currentTheme = ThemeName.FIVE_COLOR;

	public void showToast(String str) {
		StyleableToast.makeText(this, str, R.style.mytoast).show();
	}

	public void showPd(Context context) {
		ProgressDialog pd = new ProgressDialog(context);
		pd.setMessage("Please wait");
		pd.setCancelable(false);
		pd.show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
				finish();
			}
		}, 1300L);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ovpn_activity);
		codeView = findViewById(R.id.codeView);
		mHandler = new Handler();

		//Numbers
		codeView.setEnableLineNumber(true);
        codeView.setTextColor(Color.WHITE);
       /* codeView.reHighlightSyntax();
        codeView.resetHighlighter();
        codeView.reHighlightErrors();
		codeView.setLineNumberTextColor(Color.WHITE);
		codeView.setLineNumberTextSize(23f);*/

		codeView.setText(MainApp.sp.getString(Settings.OVPN_CONFIG, ""));

		languageManager = new LanguageManager(this, codeView);
		languageManager.applyTheme(currentLanguage, currentTheme);

		Init();
	}

	public void Init() {
		fab = findViewById(R.id.save_ovpn_fab);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String m = codeView.getText().toString();
				/*	if (!m.isEmpty()){
						MainApp.sp.edit().putString(Settings.OVPN_CONFIG, m).apply();
						showPd(OvpnActivity.this);
					}else{
						showToast("Empty!");
					}*/

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
					showPd(OvpnActivity.this);
				} catch (Exception e) {
					Log.e("Parser", "" + e);
				}

			}
		});

	}

}