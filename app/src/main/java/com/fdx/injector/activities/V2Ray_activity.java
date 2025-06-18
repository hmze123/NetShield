package com.fdx.injector.activities;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.fragment.app.DialogFragment;

import com.amrdeveloper.codeview.CodeView;
import com.fdx.injector.MainApp;
import com.fdx.injector.R;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.fragment.FragmentDialogLock;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.github.muddz.styleabletoast.StyleableToast;

public class V2Ray_activity extends BaseActivity {


	private CodeView codeVieww;
	private LanguageManager languageManager;
    public static SharedPreferences mPrefs;
	public static SharedPreferences sShared;
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
    protected void onResume() {
		
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.v2ray_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Menu Itens
        switch (item.getItemId()) {

            case R.id.pega:
				
				
				break;
        }

        return super.onOptionsItemSelected(item);
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.v2ray_activity);
		codeVieww = findViewById(R.id.codeVieww);
        
		mHandler = new Handler();
        
        /*androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
       
		//Numbers
		codeVieww.setEnableLineNumber(true);
        codeVieww.isLineNumberEnabled();
        codeVieww.setLineNumberTextColor(Color.WHITE);
       // codeVieww.setTextColor(Color.WHITE);
       /* codeView.reHighlightSyntax();
        codeView.resetHighlighter();
        codeView.reHighlightErrors();
		codeView.setLineNumberTextColor(Color.WHITE);*/
		codeVieww.setLineNumberTextSize(35f);

		codeVieww.setText(MainApp.sp.getString(Settings.V2RAY_JSON, ""));

		languageManager = new LanguageManager(this, codeVieww);
		languageManager.applyTheme(currentLanguage, currentTheme);

		Init();
	}
    

    public static String getClipboard(Context context) {
        try {
            ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData.Item item = cmb.getPrimaryClip().getItemAt(0);
            return item.getText().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    
    
    public void Kuda() {
		DialogFragment fragLck = new FragmentDialogLock();
		fragLck.show(getSupportFragmentManager(), "Poni");
	}

	public void Init() {
		fab = findViewById(R.id.save_v2ray_fab);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String v = codeVieww.getText().toString();
                    

				try {
					
					
					//	MainApp.sp.edit().putString(Settings.V2RAY_JSON, m).apply();
						MainApp.sp.edit().putString(Settings.V2RAY_JSON, v).apply();
					
					showPd(V2Ray_activity.this);
				} catch (Exception e) {
					Log.e("Parser", "" + e);
				}

			}
		});

	}
}