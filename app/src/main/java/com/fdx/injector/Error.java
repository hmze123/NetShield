package com.fdx.injector;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import com.fdx.injector.R;

public class Error extends AppCompatActivity {
    TextView error;
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.gravity = 17;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setLayoutParams(layoutParams);
        setContentView(linearLayout);
        ScrollView sv = new ScrollView(this);
        TextView error = new TextView(this);
        sv.addView(error);
        linearLayout.addView(sv);
        error.setText(getIntent().getStringExtra("error"));
    }
}






