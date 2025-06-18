package com.fdx.injector.coreservice.tunnel;

import android.content.SharedPreferences;

import com.fdx.injector.MainActivity;
import com.fdx.injector.MainApp;
import com.fdx.injector.coreservice.config.Settings;
import com.trilead.ssh2.ProxyData;
import com.trilead.ssh2.crypto.Base64;
import com.trilead.ssh2.transport.ClientServerHello;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import de.blinkt.openvpn.core.VpnStatus;

public class SSLRemoteProxy implements ProxyData {

	public static void Killer() {
		try {
			mSocket.close();
		} catch (IOException e) {
		}
	}

	private TrustManager[] trustAllCerts;

	class HandshakeTunnelCompletedListener implements HandshakeCompletedListener {
		private final String val$host;
		private final int val$port;
		private final SSLSocket val$sslSocket;

		HandshakeTunnelCompletedListener(String str, int i, SSLSocket sSLSocket) {
			this.val$host = str;
			this.val$port = i;
			this.val$sslSocket = sSLSocket;
		}

		public void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent) {
			VpnStatus.logWarning(new StringBuffer().append("Enable protocols: ")
					.append(Arrays.toString(val$sslSocket.getSupportedProtocols())).toString());
			VpnStatus.logWarning("ChiperSuite: " + handshakeCompletedEvent.getSession().getCipherSuite());
			String mSLorTls = "";
			int modeS = MainActivity.sShared.getInt(Settings.SSL_MODE, 0);
			if (modeS == 0) {
				mSLorTls = "Auto";
			} else if (modeS == 6) {
				mSLorTls = "SSL";
			} else if (modeS == 1) {
				mSLorTls = "TLS";
			} else if (modeS == 7) {
				mSLorTls = "SSLv3";
			} else {
				mSLorTls = handshakeCompletedEvent.getSession().getProtocol();
			}
			VpnStatus.logWarning("Protocol: " + mSLorTls);
			VpnStatus.logWarning("Handshake finished");
		}
	}

	public static Socket mSocket;
	private String proxyPass;
	private String proxyUser;
	private String requestPayload;
	private SharedPreferences sp;
	private String stunnelHostSNI;
	private int stunnelPort = 443;
	private String stunnelServer;

	@Override
	public Socket openConnection(String str, int i, int i2, int i3) throws IOException {
		VpnStatus.logWarning("Starting SSL Handshake...");
		URL url = new URL("https://" + stunnelHostSNI);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		// conn.connect();
		conn.setConnectTimeout(3000);
		conn.setReadTimeout(3000);
		conn.setRequestMethod("GET");
		int code = conn.getResponseCode();
		String ressult = Integer.toString(code);
		conn.connect();
		Certificate[] certs = conn.getServerCertificates();
		X509Certificate xnxx = (X509Certificate) certs[0];
		VpnStatus.logWarning("Principal: " + xnxx.getIssuerDN().toString());

		if (!ressult.isEmpty()) {
			mSocket = SocketChannel.open().socket();
			mSocket.connect(new InetSocketAddress(stunnelServer, stunnelPort), 3000);

			if (mSocket.isConnected()) {
				conn.disconnect();
				mSocket = doSSLHandshake(str, stunnelHostSNI, i);
				mSocket.setKeepAlive(true);
				mSocket.setTcpNoDelay(true);
				VpnStatus.logWarning("SSL KEEP ALIVE: " + mSocket.getKeepAlive());
				VpnStatus.logWarning("SSL TCP DELAY: " + mSocket.getTcpNoDelay());

				String requestPayload = getRequestPayload(str, i);
				OutputStream out = mSocket.getOutputStream();

				if (!TunnelUtils.injectSplitPayload(requestPayload, out)) {
					try {
						out.write(requestPayload.getBytes("ISO-8859-1"));
					} catch (UnsupportedEncodingException e2) {
						out.write(requestPayload.getBytes());
					}
					out.flush();
				}
				
				if (!MainApp.sp.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
					VpnStatus.logWarning(requestPayload);
				}
				

				byte[] bArr = new byte[1024];
				InputStream inputStream = this.mSocket.getInputStream();
				int readLineRN = ClientServerHello.readLineRN(inputStream, bArr);
				
				requestPayload = "";
				try {
					requestPayload = new String(bArr, 0, readLineRN, "ISO-8859-1");
				} catch (UnsupportedEncodingException e2) {
					requestPayload = new String(bArr, 0, readLineRN);
				}

				String httpReponseFirstLine = "";
				try {
					httpReponseFirstLine = new String(bArr, 0, readLineRN, "ISO-8859-1");
				} catch (UnsupportedEncodingException e3) {
					httpReponseFirstLine = new String(bArr, 0, readLineRN);
				}

				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("<strong>");
				stringBuilder.append(requestPayload);
				stringBuilder.append("</strong>");
				VpnStatus.logWarning(stringBuilder.toString());
				readLineRN = Integer.parseInt(requestPayload.substring(9, 12));

				if (readLineRN == 200) {
					return this.mSocket;
				} else {
					if (MainApp.sp.getBoolean(Settings.AUTO_REPLACE, false)) {
						String asw = String.valueOf(readLineRN);
						String replacw = httpReponseFirstLine.replace(httpReponseFirstLine, "HTTP/1.1 200 OK");
						VpnStatus.logWarning("auto replace header");
						StringBuilder su = new StringBuilder();
						su.append("<strong>");
						su.append(replacw);
						su.append("</strong>");
						readLineRN = Integer.parseInt(asw.replace(asw, "200"));
						VpnStatus.logWarning("HTTP/1.1 200 OK");
						return this.mSocket;
					}
				}

				int readLineRN2;
				String valueOf = String.valueOf(readLineRN);
				String replace = requestPayload.replace(requestPayload, "HTTP/1.1 200 Ok");
				VpnStatus.logWarning("auto replace");
				StringBuilder stringBuilder2 = new StringBuilder();
				stringBuilder2.append("<strong>");
				stringBuilder2.append(replace);
				stringBuilder2.append("</strong>");
				Integer.parseInt(valueOf.replace(valueOf, "200"));
				valueOf = requestPayload;
				while (true) {
					readLineRN2 = ClientServerHello.readLineRN(inputStream, bArr);
					if (readLineRN2 == 0) {
						break;
					}
					valueOf = new StringBuffer().append(valueOf).append("\n").toString();
					try {
						valueOf = new StringBuffer().append(valueOf)
								.append(new String(bArr, 0, readLineRN2, "ISO-8859-1")).toString();
					} catch (UnsupportedEncodingException e3) {
						valueOf = new StringBuffer().append(valueOf).append(new String(bArr, 0, readLineRN2))
								.toString();
					}
				}
				if (!valueOf.isEmpty()) {
					VpnStatus.logWarning(valueOf);
				}

				if (httpReponseFirstLine.startsWith("HTTP/") == false)
					throw new IOException("The proxy did not send back a valid HTTP response.");
				if (httpReponseFirstLine.length() < 14)
					throw new IOException("The proxy did not send back a valid HTTP response.");
				if (httpReponseFirstLine.charAt(8) != ' ')
					throw new IOException("The proxy did not send back a valid HTTP response.");
				if (httpReponseFirstLine.charAt(12) != ' ')
					throw new IOException("The proxy did not send back a valid HTTP response.");
				if (readLineRN < 0 || readLineRN > 999) {
					throw new IOException("The proxy did not send back a valid HTTP response.");
				} else if (readLineRN != 200) {
					String stringBuffer = new StringBuffer().append(
							new StringBuffer().append("HTTP/1.0 200 Connection established").append("\r\n").toString())
							.append("\r\n").toString();
					out.write(stringBuffer.getBytes());
					out.flush();
					VpnStatus.logWarning("try to enable auto replace");
					return mSocket;
				} else {
					return mSocket;
				}

			}
			return mSocket;
		}
		return mSocket;
	}

	private String getRequestPayload(String hostname, int port) {
		String payload = this.requestPayload;
		if (payload != null) {
			payload = TunnelUtils.formatCustomPayload(hostname, port, payload);
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append("CONNECT ");
			sb.append(hostname);
			sb.append(':');
			sb.append(port);
			sb.append(" HTTP/1.1\r\n");
			if (!(this.proxyUser == null || this.proxyPass == null)) {
				char[] encoded;
				String credentials = this.proxyUser + ":" + this.proxyPass;
				try {
					encoded = Base64.encode(credentials.getBytes("ISO-8859-1"));
				} catch (UnsupportedEncodingException e) {
					encoded = Base64.encode(credentials.getBytes());
				}
				sb.append("Proxy-Authorization: Basic ");
				sb.append(encoded);
				sb.append("\r\n");
			}
			sb.append("\r\n");
			payload = sb.toString();
		}
		return payload;
	}

	private SSLSocket doSSLHandshake(String host, String sni, int port) throws IOException {
		try {
			boolean secureRandom = true;

			String mSLorTls = "";
			int modeS = MainActivity.sShared.getInt(Settings.SSL_MODE, 0);
			if (modeS == 6) {
				mSLorTls = "SSL";
			} else if (modeS == 1) {
				mSLorTls = "TLS";
			} else if (modeS == 7) {
				mSLorTls = "SSLv3";
			} else {
				mSLorTls = "TLS";
			}
			SSLContext sSLContext = SSLContext.getInstance(mSLorTls);
			KeyManager[] keyManagerArr = (KeyManager[]) null;
			sSLContext.init(keyManagerArr, trustAllCerts, secureRandom ? new SecureRandom() : null);
			SSLSocket socket = (SSLSocket) sSLContext.getSocketFactory().createSocket(host, port);
			// TLSSocketFactory tsf = new TLSSocketFactory();
			// SSLSocket socket = (SSLSocket) tsf.createSocket(host, port);
			try {
				socket.getClass().getMethod("setHostname", String.class).invoke(socket, sni);
				// VpnStatus.logWarning("Setting up SNI: "+sni);
			} catch (Throwable e) {
				// ignore any error, we just can't set the hostname...
			}
			socket.addHandshakeCompletedListener(new HandshakeTunnelCompletedListener(host, port, socket));
			// VpnStatus.logWarning("Starting SSL Handshake...");
			socket.startHandshake();
			return socket;
		} catch (Exception e) {
			IOException iOException = new IOException(
					new StringBuffer().append("Could not do SSL handshake: ").append(e).toString());
			throw iOException;
		}
	}

	public SSLRemoteProxy(String str, int i, String str2, String str3) {
		this.stunnelServer = str;
		this.stunnelPort = i;
		this.stunnelHostSNI = str2;
		this.proxyUser = (String) null;
		this.proxyPass = (String) null;
		this.requestPayload = str3;
		this.sp = MainActivity.sShared;
	}

	@Override
	public void close() {
		if (this.mSocket != null) {
			try {
				this.mSocket.close();
			} catch (IOException e) {
			}
		}
	}
}
