package com.fdx.injector.ActivityTools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.material.textview.MaterialTextView;
import com.fdx.injector.R;
import com.fdx.injector.activities.BaseActivity;
import com.fdx.injector.util.Worker2;
import com.fdx.injector.util.WorkerAction;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;


public class HostChecker extends BaseActivity {
    private static final String TAG = "HostChecker";
    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    private Button Button;
    private TextInputEditText bugHost, proxy;
    private CheckBox direct, cdn;
    private String c, domain, ipProxy, method, portProxy;
    private ListView list;
    private LinearLayout main;
    private SharedPreferences sp;
    private TextInputLayout Txthost1, Txthost2;
    private boolean cdnRunning = false;

    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        savePreferences("hostChecker", bugHost.getText().toString().trim());
        savePreferences("proxyChecker", proxy.getText().toString().trim());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();

        if (bugHost.getText().toString().isEmpty()) {
            Txthost1.setVisibility(View.GONE);
        } else{
            Txthost1.setVisibility(View.VISIBLE);
        }

        if (proxy.getText().toString().isEmpty()) {
            Txthost2.setVisibility(View.GONE);
        } else{
            Txthost2.setVisibility(View.VISIBLE);
        }

        if (sp == null) {
            sp = PreferenceManager.getDefaultSharedPreferences(this);
        }
        bugHost.setText(sp.getString("hostChecker", ""));
        proxy.setText(sp.getString("proxyChecker", ""));
    }

    public void onDestroy() {
        super.onDestroy();
    }


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.h_hc);
       // Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
       // setSupportActionBar(mToolbar);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_main);
        mToolbar.setSubtitle("Host Checker");
        mToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        main = findViewById(R.id.mainV);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        c = sharedPreferences.getString("Rise", "");
        list = findViewById(R.id.listLogs);
        arrayList = new ArrayList();
        adapter = new ArrayAdapter(getApplicationContext(), R.layout.list_text, arrayList);
        list.setAdapter(adapter);
        Txthost1 = findViewById(R.id.Txthost1);
        Txthost2 = findViewById(R.id.Txthost2);
        bugHost = findViewById(R.id.editTextUrl);
        proxy = findViewById(R.id.editTextProxy);
        proxy.setVisibility(View.VISIBLE);
        spinner = findViewById(R.id.spinnerRequestMethod);
        spinner.getSelectedItem().toString();
        direct = findViewById(R.id.checkBoxDirectRequest);
        cdn = findViewById(R.id.cbSubdomain);
        spinner = findViewById(R.id.spinnerRequestMethod);
        spinner.getSelectedItem().toString();

        bugHost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = bugHost.getText().toString().trim();
                if(name.isEmpty()){
                    Txthost1.setVisibility(View.GONE);
                } else {
                    Txthost1.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        proxy.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = proxy.getText().toString().trim();
                if(name.isEmpty()){
                    Txthost2.setVisibility(View.GONE);
                } else {
                    Txthost2.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        list.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Log Entry", ((MaterialTextView) view).getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), Html.fromHtml("Copied to Clipboar"), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        direct.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (direct.isChecked()) {
                    SharedPreferences.Editor	edit = sharedPreferences.edit();
                    edit.putBoolean("Xen", true);
                    edit.commit();
                    proxy.setEnabled(false);
                    return;
                }
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("Xen", false);
                edit.commit();
                proxy.setEnabled(true);
            }
        });

        Button = findViewById(R.id.buttonSearch);
        Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cdn.isChecked() && cdnRunning) {
                    Toast.makeText(getApplicationContext(), "CDN Finder Loding...", Toast.LENGTH_SHORT).show();
                    cdnRunning = false;
                    Button.setEnabled(false);
                    return;
                }

                if (bugHost.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<font color=\"red\">Please Fill The URL</font>"), Toast.LENGTH_LONG).show();
                } else if (direct.isChecked()) {
                    start();
                } else if (proxy.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), Html.fromHtml("<font color=\"red\">fill the proxy if you want to check or select the direct to check the url</font>"), Toast.LENGTH_LONG).show();
                } else {
                    start();
                }
            }
        });

        if (sharedPreferences.getBoolean("Xen", false)) {
            direct.setChecked(true);
            proxy.setEnabled(false);
            return;
        }
        direct.setChecked(false);
        proxy.setEnabled(true);
    }

    public final void showMessage(String str) {
        if (getApplicationContext() != null) {
            Toast.makeText(getApplicationContext(),(Html.fromHtml(str)), 0).show();
        }
    }

    private void savePreferences(String str, String str2) {
        if (sp == null) {
            sp = PreferenceManager.getDefaultSharedPreferences(this);
        }
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(str, str2);
        edit.commit();
    }

    public void start() {
        String editable = bugHost.getText().toString();
        String obj = spinner.getSelectedItem().toString();
        Button.setEnabled(false);

        String trim = proxy.getText().toString().trim();
        if (trim.contains(":")) {
            String[] split = trim.split(":");
            ipProxy = split[0];
            portProxy = split[1];
        } else {
            ipProxy = trim;
            portProxy = "80";
        }

        if (direct.isChecked()) {
            arrayList.add(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(obj).append(" - ").toString()).append("URL: https://").toString()).append(editable).toString());
            arrayList.add(new StringBuffer().append(new StringBuffer().append(editable).append(" - ").toString()).append("Direct").toString());
            adapter.notifyDataSetChanged();
        } else {
            arrayList.add(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(obj).append(" - ").toString()).append("URL: http://").toString()).append(editable).toString());
            arrayList.add(new StringBuffer().append(new StringBuffer().append(editable).append(" - ").toString()).append(ipProxy).toString());
            adapter.notifyDataSetChanged();
        }

        // CDN Finder Class
       /*if (cdn.isChecked()) {
           HostChecker hostChecker = new HostChecker();
           hostChecker.findSubdomains(editable, new HostChecker.SubdomainCallback() {
            @Override
            public void onResult(List<String> subdomains) {
                List<String> uniqueSubdomains = new ArrayList<>();
                for (String subdomain : subdomains) {
                    if (!uniqueSubdomains.contains(subdomain)) {
                        uniqueSubdomains.add(subdomain);
                    }
                }
                    List<String> result = new ArrayList<>();
                    result.add("━━━━━━━━━━━━━━━━ Subdomain ━━━━━━━━━━━━━━━━");
                    for (String subdomain : uniqueSubdomains) {
                        try {
                            InetAddress address = InetAddress.getByName(subdomain);
                            String ipAddress = address.getHostAddress();
                            if (!ipAddress.equals("Unknown")) {
                            String ipInfo = getIPInfo(ipAddress);
                            //result.add(subdomain + " ==> " + ipAddress + " ==> (" + ipInfo + ")");

                            result.add(String.format("%-40s %-20s %s", subdomain, ipAddress, ipInfo));

                            //result.add(subdomain);
                            //result.add(ipAddress + " ==> (" + ipInfo + ")");
                            }
                        } catch (UnknownHostException e) {
                            //result.add(subdomain + " ==> Unknown");
                        }
                    }
                    result.add("━━━━━━━━━━━━━━━━ Done ━━━━━━━━━━━━━━━━");
                    runOnUiThread(() -> {
                        arrayList.addAll(result);
                        adapter.notifyDataSetChanged();
                   });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        arrayList.add("CDN Finder Error : " + error);
                        adapter.notifyDataSetChanged();
                    });
                }
            });
        }*/

        if (cdn.isChecked()) {
            cdnRunning = true;
            HostChecker hostChecker = new HostChecker();
            hostChecker.findSubdomains(editable, new HostChecker.SubdomainCallback() {
                @Override
                public void onResult(List<String> subdomains) {
                    List<String> uniqueSubdomains = new ArrayList<>();
                    for (String subdomain : subdomains) {
                        if (!uniqueSubdomains.contains(subdomain)) {
                            uniqueSubdomains.add(subdomain);
                        }
                    }
                    List<String> result = new ArrayList<>();
                    result.add("━━━━━━━━━━━━━━━━ Subdomain ━━━━━━━━━━━━━━━━");
                    for (String subdomain : uniqueSubdomains) {
                        try {
                            InetAddress address = InetAddress.getByName(subdomain);
                            String ipAddress = address.getHostAddress();
                            if (!ipAddress.equals("Unknown")) {
                                String ipInfo = getIPInfo(ipAddress);
                                String nginxInfo = "";
                                try {
                                    URL url = new URL("http://" + subdomain);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.connect();
                                    String serverHeader = connection.getHeaderField("Server");
                                    if (serverHeader != null && serverHeader.contains("nginx")) {
                                        nginxInfo = " (Nginx)";
                                    }
                                } catch (Exception e) {
                                    // Ignore
                                }
                                result.add(String.format("%-40s %-20s (%s) %s", subdomain, ipAddress, ipInfo, nginxInfo));
                            }
                        } catch (UnknownHostException e) {
                            //result.add(subdomain + " ==> Unknown");
                        }
                    }
                    result.add("━━━━━━━━━━━━━━━━ Done ━━━━━━━━━━━━━━━━");
                    runOnUiThread(() -> {
                        arrayList.addAll(result);
                        adapter.notifyDataSetChanged();
                        cdnRunning = false;
                        Button.setEnabled(true);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        arrayList.add("CDN Finder Error : " + error);
                        adapter.notifyDataSetChanged();
                        cdnRunning = false;
                        Button.setEnabled(true);
                    });
                }
            });
        }

        new Worker2(new WorkerAction() {
            private HttpURLConnection conn;
            String response = "";

            public void runLast() {
                Log.d(HostChecker.TAG, "DONE");

                if (response.contains("\n")) {
                    String[] split = response.split("\n");
                    for (String add : split) {
                        arrayList.add(add);
                        adapter.notifyDataSetChanged();
                    }
                    arrayList.add(" ");
                    arrayList.add("━━━━━━━━━━━━━━━━ Stopped ━━━━━━━━━━━━━━━━");
                    arrayList.add((" "));
                    adapter.notifyDataSetChanged();
                    showMessage("Success");
                    Button.setEnabled(true);
                }
            }

            public void runFirst() {
                if (cdn.isChecked()) {
                    runOnUiThread(() -> {
                        arrayList.add("CDN Finder Loading...");
                        adapter.notifyDataSetChanged();
                    });
                }
                try {
                    Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(ipProxy, Integer.parseInt(portProxy)));
                    domain = bugHost.getText().toString();
                    method = spinner.getSelectedItem().toString();
                    if (direct.isChecked()) {
                        conn = (HttpURLConnection) new URL(new StringBuffer().append("https://").append(domain).toString()).openConnection();
                        conn.setRequestMethod(method);
                    } else {
                        conn = (HttpURLConnection) new URL(new StringBuffer().append("http://").append(domain).toString()).openConnection(proxy);
                        conn.setRequestMethod(method);
                    }
                    for (Entry entry : conn.getHeaderFields().entrySet()) {
                        if (((String) entry.getKey()) == null) {
                            response = new StringBuffer().append(response).append(new StringBuffer().append(((List) entry.getValue()).toString().replace("[", "").replace("]", "")).append("\n").toString()).toString();
                        } else {
                            response = new StringBuffer().append(response).append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append((String) entry.getKey()).append(" : ").toString()).append(((List) entry.getValue()).toString().replace("[", "").replace("]", "")).toString()).append("\n").toString()).toString();
                        }
                    }
                } catch (Exception e) {
                }
            }
        }, this).execute(new Void[0]);
    }

    // CDN Finder
    interface SubdomainCallback {
        void onResult(List<String> subdomains);
        void onError(String errorMessage);
    }

    /*public void findSubdomains(String domain, SubdomainCallback callback) {
        new Thread(() -> {
            List<String> subdomains = new ArrayList<>();
            //String apiUrl = "https://crt.sh/?q=%25." + domain + "&output=json";
            String apiUrl = "https://api.c99.nl/" + domain;

            String response = sendGetRequest(apiUrl, "GET");
            if (response == null) {
                callback.onError("Failed to get subdomains");
                return;
            }

            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String subdomain = jsonObject.getString("name_value");
                    if (!subdomain.contains("Unknown")) {
                        subdomains.add(subdomain);
                    }
                }
                callback.onResult(subdomains);
            } catch (JSONException e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }*/

    public void findSubdomains(String domain, SubdomainCallback callback) {
        new Thread(() -> {
            List<String> subdomains = new ArrayList<>();
            String apiUrl = "https://crt.sh/?q=%25." + domain + "&output=json";

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                connection.disconnect();

                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String subdomain = jsonObject.getString("name_value");
                    subdomains.add(subdomain);
                }
                callback.onResult(subdomains);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }


    private String sendGetRequest(String url, String method) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(method);
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                char[] buffer = new char[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    response.append(buffer, 0, length);
                }
                in.close();
                return response.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e("HostChecker", "Failed to send request", e);
            return null;
        }
    }

    private String getIPInfo(String ipAddress) {
        String apiUrl = "http://ip-api.com/json/" + ipAddress;
        String response = sendGetRequest(apiUrl, method);
        try {
            JSONObject jsonObject = new JSONObject(response);
            String isp = jsonObject.getString("isp");
            return isp;
        } catch (JSONException e) {
            Log.e("HostChecker", "JSON Error", e);
            return "JSON Error";
        }
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_logs:
                clearLogs();
                return true;

            case R.id.action_copy_logs:
                copyLogs();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearLogs() {
        arrayList.clear();
        adapter.notifyDataSetChanged();
    }

    private void copyLogs() {
        StringBuilder logs = new StringBuilder();
        for (String log : arrayList) {
            logs.append(log).append("\n");
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Log", logs.toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copy Logs to Clipboard", Toast.LENGTH_SHORT).show();
    }

}