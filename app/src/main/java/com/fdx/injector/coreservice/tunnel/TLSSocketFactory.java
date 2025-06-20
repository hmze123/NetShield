package com.fdx.injector.coreservice.tunnel;

import android.annotation.SuppressLint;
import android.content.Context;


import com.fdx.injector.MainActivity;
import com.fdx.injector.coreservice.config.Settings;
import org.conscrypt.*;

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
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class TLSSocketFactory extends SSLSocketFactory {
    private SSLSocketFactory internalSSLSocketFactory;
    private Context ctx;

    static {
        try {
            Security.insertProviderAt(Conscrypt.newProvider(), 1);

        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }
    }

    public TLSSocketFactory(InputStream certStream)
            throws KeyManagementException, NoSuchAlgorithmException, IOException,
                    CertificateException, KeyStoreException {
        CertificateFactory cf = CertificateFactory.getInstance("Conscrypt");
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
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustAllCerts, new SecureRandom());
        internalSSLSocketFactory = context.getSocketFactory();
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
        int modeS = MainActivity.sShared.getInt(Settings.SSL_MODE, 0);
        if (socket != null && (socket instanceof SSLSocket)) {
            if (modeS == 0) {
                ((SSLSocket) socket)
                        .setEnabledProtocols(
                                new String[] { "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"});
            } else if (modeS == 2) {
                ((SSLSocket) socket).setEnabledProtocols(new String[] {"TLSv1"});
            } else if (modeS == 3) {
                ((SSLSocket) socket).setEnabledProtocols(new String[] {"TLSv1.1"});
            } else if (modeS == 4) {
                ((SSLSocket) socket).setEnabledProtocols(new String[] {"TLSv1.2"});
            } else if (modeS == 5) {
                ((SSLSocket) socket).setEnabledProtocols(new String[] {"TLSv1.3"});
            }
        }
        return socket;
    }
}
