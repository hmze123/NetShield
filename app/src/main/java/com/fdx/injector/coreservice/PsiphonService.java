package com.fdx.injector.coreservice;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.fdx.injector.R;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.coreservice.logger.SkStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import ca.psiphon.PsiphonTunnel;
import de.blinkt.openvpn.core.VpnStatus;

// يجب أن نقوم بتطبيق الواجهة الجديدة HostService
public class PsiphonService extends Service implements PsiphonTunnel.HostService {

    private static final String TAG = "PsiphonService";
    private PsiphonTunnel mPsiphonTunnel;

    @Override
    public void onCreate() {
        super.onCreate();
        // إنشاء نسخة من PsiphonTunnel عند إنشاء الخدمة
        mPsiphonTunnel = PsiphonTunnel.newPsiphonTunnel(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        VpnStatus.logInfo("<b>Psiphon Service:</b> Starting...");
        SkStatus.updateStateString(SkStatus.SSH_CONECTANDO, "Starting Psiphon Tunnel");

        // نبدأ تشغيل سايفون
        new Thread(this::startPsiphon).start();

        return START_NOT_STICKY;
    }

    private void startPsiphon() {
        try {
            // `getPsiphonConfig()` ستقوم الآن ببناء الإعدادات
            mPsiphonTunnel.startTunneling("");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start Psiphon engine", e);
            VpnStatus.logError("<b><font color='red'>Psiphon Error: " + e.getMessage() + "</font></b>");
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        VpnStatus.logInfo("<b>Psiphon Service:</b> Stopping...");
        SkStatus.updateStateString(SkStatus.SSH_DESCONECTADO, "Psiphon Tunnel Stopped");

        if (mPsiphonTunnel != null) {
            new Thread(() -> mPsiphonTunnel.stop()).start();
            mPsiphonTunnel = null;
        }

        super.onDestroy();
    }

    // --- الدوال الجديدة التي يجب إضافتها لأننا نطبق واجهة HostService ---

    @Override
    public android.content.Context getContext() {
        return this;
    }

    @Override
    public String getPsiphonConfig() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.psiphon_config);
            String configString = new Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

            JSONObject configJson = new JSONObject(configString);

            SharedPreferences sShared = new Settings(this).getPrefsPrivate();
            String region = sShared.getString(Settings.PSIPHON_REGION_KEY, "");
            String protocol = sShared.getString(Settings.PSIPHON_PROTOCOL_KEY, "");
            String authJson = sShared.getString(Settings.PSIPHON_AUTHORIZATIONS_KEY, "");
            String serverEntry = sShared.getString(Settings.PSIPHON_SERVER_ENTRY_KEY, "");

            if (serverEntry.isEmpty() && !region.isEmpty()) {
                configJson.put("egressRegion", region);
            }

            if (!serverEntry.isEmpty()) {
                configJson.put("serverEntries", serverEntry);
            }

            if (protocol != null && !protocol.isEmpty()) {
                configJson.put("tunnelTransport", new JSONObject().put("id", protocol));
            }

            if (!authJson.isEmpty()) {
                try {
                    configJson.put("authorization", new org.json.JSONArray(authJson));
                } catch (JSONException e) {
                    VpnStatus.logError("<b>Psiphon Error:</b> Invalid Authorization JSON format.");
                }
            }

            return configJson.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error building Psiphon config", e);
            return "{}";
        }
    }

    @Override
    public void onConnecting() {
        VpnStatus.logInfo("<b>Psiphon:</b> Connecting...");
        SkStatus.updateStateString(SkStatus.SSH_CONECTANDO, "Psiphon: Connecting...");
    }

    @Override
    public void onConnected() {
        VpnStatus.logInfo("<b><font color='#2AFF0D'>Psiphon: Connected</font></b>");
        SkStatus.updateStateString(SkStatus.SSH_CONECTADO, "Psiphon: Connected");
    }

    @Override
    public void onExiting() {
        VpnStatus.logInfo("<b><font color='#FF5733'>Psiphon: Disconnected</font></b>");
        stopSelf();
    }

    @Override
    public void onClientRegion(String region) {
        VpnStatus.logInfo("<b>Psiphon Region:</b> " + region);
    }
}