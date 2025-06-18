package com.fdx.injector.coreservice.tunnel;

public interface UDPListener {
    void onConnecting();
    void onConnected();
    void onNetworkLost();
    void onAuthFailed();
    void onReconnecting();
    void onConnectionLost();
    void onError();
    void onDisconnected();
}
