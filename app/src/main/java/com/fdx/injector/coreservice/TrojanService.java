package com.fdx.injector.coreservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.fdx.injector.coreservice.logger.SkStatus;
import com.fdx.injector.coreservice.tunnel.vpn.TunnelState;
import com.fdx.injector.coreservice.tunnel.vpn.TunnelVpnService;
import com.fdx.injector.coreservice.util.TrojanConfigBuilder;

import de.blinkt.openvpn.core.VpnStatus;
import lib.sing.box.SingBox;
import lib.sing.box.SingBoxService;

public class TrojanService extends Service {

    private SingBoxService singBoxService;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        VpnStatus.logInfo("<b>Trojan Service:</b> Starting...");
        SkStatus.updateStateString(SkStatus.SSH_CONECTANDO, "Starting Trojan Tunnel");

        // بدء النفق في ثريد منفصل لمنع تجميد الواجهة
        new Thread(this::startTrojanTunnel).start();

        return START_NOT_STICKY;
    }

    private void startTrojanTunnel() {
        String config = TrojanConfigBuilder.buildConfig(this);
        if (config == null) {
            VpnStatus.logError("<b><font color='red'>Trojan Error:</font></b> Failed to build config from settings. Check fields.");
            stopSelf();
            return;
        }

        try {
            // إنشاء خدمة sing-box جديدة
            singBoxService = SingBox.newService(null);
            // بدء الخدمة مع الإعدادات
            singBoxService.start(config);
            // بدء خدمة الـ VPN لجعل كل التطبيقات تستخدم النفق
            startVpnService();

            SkStatus.updateStateString(SkStatus.SSH_CONECTADO, "Trojan: Connected");
            VpnStatus.logInfo("<b><font color='#2AFF0D'>Trojan: Connected</font></b>");

        } catch (Exception e) {
            VpnStatus.logError("<b><font color='red'>Trojan Error:</font></b> " + e.getMessage());
            stopSelf();
        }
    }

    private void stopTrojanTunnel() {
        VpnStatus.logInfo("<b>Trojan Service:</b> Stopping...");
        if (singBoxService != null) {
            try {
                singBoxService.close();
            } catch (Exception e) {
                // ignore
            }
            singBoxService = null;
        }
        stopVpnService();
    }

    private void startVpnService() {
        if (!TunnelState.isVpnRunning()) {
            Intent intent = new Intent(this, TunnelVpnService.class);
            intent.setAction(TunnelVpnService.ACTION_START);
            startService(intent);
        }
    }

    private void stopVpnService() {
        if (TunnelState.isVpnRunning()) {
            Intent intent = new Intent(this, TunnelVpnService.class);
            intent.setAction(TunnelVpnService.ACTION_STOP);
            startService(intent);
        }
    }

    @Override
    public void onDestroy() {
        stopTrojanTunnel();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}