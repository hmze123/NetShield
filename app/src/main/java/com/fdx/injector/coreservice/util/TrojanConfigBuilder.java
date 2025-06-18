package com.fdx.injector.coreservice.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.fdx.injector.coreservice.config.Settings;
import org.json.JSONArray;
import org.json.JSONObject;

public class TrojanConfigBuilder {

    private static final String TAG = "TrojanConfigBuilder";

    public static String buildConfig(Context context) {
        SharedPreferences sShared = new Settings(context).getPrefsPrivate();

        try {
            // قراءة كل الإعدادات المحفوظة من الواجهة
            String address = sShared.getString(Settings.TROJAN_ADDRESS_KEY, "");
            int port = Integer.parseInt(sShared.getString(Settings.TROJAN_PORT_KEY, "443"));
            String password = sShared.getString(Settings.TROJAN_PASSWORD_KEY, "");
            String sni = sShared.getString(Settings.TROJAN_SNI_KEY, address); // استخدام العنوان كـ SNI افتراضي

            if (address.isEmpty() || password.isEmpty()) {
                Log.e(TAG, "Trojan server address or password is empty");
                return null;
            }

            // كائن الإعدادات الرئيسي
            JSONObject finalConfig = new JSONObject();

            // --- قسم الـ outbounds (الوجهة) ---
            JSONArray outboundsArray = new JSONArray();
            JSONObject trojanOutbound = new JSONObject();

            trojanOutbound.put("type", "trojan");
            trojanOutbound.put("tag", "trojan-out");
            trojanOutbound.put("server", address);
            trojanOutbound.put("server_port", port);
            trojanOutbound.put("password", password);

            // إعدادات TLS/SNI
            JSONObject tlsSettings = new JSONObject();
            tlsSettings.put("enabled", true);
            tlsSettings.put("server_name", sni);
            tlsSettings.put("insecure", false);

            trojanOutbound.put("tls", tlsSettings);

            outboundsArray.put(trojanOutbound);
            finalConfig.put("outbounds", outboundsArray);

            // --- إعدادات أساسية أخرى (log, inbounds, dns) ---
            finalConfig.put("log", new JSONObject().put("level", "warn"));

            JSONArray inboundsArray = new JSONArray();
            JSONObject socksInbound = new JSONObject();
            socksInbound.put("type", "socks");
            socksInbound.put("tag", "socks-in");
            socksInbound.put("listen", "127.0.0.1");
            socksInbound.put("listen_port", 10808); // منفذ SOCKS المحلي
            inboundsArray.put(socksInbound);
            finalConfig.put("inbounds", inboundsArray);

            Log.d(TAG, "Generated Trojan Config: " + finalConfig.toString(2));
            return finalConfig.toString();

        } catch (Exception e) {
            Log.e(TAG, "Error building Trojan config", e);
            return null;
        }
    }
}