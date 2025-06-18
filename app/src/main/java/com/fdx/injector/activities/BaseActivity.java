package com.fdx.injector.activities;

import static android.content.pm.PackageManager.GET_META_DATA;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.fdx.injector.coreservice.config.Settings;

public abstract class BaseActivity extends AppCompatActivity
{
	public static int mTheme = 0;
	private int night_mode;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		resetTitles();
		setModoNoturnoLocal();
	}
	
	public void setModoNoturnoLocal() {
        if (new Settings(this).getModoNoturno().equals("on")) {
            night_mode = 2;
        } else {
            night_mode = 1;
        }
        getDelegate().setLocalNightMode(night_mode);
    }

	protected void resetTitles() {
		try {
			ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
			if (info.labelRes != 0) {
				setTitle(info.labelRes);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}
