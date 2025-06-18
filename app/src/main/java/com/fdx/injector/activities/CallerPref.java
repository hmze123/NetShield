package com.fdx.injector.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.appcompat.widget.Toolbar;
import com.fdx.injector.*;
import com.fdx.injector.preference.*;

public class CallerPref extends BaseActivity
	implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);

		PreferenceFragmentCompat preference = new AppPreference();
		Intent intent = getIntent();
		
		String action = intent.getAction();
		
		
		// add preference settings
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.fragment_configLinearLayout, preference)
			.commit();

		// toolbar
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
		// Instantiate the new Fragment
		final Bundle bundle = pref.getExtras();
		final Fragment fragment = Fragment.instantiate(this, pref.getFragment(), bundle);
        
		fragment.setTargetFragment(caller, 0);
       
		// Replace the existing Fragment with the new Fragment
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.fragment_configLinearLayout, fragment)
			.addToBackStack(null)
			.commit();
		
		return true;
	}

	@Override
	public boolean onSupportNavigateUp()
	{
		onBackPressed();
		return true;
	}
}

