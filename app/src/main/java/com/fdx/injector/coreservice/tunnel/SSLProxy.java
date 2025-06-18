package com.fdx.injector.coreservice.tunnel;


import com.fdx.injector.MainActivity;
import com.fdx.injector.MainApp;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.coreservice.logger.SkStatus;
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
import javax.net.ssl.X509TrustManager;

import de.blinkt.openvpn.core.VpnStatus;

public class SSLProxy implements ProxyData {

	public static Socket mSocket;

	@Override
	public void close() {
		if (mSocket == null)
			return;

		try {
			mSocket.close();
		} catch (IOException e) {
			/* failed */
		}
	}

	public static void Killer() {
		if (mSocket == null)
			return;
		try {
			mSocket.close();
		} catch (IOException e) {
		}
	}

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
			SkStatus.logDebug(new StringBuffer().append("Enable protocols: ")
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

	private String proxyPass;
	private String proxyUser;
	private String requestPayload;
	private String stunnelServer;
	private int stunnelPort = 443;
	private String stunnelHostSNI;

	public SSLProxy(String server, int port, String hostSni, String requestPayload) {
		this.stunnelServer = server;
		this.stunnelPort = port;
		this.stunnelHostSNI = hostSni;
		this.proxyUser = null;
		this.proxyPass = null;
		this.requestPayload = requestPayload;
	}

	@Override
	public Socket openConnection(String hostname, int port, int connectTimeout, int readTimeout) throws IOException {

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
				mSocket = doSSLHandshake(hostname, stunnelHostSNI, port);
				mSocket.setKeepAlive(true);
				mSocket.setTcpNoDelay(true);
				VpnStatus.logWarning("SSL KEEP ALIVE: " + mSocket.getKeepAlive());
				VpnStatus.logWarning("SSL TCP DELAY: " + mSocket.getTcpNoDelay());
				String requestPayload = getRequestPayload(hostname, port);
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

				byte[] buffer = new byte[1024];
				InputStream in = mSocket.getInputStream();
				int len = ClientServerHello.readLineRN(in, buffer);

				String httpReponseFirstLine = "";
				try {
					httpReponseFirstLine = new String(buffer, 0, len, "ISO-8859-1");
				} catch (UnsupportedEncodingException e3) {
					httpReponseFirstLine = new String(buffer, 0, len);
				}

				VpnStatus.logWarning("<strong>" + httpReponseFirstLine + "</strong>");

				String str2 = httpReponseFirstLine;
				int parseInt = Integer.parseInt(str2.substring(9, 12));

				if (parseInt == 200) {
					return this.mSocket;
				} else {
					if (MainApp.sp.getBoolean(Settings.AUTO_REPLACE, false)) {
						String asw = String.valueOf(parseInt);
						String replacw = httpReponseFirstLine.replace(httpReponseFirstLine, "HTTP/1.1 200 OK");
						VpnStatus.logWarning("auto replace");
						StringBuilder su = new StringBuilder();
						su.append("<strong>");
						su.append(replacw);
						su.append("</strong>");
						parseInt = Integer.parseInt(asw.replace(asw, "200"));
						VpnStatus.logWarning("HTTP/1.1 200 OK");
						return this.mSocket;
					}
				}

				if (httpReponseFirstLine.startsWith("HTTP/") == false)
					throw new IOException("The proxy did not send back a valid HTTP response.");
				if (httpReponseFirstLine.length() < 14)
					throw new IOException("The proxy did not send back a valid HTTP response.");
				if (httpReponseFirstLine.charAt(8) != ' ')
					throw new IOException("The proxy did not send back a valid HTTP response.");
				if (httpReponseFirstLine.charAt(12) != ' ')
					throw new IOException("The proxy did not send back a valid HTTP response.");
				if (len < 0 || len > 999) {
					throw new IOException("The proxy did not send back a valid HTTP response.");
				} else if (len != 200) {
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
			sb.append(" HTTP/1.0\r\n");
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
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
		try {
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
			KeyManager[] keyManagerArr = null;
			sSLContext.init(keyManagerArr, trustAllCerts, new SecureRandom());
			TLSSocketFactory tsf = new TLSSocketFactory();
			SSLSocket socket = (SSLSocket) tsf.createSocket(host, port);
			try {
				socket.getClass().getMethod("setHostname", String.class).invoke(socket, sni);
			} catch (Throwable e) {
			}
			socket.addHandshakeCompletedListener(new HandshakeTunnelCompletedListener(host, port, socket));
			socket.startHandshake();
			return socket;
		} catch (Exception e) {
			IOException iOException = new IOException(
					new StringBuffer().append("Could not do SSL handshake: ").append(e).toString());
			throw iOException;
		}
	}
}
