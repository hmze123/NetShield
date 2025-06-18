package com.fdx.injector.coreservice.tunnel.vpn;

import android.app.Service;

public interface V2Listener {
    boolean onProtect(final int socket);
    Service getService();
    void startService();
    void stopService();
    void onConnected();
    void onError();

}
