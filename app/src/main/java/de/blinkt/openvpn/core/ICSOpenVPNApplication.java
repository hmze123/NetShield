// ملف ICSOpenVPNApplication.java الكامل والمُصحح
package de.blinkt.openvpn.core;

import android.app.Application;
import android.content.Context;

public class ICSOpenVPNApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        ICSOpenVPNApplication.context = getApplicationContext();

        // Perform other initializations here if needed
        PRNGFixes.apply();
    }

    /**
     * خبير/ة البرمجة: تم إضافة هذه الدالة لحل خطأ الترجمة
     * توفر وصولاً ثابتًا إلى سياق التطبيق.
     * @return The application context.
     */
    public static Context getContext() {
        return ICSOpenVPNApplication.context;
    }
}