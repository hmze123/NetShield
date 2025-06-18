package com.fdx.injector.coreservice.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.ArrayMap;
import android.util.Log;


import com.fdx.injector.MainApp;
import com.fdx.injector.coreservice.core.ResponseHeader;
import com.fdx.injector.util.Constant;
import com.trilead.ssh2.HTTPProxyException;
import com.fdx.injector.coreservice.config.Settings;
import com.trilead.ssh2.transport.ClientServerHello;

import com.trilead.ssh2.ProxyData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CHZSupport implements ProxyData {

	public static Map<String, CharSequence> BBCODES_LIST;
	Context cx;
	private final Socket input;
	public Socket socket;
	private static final Map<Integer, Integer> lastRotateList = new ArrayMap();
	private static String lastPayload = "";

	private static SharedPreferences sp;
	private HttpsURLConnection u;
	public SSLSocket sslSocket;
	private String sshHost = "";
	private String sshPort = "";
	private String payLoad = "";
	private String sSNI = "";

	public CHZSupport(Socket socket) {
		this.input = socket;
		sp = MainApp.sp;
	}

	public void close() {
	}

	public Socket openConnection(String str, int i, int i2, int i3) throws IOException {
		return null;
	}

	void addLog(String str) {
		Log.i("CHZSupport", str);
	}

	public static void restartRotateAndRandom() {
		lastRotateList.clear();
	}

	public Socket newSocket(String sni, String host, int port) {
		try {
			TLSSocketFactory d2 = new TLSSocketFactory();
			if (sni.isEmpty()) {
				((SSLSocket) d2.createSocket(host, port)).startHandshake();
			} else {
				URL uRL = new URL("https://" + sni);
				String string4 = uRL.getHost();
				if (uRL.getPort() > 0) {
					string4 = string4 + ":" + uRL.getPort();
				}
				if (!uRL.getPath().equals((Object) "/")) {
					string4 = string4 + uRL.getPath();
				}
				/*if (kpn.soft.dev.kpnrevolution.c.h.M() || kpn.soft.dev.kpnrevolution.c.h.L()) {
					string4 = j.a(string4);
				}*/
				u = /*true ? (HttpsURLConnection)uRL.openConnection(new Proxy(Proxy.Type.HTTP, this.v.a())) :*/
						(HttpsURLConnection) uRL.openConnection();
				this.u.setHostnameVerifier(new HostnameVerifier() {
					@SuppressLint(value = { "BadHostnameVerifier" })
					public boolean verify(String string, SSLSession sSLSession) {
						return true;
					}
				});
				this.u.setSSLSocketFactory((SSLSocketFactory) d2);
				this.u.connect();
			}
			return sslSocket;
		} catch (Exception e) {
			// addLog(new StringBuffer().append("Exception in proxy thread:
			// ").append(e.getMessage()).toString());
			return null;
		}
	}

	public Socket startSocket() {
		try {
			Socket socket = SocketChannel.open().socket();
			socket.connect(new InetSocketAddress(sshHost, Integer.valueOf(sshPort)), 10000);
			if (socket.isConnected()) {
				socket = doTLSHandshake(socket, payLoad, sshHost, Integer.valueOf(sshPort));
			}
			return socket;
		} catch (Exception e) {
			return null;
		}
	}

	private Socket doTLSHandshake(Socket socket, String sni, String host, int port) {
		SSLSocket sslSocket;
		try {
			TLSSocketFactory factory = new TLSSocketFactory();
			sslSocket = (SSLSocket) factory.createSocket(socket, sni, port, true);
			sslSocket.getClass().getMethod("setHostname", new Class[] { String.class }).invoke(sslSocket,
					new Object[] { sni });
			sslSocket.startHandshake();
			sslSocket.getOutputStream().write(String
					.format("CONNECT %s:%s HTTP/1.1\r\nHost: %s\r\n\r\n", new Object[] { host, port, sni }).getBytes());
			InputStream inStream = sslSocket.getInputStream();
			ResponseHeader resHeader = new HTTPHeaderReader(inStream).read();
			if (resHeader.getBodyLength() <= 0 || inStream.read(new byte[resHeader.getBodyLength()]) != -1) {
				if (resHeader.getStatusText().toLowerCase().equals("connection established")) {
					// this.listener.onConnectionCompleted();
				}
				return sslSocket;
			}
		} catch (Exception e) {
			// addLog(e.getMessage());
			return null;
		}
		return null;
	}

	public static String parseRotate(String str) {
		int i;
		Matcher matcher = Pattern.compile("\\[rotate=(.*?)\\]").matcher(str);
		if (!lastPayload.equals(str)) {
			restartRotateAndRandom();
			lastPayload = str;
		}
		int i2 = 0;
		while (matcher.find()) {
			String[] split = matcher.group(1).split(";");
			if (split.length > 0) {
				Map<Integer, Integer> map = lastRotateList;
				if (!map.containsKey(Integer.valueOf(i2))
						|| (i = map.get(Integer.valueOf(i2)).intValue() + 1) >= split.length) {
					i = 0;
				}
				str = str.replace(matcher.group(0), split[i]);
				map.put(Integer.valueOf(i2), Integer.valueOf(i));
				i2++;
			}
		}
		return str;
	}

	public static String parseRandom(String str) {
		Matcher matcher = Pattern.compile("\\[random=(.*?)\\]").matcher(str);
		while (matcher.find()) {
			String[] split = matcher.group(1).split(";");
			if (split.length > 0) {
				int nextInt = new Random().nextInt(split.length);
				if (nextInt >= split.length || nextInt < 0) {
					nextInt = 0;
				}
				str = str.replace(matcher.group(0), split[nextInt]);
			}
		}
		return str;
	}

	public static String formatCustomPayload(String str, int i, String str2) {
		ArrayMap arrayMap = new ArrayMap();
		BBCODES_LIST = arrayMap;
		arrayMap.put("[method]", "CONNECT");
		BBCODES_LIST.put("[host]", str);
		BBCODES_LIST.put("[port]", Integer.toString(i));
		BBCODES_LIST.put("[host_port]", String.format("%s:%d", str, Integer.valueOf(i)));
		BBCODES_LIST.put("[protocol]", "HTTP/1.0");
		BBCODES_LIST.put("[ssh]", String.format("%s:%d", str, Integer.valueOf(i)));
		BBCODES_LIST.put("[crlf]", "\r\n");
		BBCODES_LIST.put("[cr]", "\r");
		BBCODES_LIST.put("[lf]", "\n");
		BBCODES_LIST.put("[lfcr]", "\n\r");
		BBCODES_LIST.put("\\n", "\n");
		BBCODES_LIST.put("\\r", "\r");
		String property = System.getProperty("http.agent");
		Map<String, CharSequence> map = BBCODES_LIST;
		if (property == null) {
			property = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko)"
					+ " Chrome/44.0.2403.130 Safari/537.36";
		}
		map.put("[ua]", property);
		if (str2.isEmpty()) {
			return str2;
		}
		for (String str3 : BBCODES_LIST.keySet()) {
			String lowerCase = str3.toLowerCase();
			str2 = str2.replace(lowerCase, BBCODES_LIST.get(lowerCase));
		}
		return parseRandom(parseRotate(str2));
	}

	private String getRequestPayload(String str, int i) {
		/* String payload1 =
		        "GET ws://v27.tiktokcdn.com HTTP/1.1[crlf]Host:"
		                + " sgdo01-07-openvpn.darktunnel.net[crlf]Service: OpenVPN[crlf]Upgrade:"
		                + " websocket[crlf][crlf]";*/
		String payload1 = sp.getString(Settings.CUSTOM_PAYLOAD_KEY, "");
		if (payload1 != null) {
			return formatCustomPayload(str, i, payload1);
		}
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("CONNECT ");
		stringBuffer.append(str);
		stringBuffer.append(':');
		stringBuffer.append(i);
		stringBuffer.append(" HTTP/1.0\r\n");
		stringBuffer.append("\r\n");
		return stringBuffer.toString();
	}

	private static boolean injectSimpleSplit(String str, OutputStream outputStream) throws IOException {
		if (!str.contains("[split]")) {
			return false;
		}
		String[] split = str.split(Pattern.quote("[split]"));
		for (int i = 0; i < split.length; i++) {
			try {
				outputStream.write(split[i].getBytes(StandardCharsets.ISO_8859_1));
			} catch (UnsupportedEncodingException unused) {
				outputStream.write(split[i].getBytes());
			}
			outputStream.flush();
		}
		return true;
	}

	public static boolean injectSplitPayload(String str, OutputStream outputStream) throws IOException {
		if (!str.contains("[delay_split]")) {
			return injectSimpleSplit(str, outputStream);
		}
		String[] split = str.split(Pattern.quote("[delay_split]"));
		for (int i = 0; i < split.length; i++) {
			String str2 = split[i];
			if (!injectSimpleSplit(str2, outputStream)) {
				try {
					outputStream.write(str2.getBytes(StandardCharsets.ISO_8859_1));
				} catch (UnsupportedEncodingException unused) {
					outputStream.write(str2.getBytes());
				}
				outputStream.flush();
			}
			try {
				if (i != split.length - 1) {
					Thread.sleep(1000L);
				}
			} catch (InterruptedException unused2) {
			}
		}
		return true;
	}

	private String readRequestHeader() throws IOException {
		String readLine;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.input.getInputStream()));
		String str = null;
		do {
			readLine = bufferedReader.readLine();
			if (readLine == null) {
				break;
			} else if (readLine.startsWith("CONNECT") && str == null) {
				str = readLine.split(" ")[1];
			}
		} while (readLine.length() != 0);
		return str;
	}

	public Socket socket() {
		try {
			String readRequestHeader = readRequestHeader();
			if (readRequestHeader == null || !readRequestHeader.contains(":")) {
				addLog("Invalid request: " + readRequestHeader);
				return null;
			}
			/*  try {
			this.http.getProxyHost();
			try {
				this.http.getProxyPort();*/
			sendForwardSuccess(this.input);
			Socket socket = SocketChannel.open().socket();
			this.socket = socket;
			socket.connect(new InetSocketAddress(sp.getString(Settings.CUSTOM_SNI, ""),
					Integer.parseInt(sp.getString(Settings.PROXY_IP_PORT, "").split(":")[1])));
			String requestPayload = getRequestPayload(sp.getString(Settings.CUSTOM_SNI, ""),
					Integer.parseInt(sp.getString(Settings.PROXY_IP_PORT, "").split(":")[1]));

			if (!this.socket.isConnected()) {
				return this.socket;
			}
			
			this.socket = doSSLHandshake(sp.getString(Settings.CUSTOM_SNI, ""), sp.getString(Settings.CUSTOM_SNI, ""),
					Integer.parseInt(sp.getString(Settings.OVPN_PORT, "")));

			OutputStream outputStream = this.socket.getOutputStream();
			if (!injectSplitPayload(requestPayload, outputStream)) {
				try {
					outputStream.write(requestPayload.getBytes(StandardCharsets.ISO_8859_1));
				} catch (UnsupportedEncodingException unused) {
					outputStream.write(requestPayload.getBytes());
				}
				outputStream.flush();
			}
			byte[] bArr = new byte[1024];
			String str = new String(bArr, 0, ClientServerHello.readLineRN(this.socket.getInputStream(), bArr),
					StandardCharsets.ISO_8859_1);
			// addLog(str);
			int parseInt = Integer.parseInt(str.substring(9, 12));

			if (parseInt == 200) {
				return this.socket;
			}
			if (parseInt == 101) {
				return this.socket;
			} else if (!str.startsWith("HTTP/")) {
				throw new IOException("The proxy did not send back a valid HTTP response.");
			} else if (str.length() >= 14 && str.charAt(8) == ' ' && str.charAt(12) == ' ') {
				try {
					int parseInt2 = Integer.parseInt(str.substring(9, 12));
					if (parseInt2 < 0 || parseInt2 > 999) {
						throw new IOException("The proxy did not send back a valid HTTP response.");
					} else if (parseInt2 == 200) {
						return this.socket;
					} else {
						throw new HTTPProxyException(str.substring(13), parseInt2);
					}
				} catch (NumberFormatException unused2) {
					throw new IOException("The proxy did not send back a valid HTTP response.");
				}
			} else {
				throw new IOException("The proxy did not send back a valid HTTP response.");
			}
		} catch (Exception e) {
			addLog("Error Handshake: " + e.getMessage());
			return null;
		}
	}

	class mHandshakeCompletedListener implements HandshakeCompletedListener {
		private final String val$host;
		private final int val$port;
		private final SSLSocket val$sslSocket;

		mHandshakeCompletedListener(String str, int i, SSLSocket sSLSocket) {
			this.val$host = str;
			this.val$port = i;
			this.val$sslSocket = sSLSocket;
		}

		public void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent) {
			try {
				addLog(new StringBuffer().append("Supported protocols <br>")
						.append(Arrays.toString(val$sslSocket.getSupportedProtocols())).toString().replace("[", "")
						.replace("]", "").replace(",", "<br>"));
				addLog("Using cipher " + handshakeCompletedEvent.getSession().getCipherSuite());
				addLog("Using protocol " + handshakeCompletedEvent.getSession().getProtocol());
				addLog("Handshake finished");
			} catch (Exception e) {

			}
		}
	}

	private void sendForwardSuccess(Socket socket) throws IOException {
		socket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
		socket.getOutputStream().flush();
	}

	private SSLSocket doSSLHandshake(String str, String str2, int i) throws IOException {
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
			SSLContext sSLContext = SSLContext.getInstance("TLS");
			KeyManager[] keyManagerArr = (KeyManager[]) null;
			sSLContext.init(keyManagerArr, trustAllCerts, new SecureRandom());
			SSLSocket socket = (SSLSocket) sSLContext.getSocketFactory().createSocket(str, i);
			setSNIHost(sSLContext.getSocketFactory(), socket, str2);
			socket.setEnabledProtocols(socket.getEnabledProtocols());
			socket.addHandshakeCompletedListener(new mHandshakeCompletedListener(str, i, socket));
			socket.startHandshake();
			return socket;
		} catch (Exception e) {
			throw new IOException(" failed : " + e);
		}
	}

	private void setSNIHost(final SSLSocketFactory factory, final SSLSocket socket, final String hostname) {
		if (factory instanceof android.net.SSLCertificateSocketFactory
				&& android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			((android.net.SSLCertificateSocketFactory) factory).setHostname(socket, hostname);
		} else {
			try {
				socket.getClass().getMethod("setHostname", String.class).invoke(socket, hostname);
			} catch (Throwable e) {
				// ignore any error, we just can't set the hostname...
			}
		}
	}
}
