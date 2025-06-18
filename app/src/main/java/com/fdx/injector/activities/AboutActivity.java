package com.fdx.injector.activities;

import android.net.Uri;
import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.fdx.injector.R;

public class AboutActivity extends AppCompatActivity {

	private TextView aboutme;
	private TextView aboutucf;
	private TextView aboutgt;
	private TextView aboutpg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Initialized();
	}

	public void Initialized() {
	//	Html.fromHtml
	/*	aboutme = (TextView) findViewById(R.id.aboutmep);
		aboutme.setText(Html.fromHtml("<u>" + aboutme.getText().toString() + "</u>"));
		aboutme.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cintent = new Intent("android.intent.action.VIEW", Uri.parse("https://t.me/chetoosz"));
				startActivity(cintent);
			}
		});
		*/
		/*aboutucf = (TextView) findViewById(R.id.aboutucf);
		aboutucf.setText(Html.fromHtml("<u>" + aboutucf.getText().toString() + "</u>"));
		aboutucf.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cintent = new Intent("android.intent.action.VIEW", Uri.parse("https://t.me/UCF69"));
				startActivity(cintent);
			}
		});
		
		aboutgt = (TextView) findViewById(R.id.aboutgt);
		aboutgt.setText(Html.fromHtml("<u>" + aboutgt.getText().toString() + "</u>"));		
		aboutgt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cintent = new Intent("android.intent.action.VIEW", Uri.parse("https://t.me/gretongersteamofficial"));
				startActivity(cintent);
			}
		});
		
		aboutpg = (TextView) findViewById(R.id.aboutpg);
		aboutpg.setText(Html.fromHtml("<u>" + aboutpg.getText().toString() + "</u>"));
		aboutpg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cintent = new Intent("android.intent.action.VIEW", Uri.parse("https://t.me/pejuang_gratisan_group"));
				startActivity(cintent);
			}
		});*/
	}

}