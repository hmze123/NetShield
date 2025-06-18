package com.fdx.injector.ActivityTools;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.fdx.injector.R;
import android.graphics.Color;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;


public class subfinder extends AppCompatActivity {


    TextView subdomainList;
    private ArrayList<String> subdomains;
    private EditText domainName;
    private Button discoverButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subdomainfinder);
        androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_main);
        mToolbar.setSubtitle("Subdomain Finder");
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        domainName = findViewById(R.id.domainName);
        discoverButton = findViewById(R.id.getSubdomains);
        subdomainList = findViewById(R.id.subdomains);

        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String baseDomain = domainName.getText().toString();
                // Perform subdomain discovery here using a library or API
                // and display the results in the subdomainsTextView
                if (baseDomain.length()>0){
                    new SubdomainDiscoveryTask().execute(baseDomain);
                    Toast.makeText(subfinder.this, "Searching for host...", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(subfinder.this, "Please enter a domain", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subfinder, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.CopyLogs:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("subdomains", subdomainList.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;   
        }
        return false;
    }


    private class SubdomainDiscoveryTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String baseDomain = params[0];
            String urlString = "https://www.virustotal.com/vtapi/v2/domain/report?domain=" + baseDomain + "&apikey=1d0d5f5070c2a9154bc001264a231c9e12e58f78df26f6f19410d2052d330e85";
            String subdomainsString = "";
            try {
                URL url = new URL(urlString);
                Scanner scanner = new Scanner(url.openStream());
                String response = "";
                while (scanner.hasNext()) {
                    response += scanner.nextLine();
                }
                scanner.close();
                int counter = 1;
                JSONObject json = new JSONObject(response);
                JSONArray subdomains = json.getJSONArray("subdomains");
                for (int i = 0; i < subdomains.length(); i++) {
                    subdomainsString += counter + ". " + subdomains.getString(i) + "\n";
                    counter++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return subdomainsString;
        }

        @Override
        protected void onPostExecute(String subdomains) {
            subdomainList.setText(subdomains);
            registerForContextMenu(subdomainList);

        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, "Copy");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Copy") {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("subdomains", subdomainList.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }
@Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }



}

