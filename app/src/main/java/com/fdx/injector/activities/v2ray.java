package com.fdx.injector.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.airbnb.lottie.LottieAnimationView;
import com.fdx.injector.R;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.coreservice.logger.SkStatus;
import com.fdx.injector.coreservice.util.securepreferences.SecurePreferences;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import es.dmoral.toasty.Toasty;

public class v2ray extends BaseActivity {

	private Toolbar tb;
	private View changelog, license, dev;
	private AlertDialog.Builder ab;
	private TextView bloqueda;
    private FloatingActionButton fab;
	private LottieAnimationView block;
   private TextInputLayout v2;
    
   private ClipboardManager clipboardManager;
    private TextInputEditText codeView;
    public Settings mConfig;
	private AdView adsBannerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.v2ray_settings);
       
          final SharedPreferences defaultSharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this);
        
        Settings settings = new Settings(this);
        mConfig = settings;
		tb = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(tb);
        block = findViewById(R.id.block);
		v2 = findViewById(R.id.v2);
		
		block.loop(true);
        block.playAnimation();
        bloqueda = findViewById(R.id.bloqueda);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		codeView = findViewById(R.id.v2ray_json_config);
       clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
       doUpdateLayout();
       
        final SecurePreferences prefsPrivate = mConfig.getPrefsPrivate();
codeView.setText(prefsPrivate.getString(Settings.V2RAY_JSON,""));
		
        
}
    
    
    
   private void doUpdateLayout() {
        SharedPreferences prefs = mConfig.getPrefsPrivate();
        boolean isRunning = SkStatus.isTunnelActive();
        boolean protect = prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false);
        
       
                if (protect) {
                    codeView.setEnabled(false);
                
					
					
					
					block.setVisibility(View.VISIBLE);
					
               bloqueda.setVisibility(View.VISIBLE);
            v2.setVisibility(View.GONE);
                codeView.setVisibility(View.GONE);
            
            
                } else {
            
           if (SkStatus.isTunnelActive()){
                
           codeView.setEnabled(false);
                
                
                
                }
            
            
            
            
            v2.setVisibility(View.VISIBLE);
                    codeView.setVisibility(View.VISIBLE);
            block.setVisibility(View.GONE);
                    bloqueda.setVisibility(View.GONE);
                    
            }
                
            
        
        }
    
    
    
    
    
    
      @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.v2ray_menu, menu);
        
				
   return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            
            
case R.id.pega:
            SharedPreferences prefs = mConfig.getPrefsPrivate();
        boolean isRunning = SkStatus.isTunnelActive();
        boolean protect = prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false);
           if (protect) {
                   error();
                } else {
                
               if (!SkStatus.isTunnelActive()) {
					
                    pasteFromClipboard();
				} 
                
                 else {
              Toasty.error(this, (R.string.error_vpn_sniffer_detected), Toast.LENGTH_SHORT, true).show();
                    
                    }
            }
				
			  break;
            
           case R.id.borrar:
                
               SharedPreferences prefs2 = mConfig.getPrefsPrivate();
        boolean protect6 = prefs2.getBoolean(Settings.CONFIG_PROTEGER_KEY, false);
           if (protect6) {
                   error();
                
                } else {
                
                
                if (!SkStatus.isTunnelActive()) {
                    codeView.setText("");
					
				} else {
				Toasty.error(this, (R.string.error_vpn_sniffer_detected), Toast.LENGTH_SHORT, true).show();
				}
                
                
                
                    }
            
            
           
            
           
			  break;
            
            
            
           case R.id.guardar:
           String v2 = codeView.getEditableText().toString();
                SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();
                edit.putString(Settings.V2RAY_JSON, v2);
                edit.apply();
           finish();
			  break;

        }

        return super.onOptionsItemSelected(item);
    }

    
    
   private void error() {
		Toasty.error(this, "Configuracion bloqueada", Toast.LENGTH_SHORT, true).show();
	}

    
   private void pasteFromClipboard() {
       ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
           ClipData clipData = clipboard.getPrimaryClip();
           if (clipData != null && clipData.getItemCount() > 0) {
               CharSequence text = clipData.getItemAt(0).getText();
               codeView.setText(text);
   }
    
    }
    
}