package com.fdx.injector.coreservice.core;

import android.content.SharedPreferences;
import android.util.Log;

import com.fdx.injector.MainApp;
import com.fdx.injector.coreservice.config.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Locale;

import de.blinkt.openvpn.core.VpnStatus;

public class MainThread extends Thread {
	Socket incoming;
	Socket outgoing;
	private boolean clientToServer;

	private SharedPreferences sp;

	public MainThread(Socket socket, Socket socket2, boolean z) {
		incoming = socket;
		outgoing = socket2;
		this.clientToServer = z;
		sp = MainApp.sp;
	}

	public final void run() {
		try {
			byte[] buffer;
			if (clientToServer) {
				buffer = new byte[Integer.parseInt(sp.getString("buffer_send", "16384"))];
			} else {
				buffer = new byte[Integer.parseInt(sp.getString("buffer_receive", "32768"))];
			}
			InputStream FromClient = this.incoming.getInputStream();
			OutputStream ToClient = this.outgoing.getOutputStream();
			while (true) {
				int numberRead = FromClient.read(buffer);
				if (numberRead == -1) {
					break;
				}
				String result = new String(buffer, 0, numberRead);
				if (this.clientToServer) {
					ToClient.write(buffer, 0, numberRead);
					ToClient.flush();
				} else {
					String[] split = result.split("\r\n");
					if (split[0].toLowerCase(Locale.getDefault()).startsWith("http")) {
						result = split[0].substring(9, 12);
						if (!sp.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
							VpnStatus.logWarning(split[0]);
						}	
						if (result.indexOf("200") >= 0) {
							ToClient.write(buffer, 0, numberRead);
							ToClient.flush();
						} else if (sp.getBoolean(Settings.AUTO_REPLACE, false)) {
						//	addLog("set auto replace response");
							VpnStatus.logWarning("auto replace");
							if (split[0].split(" ")[0].equals("HTTP/1.1")) {
							//	addLog("HTTP/1.1 200 OK");
								VpnStatus.logWarning("HTTP/1.1 200 OK");
								ToClient.write(new StringBuilder(String.valueOf(split[0].split(" ")[0]))
										.append(" 200 OK\r\n\r\n").toString().getBytes());
							} else {
								try {
									addLog("<b>Status: 200 (Connection established) Successful</b>"
											+ " - The action requested by the client was" + " successful.");
									ToClient.write(new StringBuilder(String.valueOf(split[0].split(" ")[0]))
											.append(" 200 Connection established\r\n\r\n").toString().getBytes());
								} catch (Exception e) {
									/*if (!e.toString().contains("Socket closed")) {
										addLog("socket closed");
									}*/
									addLog("1 :" + e.getMessage());
									
									try {
										if (this.incoming != null) {
											this.incoming.close();
										}
										if (this.outgoing != null) {
											this.outgoing.close();
											return;
										}
										return;
									} catch (IOException e2) {
										addLog("2 :" + e2.getMessage());
										return;
									}
								} catch (Throwable th) {
									addLog("3 :" + th.getMessage());
									try {
										if (this.incoming != null) {
											this.incoming.close();
										}
										if (this.outgoing != null) {
											this.outgoing.close();
										}
									} catch (IOException e3) {
										addLog("4 :" + e3.getMessage());
									}
								}
							}
							ToClient.flush();
						} else {
							ToClient.write(buffer, 0, numberRead);
							ToClient.flush();
						}
					} else {
						ToClient.write(buffer, 0, numberRead);
						ToClient.flush();
					}
				}
			}
			FromClient.close();
			ToClient.close();
		} catch (Exception e) {
			addLog("5 :" + e.getMessage());
			VpnStatus.logWarning("try enable auto replace");
			try {
				if (this.incoming != null) {
					this.incoming.close();
				}
				if (this.outgoing != null) {
					this.outgoing.close();
				}
			} catch (IOException e4) {
				addLog("6 :" + e4.getMessage());
			}
		}
	}

	void addLog(String str) {
		Log.i("MainThread", str);
	}
}
