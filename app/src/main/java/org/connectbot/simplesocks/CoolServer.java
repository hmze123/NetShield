package org.connectbot.simplesocks;

import android.content.DialogInterface;
import android.app.AlertDialog;
import java.io.IOException;
import java.util.TimerTask;
import java.util.Timer;
import android.os.Handler;
import android.content.pm.PackageManager;

import android.content.Context;

public class CoolServer {
	private Context context;
	private String[] items;

	public CoolServer(Context c, String[] i) {
		context = c;
		items = i;
	}

	private boolean check(String uri) {
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

	public String checkM() {
		String mmaap = null;
		for (int i = 0; i < items.length; i++) {
			if (check(items[i])) {
		//		alert(items[i]);
				mmaap = items[i];
				break;
			}
		}
		return mmaap;
	}

/*	public void init() {
		final Handler handler = new Handler();
		Timer timer = new Timer();
		TimerTask doAsynchronousTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						check();
					}
				});
			}
		};
		timer.schedule(doAsynchronousTask, 0, 3000);
	}*/

	void alert(String app) {
		new AlertDialog.Builder(context)
			.setCancelable(false)
			.setMessage(""+app)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
				}
			}).create().show();	
	}
}
