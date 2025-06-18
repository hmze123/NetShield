package com.fdx.injector.coreservice.core;

import android.content.SharedPreferences;
import android.util.Log;

import com.fdx.injector.MainApp;
import com.fdx.injector.coreservice.config.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class CHZSSL {
	private Socket input;
	public SSLSocket bc;
	public Socket socket;
	private SharedPreferences sp;

	public CHZSSL(Socket in) {
		input = in;
		sp = MainApp.sp;
	}

	public Socket socket() {
		try {
			String readRequestHeader = readRequestHeader();
			if (readRequestHeader == null || !readRequestHeader.contains(":")) {
				//addLog(new StringBuffer().append("Invalid request: ").append(readRequestHeader).toString());
				return (Socket) null;
			}
			String host = readRequestHeader.split(":")[0];
			int port = Integer.parseInt(readRequestHeader.split(":")[1]);
			sendForwardSuccess(input);
			socket = SocketChannel.open().socket();
			socket.connect(new InetSocketAddress(host, port));

			//doVpnProtect(socket);
			//socket = doSSLHandshake(socket, utils.getPayload(), 443);
			if (socket.isConnected()) {
				//socket = newSocket(http.getPayload(), http.getSSHHost(), Integer.parseInt(http.getSSHPort()));
				socket = doSSLHandshake(sp.getString(Settings.OVPN_HOST, ""), sp.getString(Settings.CUSTOM_SNI, ""),
						Integer.parseInt(sp.getString(Settings.OVPN_PORT, "")));
			}
			return socket;
		} catch (Exception e) {
			return null;
		}
	}

	private Socket doTLSHandshake(String sni, String host, int port) {
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
					//this.listener.onConnectionCompleted();
				}
				return sslSocket;
			}
		} catch (Exception e) {
			//addLog(e.getMessage());
			return null;
		}
		return null;
	}

	private String readRequestHeader() throws IOException {
		Reader reader = new InputStreamReader(input.getInputStream());
		BufferedReader lineReader = new BufferedReader(reader);
		String request = null;
		String line;
		while ((line = lineReader.readLine()) != null) {
			if (line.startsWith("CONNECT") && request == null) {
				request = line.split(" ")[1];
			}
			if (line.length() == 0) {
				return request;
			}
		}
		return request;
	}

	private void sendForwardSuccess(Socket socket) throws IOException {
		String respond = "HTTP/1.1 200 OK\r\n\r\n";
		socket.getOutputStream().write(respond.getBytes());
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
				// addLog(new StringBuffer().append("<b>Established ").append(handshakeCompletedEvent.getSession().getProtocol()).append(" connection with ").append(val$host).append(":").append(this.val$port).append(" using ").append(handshakeCompletedEvent.getCipherSuite()).append("</b>").toString());
				//addLog(new StringBuffer().append("<b>Established ").append(handshakeCompletedEvent.getSession().getProtocol()).append(" connection ").append("using ").append(handshakeCompletedEvent.getCipherSuite()).append("</b>").toString());
				//addLog(new StringBuffer().append("Supported cipher suites: ").append(Arrays.toString(this.val$sslSocket.getSupportedCipherSuites())).toString());
				//addLog(new StringBuffer().append("Enabled cipher suites: ").append(Arrays.toString(this.val$sslSocket.getEnabledCipherSuites())).toString());
				addLog(new StringBuffer().append("Supported protocols <br>")
						.append(Arrays.toString(val$sslSocket.getSupportedProtocols())).toString().replace("[", "")
						.replace("]", "").replace(",", "<br>"));
				//addLog(new StringBuffer().append("SSL: Enabled protocols: <br>").append(Arrays.toString(val$sslSocket.getEnabledProtocols())).toString().replace("[", "").replace("]", "").replace(",", "<br>"));
				addLog("Using cipher " + handshakeCompletedEvent.getSession().getCipherSuite());
				addLog("Using protocol " + handshakeCompletedEvent.getSession().getProtocol());
				//addLog("" + handshakeCompletedEvent.getPeerPrincipal().toString());
				addLog("Handshake finished");
			} catch (Exception e) {

			}
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

	void addLog(String str) {
		Log.i("CHZSSL", str);
	}

}
