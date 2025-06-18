package com.fdx.injector.coreservice.core;

import android.annotation.*;
import android.content.*;
import android.preference.PreferenceManager;
import android.text.*;

import com.fdx.injector.MainActivity;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class TLSSocketFactory extends SSLSocketFactory {
    private SSLSocketFactory internalSSLSocketFactory;

    private SharedPreferences sp;
    private Context ctx;
    public SSLContext sslctx;

    public TLSSocketFactory(InputStream certStream)
            throws KeyManagementException, NoSuchAlgorithmException, IOException,
                    CertificateException, KeyStoreException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = new BufferedInputStream(certStream);
        try {
            Certificate ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext sslctx = SSLContext.getInstance("TLS");
            sslctx.init(null, tmf.getTrustManagers(), null);
            this.internalSSLSocketFactory = sslctx.getSocketFactory();
        } finally {
            caInput.close();
        }
    }

    public TLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        // For easier debugging purpose, trust all certificates
        sp = MainActivity.sShared;
        TrustManager[] trustAllCerts =
                new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @SuppressLint({"TrustAllX509TrustManager"})
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                        @SuppressLint({"TrustAllX509TrustManager"})
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
                };
        // SSLContext protocols: TLS, SSL, SSLv3
        // SSLContext sc = SSLContext.getInstance("SSLv3");
        // System.out.println("\nSSLContext class: "+sc.getClass());
        // System.out.println("   Protocol: "+sc.getProtocol());
        // System.out.println("   Provider: "+sc.getProvider());

        sslctx = SSLContext.getInstance("TLS");
        sslctx.init(null, trustAllCerts, new SecureRandom());
        internalSSLSocketFactory = sslctx.getSocketFactory();
    }

    public String[] getDefaultCipherSuites() {
        return this.internalSSLSocketFactory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return this.internalSSLSocketFactory.getSupportedCipherSuites();
    }

    public Socket createSocket() throws IOException {
        return enableTLSOnSocket(this.internalSSLSocketFactory.createSocket());
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException {
        return enableTLSOnSocket(
                this.internalSSLSocketFactory.createSocket(s, host, port, autoClose));
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return enableTLSOnSocket(this.internalSSLSocketFactory.createSocket(host, port));
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return enableTLSOnSocket(
                this.internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLSOnSocket(this.internalSSLSocketFactory.createSocket(host, port));
    }

    public Socket createSocket(
            InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
        return enableTLSOnSocket(
                this.internalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    private Socket enableTLSOnSocket(Socket socket) {
        if (socket instanceof SSLSocket) {
            // ((SSLSocket) socket).setEnabledProtocols(((SSLSocket)
            // socket).getSupportedProtocols());
            ((SSLSocket) socket)
                    .setEnabledProtocols(
                            new String[] {sp.getString("tls_version_min_override", "TLSv1")});
        }
        return socket;
    }
}
