package com.fdx.injector.coreservice;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.coreservice.logger.SkStatus;

// استيراد المكتبات الجديدة الخاصة بـ WireGuard
import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Tunnel;
import com.wireguard.config.Config;
import com.wireguard.config.Peer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class WireGuardService extends VpnService {

    private static final String TAG = "WireGuardService";
    public static final String ACTION_CONNECT = "com.fdx.injector.services.ACTION_CONNECT";
    public static final String ACTION_DISCONNECT = "com.fdx.injector.services.ACTION_DISCONNECT";

    private ParcelFileDescriptor vpnInterface = null;
    private Thread vpnThread = null;
    private Settings mSettings;
    private Backend backend = null;
    private Config wgConfig = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = new Settings(this);
        backend = new GoBackend(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            disconnect();
            return START_NOT_STICKY;
        }

        connect();
        return START_STICKY;
    }

    private void connect() {
        vpnThread = new Thread(() -> {
            try {
                SkStatus.updateStateString(SkStatus.WIREGUARD_CONECTANDO, "Reading WireGuard config...");

                String configText = mSettings.getWgConfig();
                if (configText.isEmpty()) {
                    throw new Exception("WireGuard config is empty");
                }

                InputStream inputStream = new ByteArrayInputStream(configText.getBytes(StandardCharsets.UTF_8));
                wgConfig = Config.parse(inputStream);

                // بناء واجهة الـ VPN بناءً على الإعدادات
                vpnInterface = buildVpn();
                if (vpnInterface == null) {
                    throw new IllegalStateException("Failed to build VPN interface");
                }

                SkStatus.updateStateString(SkStatus.WIREGUARD_CONECTANDO, "Interface prepared, connecting...");

                // === التصحيح هنا: أضفنا (Tunnel) قبل backend ===
                // هذا يخبر المترجم صراحةً أن يتعامل مع backend على أنه من نوع Tunnel
                backend.setState((Tunnel) backend, Tunnel.State.UP, wgConfig);

                SkStatus.updateStateString(SkStatus.WIREGUARD_CONECTADO, "WireGuard Connected");

            } catch (Exception e) {
                Log.e(TAG, "WireGuard connection error", e);
                SkStatus.logError("WireGuard Error: " + e.getMessage());
                disconnect();
            }
        }, "WireGuardThread");

        vpnThread.start();
    }

    private ParcelFileDescriptor buildVpn() throws Exception {
        Builder builder = new Builder();
        builder.setSession("WireGuard Tunnel: " + wgConfig.getPeers().get(0).getEndpoint());

        for (com.wireguard.config.InetNetwork address : wgConfig.getInterface().getAddresses()) {
            builder.addAddress(address.getAddress(), address.getMask());
        }

        for (Peer peer : wgConfig.getPeers()) {
            for (com.wireguard.config.InetNetwork allowedIp : peer.getAllowedIps()) {
                builder.addRoute(allowedIp.getAddress(), allowedIp.getMask());
            }
        }

        for (java.net.InetAddress dns : wgConfig.getInterface().getDnsServers()) {
            builder.addDnsServer(dns);
        }

        return builder.establish();
    }

    private void disconnect() {
        SkStatus.updateStateString(SkStatus.WIREGUARD_DESCONECTADO, "WireGuard Disconnected");
        Log.i(TAG, "Stopping WireGuard service.");

        if (vpnThread != null) {
            vpnThread.interrupt();
            vpnThread = null;
        }

        if (backend != null) {
            try {
                // === التصحيح هنا أيضاً ===
                backend.setState((Tunnel) backend, Tunnel.State.DOWN, null);
            } catch (Exception e) {
                Log.e(TAG, "Failed to set backend state to DOWN", e);
            }
        }

        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing VPN interface", e);
            }
            vpnInterface = null;
        }

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }
}