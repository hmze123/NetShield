package com.fdx.injector.coreservice.tunnel;

import android.content.Context;
import android.content.Intent;
import com.fdx.injector.coreservice.SocksHttpService;

/**
 * هذا هو المدير المسؤول عن نفق SSH.
 * إنه يطبق واجهة ITunnelManager، ويحتوي على كل المنطق
 * الخاص ببدء وإيقاف والتحقق من حالة خدمة SSH.
 */
public class SshTunnelManager implements ITunnelManager {

    @Override
    public void startTunnel(Context context) {
        // كل ما يفعله هو إرسال أمر لبدء خدمة SSH
        Intent sshIntent = new Intent(context, SocksHttpService.class);
        context.startService(sshIntent);
    }

    @Override
    public void stopTunnel(Context context) {
        // كل ما يفعله هو إرسال أمر لإيقاف خدمة SSH
        Intent sshIntent = new Intent(context, SocksHttpService.class);
        context.stopService(sshIntent);
    }

    @Override
    public boolean isRunning() {
        // للتحقق من أن الخدمة تعمل، نستخدم متغيراً ثابتاً (static)
        // يجب أن يكون موجوداً داخل خدمة SocksHttpService نفسها.
        return SocksHttpService.isServiceRunning;
    }
}