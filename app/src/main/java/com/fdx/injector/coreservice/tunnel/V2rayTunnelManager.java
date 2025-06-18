package com.fdx.injector.coreservice.tunnel;

import android.content.Context;
import android.content.Intent;
import com.fdx.injector.coreservice.V2Service;

/**
 * هذا هو المدير المسؤول عن نفق V2Ray.
 * إنه يطبق واجهة ITunnelManager، ويحتوي على كل المنطق
 * الخاص ببدء وإيقاف والتحقق من حالة خدمة V2Ray.
 */
public class V2rayTunnelManager implements ITunnelManager {

    @Override
    public void startTunnel(Context context) {
        // إرسال أمر لبدء خدمة V2Ray
        Intent v2rayIntent = new Intent(context, V2Service.class);
        context.startService(v2rayIntent);
    }

    @Override
    public void stopTunnel(Context context) {
        // إرسال أمر لإيقاف خدمة V2Ray
        Intent v2rayIntent = new Intent(context, V2Service.class);
        context.stopService(v2rayIntent);
    }

    @Override
    public boolean isRunning() {
        // نستخدم نفس الطريقة للتحقق من أن الخدمة تعمل
        // عبر متغير ثابت (static) داخل خدمة V2Service.
        return V2Service.isServiceRunning;
    }
}
