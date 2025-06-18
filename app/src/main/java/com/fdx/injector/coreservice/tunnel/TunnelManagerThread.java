package com.fdx.injector.coreservice.tunnel;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.fdx.injector.MainApp;
import com.fdx.injector.R;
import com.fdx.injector.coreservice.SocksHttpService;
import com.fdx.injector.coreservice.TrojanService;
import com.fdx.injector.coreservice.WireGuardService;
import com.fdx.injector.coreservice.config.PasswordCache;
import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.coreservice.logger.SkStatus;
import com.fdx.injector.coreservice.tunnel.vpn.TunnelState;
import com.fdx.injector.coreservice.tunnel.vpn.TunnelVpnManager;
import com.fdx.injector.coreservice.tunnel.vpn.TunnelVpnService;
import com.fdx.injector.coreservice.tunnel.vpn.TunnelVpnSettings;
import com.fdx.injector.coreservice.tunnel.vpn.V2Listener;
import com.fdx.injector.coreservice.tunnel.vpn.VpnUtils;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.ConnectionMonitor;
import com.trilead.ssh2.DebugLogger;
import com.trilead.ssh2.DynamicPortForwarder;
import com.trilead.ssh2.InteractiveCallback;
import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.ProxyData;
import com.trilead.ssh2.ServerHostKeyVerifier;
import com.trilead.ssh2.transport.TransportManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import de.blinkt.openvpn.core.VpnStatus;

public class TunnelManagerThread
		implements Runnable, ConnectionMonitor, InteractiveCallback, ServerHostKeyVerifier, DebugLogger {
	private static final String TAG = TunnelManagerThread.class.getSimpleName();
	private boolean iswireguardmode() {
		return prefs.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_TYPE_WIREGUARD;
	}

	private boolean isTrojanMode() {
		return prefs.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_TYPE_TROJAN;
	}
	private OnStopCliente mListener;
	private Context mContext;
	private Handler mHandler;
	private Settings mConfig;
	private boolean mRunning = false, mStopping = false, mStarting = false;
	private Pinger pinger;
	private static UDPListener udpListener;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback callback;
    private boolean runningatesccn = false;
    private UDPTunnel udpTunnel;
    private boolean v2rayrunning = false;
    private V2Tunnel v2Tunnel;
    private static V2Listener v2Listener;
    private SharedPreferences prefs;

    private CountDownLatch mTunnelThreadStopSignal;
    //private ConnectivityManager mCmgr;

    public interface OnStopCliente {
        void onStop();
    }

	public TunnelManagerThread(Handler handler, Context context) {
		mContext = context;
		mHandler = handler;
		mConfig = new Settings(context);
        prefs = mConfig.getPrefsPrivate();
        setCallback();
        udpListener = new UDPListener() {
            @Override
            public void onConnecting() {
                SkStatus.updateStateString(SkStatus.SSH_CONECTANDO, "Connecting");
            }
            @Override
            public void onConnected() {
                try {
                    SkStatus.updateStateString(SkStatus.SSH_CONECTADO, "SSH Connection Established");
                    VpnStatus.logWarning("<font color='green'><strong>" + mContext.getString(R.string.state_connected) + "</strong></font>");

                    mConnected = true;
                    startTunnelVpnService();
                } catch (Exception e)  {

                }
            }
            @Override
            public void onNetworkLost() {
            }
            @Override
            public void onAuthFailed() {
                stopAll();
            }
            @Override
            public void onReconnecting() {
                stopAll();
            }
            @Override
            public void onConnectionLost() {
                stopAll();
            }
            @Override
            public void onError() {
                stopAll();
            }
            @Override
            public void onDisconnected() {

            }
        };
        v2Listener = new V2Listener() {
            @Override
            public boolean onProtect(int socket) {
                return false;
            }

            @Override
            public Service getService() {
                return null;
            }

            @Override
            public void startService() {
                SkStatus.updateStateString(SkStatus.SSH_CONECTANDO, "Connecting");
            }

            @Override
            public void stopService() {
            }
            @Override
            public void onConnected() {
                try {
                    SkStatus.updateStateString(SkStatus.SSH_CONECTADO, "SSH Connection Established");
                    VpnStatus.logWarning("<font color='#2AFF0D'><strong>V2Ray Service Connected</strong></font>");
                    mConnected = false;
                    startTunnelVpnService();
                } catch (Exception ignored)  {

                }

            }

            @Override
            public void onError() {
                Intent stopTunnel = new Intent(SocksHttpService.TUNNEL_SSH_STOP_SERVICE);
                LocalBroadcastManager.getInstance(context).sendBroadcast(stopTunnel);

            }
        };
    }

    public void setOnStopClienteListener(OnStopCliente listener) {
        mListener = listener;
    }

    public static UDPListener getUDPListener() {
        return udpListener;
    }

    public static V2Listener getV2rayServicesListener() {
        return v2Listener;
    }
    
	@Override
    
    public void run()
	{
		
		
		mStarting = true;
		mTunnelThreadStopSignal = new CountDownLatch(1);
		
		VpnStatus.logWarning("<strong>" + mContext.getString(R.string.starting_service_ssh) + "</strong>");
		
		int tries = 0;
		while (!mStopping) {
			try {
				if (!TunnelUtils.isNetworkOnline(mContext)) {
					SkStatus.updateStateString(SkStatus.SSH_AGUARDANDO_REDE, mContext.getString(R.string.state_nonetwork));

					VpnStatus.logWarning(R.string.state_nonetwork);
					
					try {
						Thread.sleep(5000);
					} catch(InterruptedException e2) {
						stopAll();
						break;
					}
				}
				else {
					if (tries > 0)
						VpnStatus.logWarning("<strong>" + mContext.getString(R.string.state_reconnecting) + "</strong>");

					try {
						Thread.sleep(500);
					} catch(InterruptedException e2) {
						stopAll();
						break;
					}

					  if(isudpmode()){
                        if (udpTunnel == null) {
                            udpTunnel = new UDPTunnel(mContext);
                            udpTunnel.iniciarUdp();
                            runningatesccn = true;
                        }
                    } else if (isv2raymode()) {
                        if (v2Tunnel == null) {
                            v2Tunnel = new V2Tunnel(mContext);
                            V2Tunnel.StartV2ray(mContext.getApplicationContext(), "Default", mConfig.getPrivString(Settings.V2RAY_JSON), null);
                            v2rayrunning = true;
						}

					  } else if (iswireguardmode()) { // <<<====== السطر الجديد
						  // هنا نقوم بتشغيل خدمة WireGuard الجديدة
						  Intent wgIntent = new Intent(mContext, WireGuardService.class);
						  wgIntent.setAction(WireGuardService.ACTION_CONNECT);
						  mContext.startService(wgIntent);
					  } else if (isTrojanMode()) { // <<<==== أضف هذه الكتلة
						  Intent trojanIntent = new Intent(mContext, TrojanService.class);
						  mContext.startService(trojanIntent);
					  } else {
						  // الحالة الافتراضية هي SSH
						  startClienteSSH();
                    }
                    break;
				}
			} catch(Exception e) {

				SkStatus.logError("<strong>" + mContext.getString(R.string.state_disconnected) + "</strong>");
				closeSSH();
				
				try {
					Thread.sleep(500);
				} catch(InterruptedException e2) {
					stopAll();
					break;
				}
			}
			
			tries++;
			
			
		}
		
		mStarting = false;
		
		if (!mStopping) {
			try {
				mTunnelThreadStopSignal.await();
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		if (mListener != null) {
			mListener.onStop();
		}
		
	}
    
	/*public void run() {
		mStarting = true;
		mTunnelThreadStopSignal = new CountDownLatch(1);

		VpnStatus.logWarning("<strong>" + mContext.getString(R.string.starting_service_ssh) + "</strong>");

		int tries = 0;
		while (!mStopping) {
			try {
				if (!TunnelUtils.isNetworkOnline(mContext)) {
					SkStatus.updateStateString(SkStatus.SSH_AGUARDANDO_REDE,
							mContext.getString(R.string.state_nonetwork));

					VpnStatus.logWarning(R.string.state_nonetwork);

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e2) {
						stopAll();
						break;
					}
				} else {
					if (tries > 0)
						VpnStatus
								.logWarning("<strong>" + mContext.getString(R.string.state_reconnecting) + "</strong>");

					try {
						Thread.sleep(500);
					} catch (InterruptedException e2) {
						stopAll();
						break;
					}

					startClienteSSH();
					break;
				}
			} catch (Exception e) {

				SkStatus.logError("<strong><font color='red'>" + mContext.getString(R.string.state_disconnected)
						+ "</font></strong>");
				closeSSH();
				//    disconnectVibrate();
				//stopSocketss();
				/*             if (SocksHttpService.isInstanceCreated()
										&& mConfig.getPrefsPrivate().getBoolean(Settings.WAKELOCK_KEY,true)) {
				    SocksHttpService.wakeLock.release();
				    VpnStatus.logWarning("<strong>Wakelock Released</strong>");
				}*/
			/*	try {
					Thread.sleep(500);
				} catch (InterruptedException e2) {
					stopAll();
					break;
				}
			}

			tries++;
		}

		mStarting = false;

		if (!mStopping) {
			try {
				mTunnelThreadStopSignal.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		if (mListener != null) {
			mListener.onStop();
		}
	}*/

	public void stopSocketss() {
		if (SSLProxy.mSocket != null) {
			try {
				SSLProxy.mSocket.close();
			} catch (IOException m) {
			}
		}
		if (SSLRemoteProxy.mSocket != null) {
			try {
				SSLRemoteProxy.mSocket.close();
			} catch (IOException e) {
			}
		}
		if (SSLTunnelProxy.mSocket != null) {
			try {
				SSLTunnelProxy.mSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private void disconnectVibrate() {
		((Vibrator) this.mContext.getSystemService("vibrator")).vibrate(new long[] { 0, 100, 100, 100 }, -1);
	}

	public void stopAll() {
		if (mStopping)
			return;

		SkStatus.updateStateString(SkStatus.SSH_PARANDO, mContext.getString(R.string.stopping_service_ssh));
		VpnStatus.logWarning("<strong>" + mContext.getString(R.string.stopping_service_ssh) + "</strong>");

		new Thread(new Runnable() {
			@Override
			public void run() {
                mStopping = true;

                if (mTunnelThreadStopSignal != null)
                    mTunnelThreadStopSignal.countDown();

                if (isudpmode()) {
                    if (runningatesccn) {
                        udpTunnel.detenerUdp();
                        udpTunnel = null;
                        runningatesccn = false;
                        connectivityManager.unregisterNetworkCallback(callback);
                    }
                    if (mConnected) {
                        stopForwarder();
                    }
                    mRunning = false;
                    mStarting = false;
                    mReconnecting = false;
                } else if (isv2raymode()) {
                    if (v2rayrunning) {
                        V2Tunnel.StopV2ray(mContext.getApplicationContext());
                        v2Tunnel = null;
                        v2rayrunning= false;
                        connectivityManager.unregisterNetworkCallback(callback);
                    }
                    if (mConnected) {
                        stopForwarder();
                    }
                    mRunning = false;
                    mStarting = false;
                    mReconnecting = false;
                } else {
                    closeSSH();
                    mRunning = false;
                    mStarting = false;
                    mReconnecting = false;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                SkStatus.updateStateString(SkStatus.SSH_DESCONECTADO, mContext.getString(R.string.state_disconnected));
                    
                    if (mConfig.getWakelock()) {
					SocksHttpService.wakeLock.release();
					VpnStatus.logWarning("<strong>WakeLock Released</strong>");
				}
            }
        }).start();
    }

	/** Forwarder */
	protected void startForwarder(int portaLocal) throws Exception {
		if (!mConnected) {
			throw new Exception();
		}

		startForwarderSocks(portaLocal);
		startTunnelVpnService();

		/*    if (mConfig.network_meter()) {
		    mContext.startService(new Intent(mContext, nspeednotif.class));
		}*/

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (!mConnected)
						break;
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}).start();

		/* If connected, 1s delay before pinger started */
		String PING = mConfig.setPinger();

		if (mConfig.setAutoPing()) {

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			if (!PING.equals("")) {
				pinger = new Pinger(mConnection, PING);
				pinger.start();
			}
		}
	}

	private synchronized void interruptPinger() {
		if (pinger != null && pinger.isAlive()) {
			//  VpnStatus.logWarning("stopping pinger");

			pinger.interrupt();
		}
	}

	protected void stopForwarder() {
		stopTunnelVpnService();

		stopForwarderSocks();
	}

	/** Cliente SSH */
	private static final int AUTH_TRIES = 1;

	private static final int RECONNECT_TRIES = 5;

	private Connection mConnection;

	private boolean mConnected = false;

	protected void startClienteSSH() throws Exception {
		mStopping = false;
		mRunning = true;

		String servidor = mConfig.getPrivString(Settings.SERVIDOR_KEY);
		int porta = Integer.parseInt(mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY));
		String usuario = mConfig.getPrivString(Settings.USUARIO_KEY);

		String _senha = mConfig.getPrivString(Settings.SENHA_KEY);
		String senha = _senha.isEmpty() ? PasswordCache.getAuthPassword(null, false) : _senha;

		String keyPath = mConfig.getSSHKeypath();
		int portaLocal = Integer.parseInt(mConfig.getPrivString(Settings.PORTA_LOCAL_KEY));

		try {

			conectar(servidor, porta);

			for (int i = 0; i < AUTH_TRIES; i++) {
				if (mStopping) {
					return;
				}

				try {
					autenticar(usuario, senha, keyPath);

					break;
				} catch (IOException e) {
					if (i + 1 >= AUTH_TRIES) {
						throw new IOException("Authentication failed");
					} else {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e2) {
							return;
						}
					}
				}
			}

			SkStatus.updateStateString(SkStatus.SSH_CONECTADO, "SSH Connection Established");
			SkStatus.logError(
					"<strong><font color='#2AFF0D'>" + mContext.getString(R.string.state_connected) + "</font></strong>");

			//	VpnStatus.logWarning("<strong><html><font color='#0062AF'>" +
			// mContext.getString(R.string.state_connected) + "</font></html></strong>");

			/** if (mConfig.getSSHPinger() > 0) { startPinger(mConfig.getSSHPinger()); }* */
			startForwarder(portaLocal);

		} catch (Exception e) {
			mConnected = false;

			throw e;
		}
	}

	public synchronized void closeSSH() {
		stopForwarder();
		// stopPinger();
		interruptPinger();

		if (mConnection != null) {
			SkStatus.logDebug("Stopping SSH");
			mConnection.close();
		}
	}

	protected void conectar(String servidor, int porta) throws Exception {
		if (!mStarting) {
			throw new Exception();
		}
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		// aqui deve conectar
		try {
			mConnection = new Connection(servidor, porta);
			if (mConfig.getModoDebug() && !prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
				// Desativado, pois estava enchendo o Logger
				// mConnection.enableDebugging(true, this);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(mContext, "Debug mode enabled", Toast.LENGTH_SHORT).show();
					}
				});
			}

			// delay sleep
			if (MainApp.sp.getBoolean(Settings.DISABLE_DELAY_KEY, false)) {
				mConnection.setTCPNoDelay(true);
				VpnStatus.logWarning("<strong>Disable TCP Delay Actived</strong>");
			}

			// Data Compression
			if (mConfig.ssh_compression()) {
				mConnection.setCompression(true);
				VpnStatus.logWarning("<strong>SSH Compression Enabled</strong>");
			}

			// proxy
		/*	addProxy(prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false),
					prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT),
					(!prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true)
							? prefs.getString(Settings.CUSTOM_PAYLOAD_KEY, "")
							: null),
					mConfig.getPrivString(Settings.CUSTOM_SNI, mConnection);*/
            
            addProxy(prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false),
                 prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT),
                    (!prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true) ? mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY) : null), mConfig.getPrivString(Settings.CUSTOM_SNI),
                    mConnection);

			// monitora a conex├гo
			mConnection.addConnectionMonitor(this);

			if (Build.VERSION.SDK_INT >= 23) {
				ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
				ProxyInfo proxy = cm.getDefaultProxy();
				if (proxy != null) {
					VpnStatus.logWarning("<strong>Network Proxy:</strong> "
							+ String.format("%s:%d", proxy.getHost(), proxy.getPort()));
				}
			}

			SkStatus.updateStateString(SkStatus.SSH_CONECTANDO, mContext.getString(R.string.state_connecting));
			VpnStatus.logWarning(R.string.state_connecting);
			mConnection.connect(this, 10 * 1000, 20 * 1000);
			mConnected = true;

		} catch (Exception e) {

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String cause = e.getCause().toString();
			if (useProxy && cause.contains("Key exchange was not finished")) {
				//    VpnStatus.logWarning("Proxy: Connection Lost");
				VpnStatus.logWarning(cause);
			} else {
				//
			}
			throw new Exception(e);
		}
	}

	/** Autentica├з├гo */
	private static final String AUTH_PUBLICKEY = "publickey", AUTH_PASSWORD = "password";

	protected void autenticar(String usuario, String senha, String keyPath) throws IOException {
		if (!mConnected) {
			throw new IOException();
		}

		SkStatus.updateStateString(SkStatus.SSH_AUTENTICANDO, mContext.getString(R.string.state_auth));

		try {
			if (mConnection.isAuthMethodAvailable(usuario, AUTH_PASSWORD)) {

				if (mConnection.authenticateWithPassword(usuario, senha)) {
					VpnStatus.logWarning("<strong>" + mContext.getString(R.string.state_auth_success) + "</strong>");
				}
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, "Connection went away while we were trying to authenticate", e);
		} catch (Exception e) {
			Log.e(TAG, "Problem during handleAuthentication()", e);
		}

		try {
			if (mConnection.isAuthMethodAvailable(usuario, AUTH_PUBLICKEY) && keyPath != null && !keyPath.isEmpty()) {
				File f = new File(keyPath);
				if (f.exists()) {
					if (senha.equals(""))
						senha = null;

					VpnStatus.logWarning("Authenticating com public key");

					if (mConnection.authenticateWithPublicKey(usuario, f, senha)) {
						VpnStatus
								.logWarning("<strong>" + mContext.getString(R.string.state_auth_success) + "</strong>");
					}
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Host does not support 'Public key' authentication.");
		}

		if (!mConnection.isAuthenticationComplete()) {
			//  Authen = true;
			VpnStatus.logWarning("<font color = #C01C47> Failed to authenticate, User or Password Expired");
			stopAll();
			//  SkStatus.updateStateString(SkStatus.Failed_authenticate,
			// mContext.getString(R.string.state_auth_failed));
			throw new IOException("It was not possible to authenticate with the data provided");
		}
	}

	// XXX: Is it right?
	@Override
	public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt, boolean[] echo)
			throws Exception {
		String[] responses = new String[numPrompts];
		for (int i = 0; i < numPrompts; i++) {
			// request response from user for each prompt
			if (prompt[i].toLowerCase().contains("password"))
				responses[i] = mConfig.getPrivString(Settings.SENHA_KEY);
		}
		return responses;
	}

	/** ServerHostKeyVerifier Fingerprint */
	@Override
	public boolean verifyServerHostKey(String hostname, int port, String keyAlgorithm, byte[] hostKey)
			throws Exception {
		String createHexFingerprint = KnownHosts.createHexFingerprint(keyAlgorithm, hostKey);
		String createHashedHostname = KnownHosts.createHashedHostname(hostname);
		String createBubblebabbleFingerprint = KnownHosts.createBubblebabbleFingerprint(keyAlgorithm, hostKey);
		// VpnStatus.logWarning(new StringBuffer().append("Hostkey fingerprint:
		// ").append(createHexFingerprint).toString());
		// sshMsg(new StringBuffer().append("<b>Hashed Hostname:</b>
		// ").append(createHashedHostname).toString());
		/* VpnStatus.logWarning(
		new StringBuffer()
		        .append("<b>Bubble Babble  Fingerprint:</b>")
		        .append(createBubblebabbleFingerprint)
		        .toString());*/
		// VpnStatus.logWarning(new StringBuffer().append("<b>Key exchange algorithm:</b>
		// ").append(hostKey).toString());
		VpnStatus.logWarning(new StringBuffer().append("Using algorithm: ").append(keyAlgorithm)
				.append(" fingerprint: ").append(createHexFingerprint).toString());
		return true;
	}

	/** Proxy */
	private boolean useProxy = false;

	protected void addProxy(boolean isProteger, int mTunnelType, String mCustomPayload, String mCustomSNI,
			Connection conn) throws Exception {

		if (mTunnelType != 0) {
			useProxy = true;
			switch (mTunnelType) {
			case Settings.bTUNNEL_TYPE_SSH_DIRECT:
				VpnStatus.logWarning("Direct SSH");
				if (mCustomPayload != null) {
					try {
						ProxyData proxyData = new HttpProxyCustom(mConfig.getPrivString(Settings.SERVIDOR_KEY),
								Integer.parseInt(mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY)), null, null,
								mCustomPayload, true, mContext);
						conn.setProxyData(proxyData);
					} catch (Exception e) {
						throw new Exception(mContext.getString(R.string.error_proxy_invalid));
					}
				} else {
					useProxy = false;
				}
				break;
			case Settings.bTUNNEL_TYPE_SSH_PROXY:
				String customPayload = mCustomPayload;
				VpnStatus.logWarning("added proxy");
				if (customPayload != null && customPayload.isEmpty()) {
					customPayload = null;
				}

				String[] arr = MainApp.sp.getString(Settings.PROXY_IP_PORT, "").split("[:]");

				  String servidor = mConfig.getPrivString(Settings.PROXY_IP_KEY);
				  int porta = Integer.parseInt(mConfig.getPrivString(Settings.PROXY_PORTA_KEY));

				try {
					ProxyData proxyData = new HttpProxyCustom(arr[0], Integer.parseInt(arr[1]), null, null,
							customPayload, false, mContext);

					conn.setProxyData(proxyData);

				} catch (Exception e) {
					SkStatus.logError(R.string.error_proxy_invalid);

					throw new Exception(mContext.getString(R.string.error_proxy_invalid));
				}
				break;

			case Settings.bTUNNEL_TYPE_SSH_SSL:
                VpnStatus.logWarning("SSL");
				String customSNI = mCustomSNI;
				if (customSNI != null && customSNI.isEmpty()) {
					customPayload = null;
				}

				String sshServer = mConfig.getPrivString(Settings.SERVIDOR_KEY);
				int sshPort = Integer.parseInt(mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY));

				try {

					ProxyData sslTypeData = new SSLTunnelProxy(sshServer, sshPort, customSNI);
					conn.setProxyData(sslTypeData);

				} catch (Exception e) {
					VpnStatus.logWarning(e.getMessage());
				}
				break;
			case Settings.bTUNNEL_TYPE_PAY_SSL:
                VpnStatus.logWarning("Ssl/pay");
				String customSNI2 = mCustomSNI;
				if (customSNI2 != null && customSNI2.isEmpty()) {
					customSNI2 = null;
				}
				String customPayload2 = mCustomPayload;

				if (customPayload2 != null && customPayload2.isEmpty()) {
					customPayload2 = null;
				}

				String sshServer2 = mConfig.getPrivString(Settings.SERVIDOR_KEY);
				int sshPort2 = Integer.parseInt(mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY));

				try {
					SSLProxy sslTun = new SSLProxy(sshServer2, sshPort2, customSNI2, customPayload2);
					conn.setProxyData(sslTun);

				} catch (Exception e) {
					VpnStatus.logWarning(e.getMessage());
				}
				break;

			case Settings.bTUNNEL_TYPE_SLOWDNS:
                VpnStatus.logWarning("dns");
				if (mCustomPayload != null) {
					try {
						ProxyData proxyData = new HttpProxyCustom("127.0.0.1", Integer.parseInt("2222"), null, null,
								mCustomPayload, true, mContext);

						conn.setProxyData(proxyData);

						if (!mCustomPayload.isEmpty() && !isProteger)
							VpnStatus.logWarning(R.string.payload + mCustomPayload);

					} catch (Exception e) {
						throw new Exception(mContext.getString(R.string.error_proxy_invalid));
					}
				} else {
					useProxy = false;
				}
				break;
			case Settings.bTUNNEL_TYPE_SSL_RP:
				// Toast.makeText(mContext, "SSL Payload + Proxy", Toast.LENGTH_SHORT).show();
				VpnStatus.logWarning("pay proxy");
				String customSNI3 = mCustomSNI;
				if (customSNI3 != null && customSNI3.isEmpty()) {
					customSNI3 = null;
				}
				String customPayload3 = mCustomPayload;

				if (customPayload3 != null && customPayload3.isEmpty()) {
					customPayload3 = null;
				}

				String sshServer3 = mConfig.getPrivString(Settings.SERVIDOR_KEY);
				int sshPort3 = Integer.parseInt(mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY));
				String servidor3 = mConfig.getPrivString("proxyRemoto");
				int porta3 = Integer.parseInt(mConfig.getPrivString("proxyRemotoPorta"));

				try {
					SSLRemoteProxy sslTun = new SSLRemoteProxy(sshServer3, sshPort3, customSNI3, customPayload3);
					conn.setProxyData(sslTun);

				} catch (Exception e) {
					VpnStatus.logWarning(e.getMessage());
				}
				break;
			default:
				useProxy = false;
			}
		}
	}

	/** Socks5 Forwarder */
	private DynamicPortForwarder dpf;

	private synchronized void startForwarderSocks(int portaLocal) throws Exception {
		if (!mConnected) {
			throw new Exception();
		}

		// VpnStatus.logWarning("starting socks local");
		// SkStatus.logDebug(String.format("socks local listen: %d", portaLocal));

		try {

			int nThreads = mConfig.getMaximoThreadsSocks();

			if (nThreads > 0) {
				dpf = mConnection.createDynamicPortForwarder(portaLocal, nThreads);

				SkStatus.logDebug("socks local number threads: " + Integer.toString(nThreads));
			} else {
				dpf = mConnection.createDynamicPortForwarder(portaLocal);
			}

		} catch (Exception e) {
			SkStatus.logError("Socks Local: " + e.getCause().toString());

			throw new Exception();
		}
	}

	private synchronized void stopForwarderSocks() {
		if (dpf != null) {
			try {
				dpf.close();
			} catch (IOException e) {
			}
			dpf = null;
		}
	}

	/** Connection Monitor */
	@Override
	public void connectionLost(Throwable reason) {
		if (mStarting || mStopping || mReconnecting) {
			return;
		}

		SkStatus.logWarning("<strong>" + mContext.getString(R.string.log_conection_lost) + "</strong>");
		
		//
		reconnectSSH();
	}

	public boolean mReconnecting = false;

	public void reconnectSSH() {
		if (mStarting || mStopping || mReconnecting) {
			return;
		}

		mReconnecting = true;
		closeSSH();
	//	disconnectVibrate();
		SkStatus.updateStateString(SkStatus.SSH_RECONECTANDO, "Reconnecting..");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			mReconnecting = false;
			return;
		}

		SkStatus.clearLog();
		for (int i = 0; i < RECONNECT_TRIES; i++) {
			if (mStopping) {
				mReconnecting = false;
				return;
			}
			int sleepTime = 5;
			if (!TunnelUtils.isNetworkOnline(mContext)) {
				SkStatus.updateStateString(SkStatus.SSH_AGUARDANDO_REDE, "Waiting for network ..");
				VpnStatus.logWarning(R.string.state_nonetwork);
			} else {
				sleepTime = 3;
				mStarting = true;
				SkStatus.updateStateString(SkStatus.SSH_RECONECTANDO, "Reconnecting..");
				VpnStatus.logWarning("<strong>" + mContext.getString(R.string.state_reconnecting) + "</strong>");
				try {
					startClienteSSH();
					mStarting = false;
					mReconnecting = false;
					// mConnected = true;

					return;
				} catch (Exception e) {
					VpnStatus.logWarning("<strong><html><font color=\"red\">"
							+ mContext.getString(R.string.state_disconnected) + "</font></html></strong>");
				}
				mStarting = false;
			}
			try {
				Thread.sleep(sleepTime * 1000);
				i--;
			} catch (InterruptedException e2) {
				mReconnecting = false;
				return;
			}
		}
		mReconnecting = false;
		stopAll();
	}

	@Override
	public void onReceiveInfo(int id, String msg) {
		if (id == SERVER_BANNER) {
			VpnStatus.logWarning("<strong>" + mContext.getString(R.string.log_server_banner) + "</strong><br>" + msg);
		}
	}

	/** Debug Logger */
	@Override
	public void log(int level, String className, String message) {
		SkStatus.logDebug(String.format("%s: %s", className, message));
	}

	/** Vpn Tunnel */
	String serverAddr;

	protected void startTunnelVpnService() throws IOException {
		if (!mConnected) {
			throw new IOException();
		}
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		//VpnStatus.logWarning("starting tunnel service");
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		// Broadcast
		IntentFilter broadcastFilter =
			new IntentFilter(TunnelVpnService.TUNNEL_VPN_DISCONNECT_BROADCAST);
		broadcastFilter.addAction(TunnelVpnService.TUNNEL_VPN_START_BROADCAST);
		// Inicia Broadcast
		LocalBroadcastManager.getInstance(mContext)
			.registerReceiver(m_vpnTunnelBroadcastReceiver, broadcastFilter);

		String m_socksServerAddress = String.format("127.0.0.1:%s", mConfig.getPrivString(Settings.PORTA_LOCAL_KEY));
		boolean m_dnsForward = mConfig.getVpnDnsForward();
		boolean m_autoPing = mConfig.setAutoPing();
		boolean m_udpForward = mConfig.getVpnUdpForward();
		String m_udpResolver = mConfig.getVpnUdpForward() ? mConfig.getVpnUdpResolver() : null;

		String NmN = mConfig.getVpnUdpResolver().toString();
		String getUdpLastString = NmN.substring(NmN.lastIndexOf(":") + 1);

		String servidorIP = mConfig.getPrivString(Settings.SERVIDOR_KEY);

		String[] arr = MainApp.sp.getString(Settings.PROXY_IP_PORT, "").split("[:]");

		if (prefs.getInt(Settings.TUNNELTYPE_KEY,
				Settings.bTUNNEL_TYPE_SSH_DIRECT) == Settings.bTUNNEL_TYPE_SSH_PROXY) {
			try {
				servidorIP = arr[0];
			} catch (Exception e) {
				VpnStatus.logWarning(R.string.error_proxy_invalid);

				throw new IOException(mContext.getString(R.string.error_proxy_invalid));
			}
		}

		try {
            if (isudpmode()) {
                serverAddr = servidorIP = getIPv4Addresses(InetAddress.getAllByName(prefs.getString(Settings.SERVIDOR_KEY, ""))).getHostAddress();
            } else {
			InetAddress servidorAddr = TransportManager.createInetAddress(servidorIP);
            serverAddr = servidorIP = servidorAddr.getHostAddress(); }
		} catch(UnknownHostException e) {
			throw new IOException(mContext.getString(R.string.error_server_ip_invalid));
		}

		String[] m_excludeIps = { servidorIP };

        /*if (prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT) == Settings.bTUNNEL_TYPE_SSH_PROXY) {
            try {
                servidorIP = arr[0];
            } catch(Exception e) {
                SkStatus.logError(R.string.error_proxy_invalid);

                throw new IOException(mContext.getString(R.string.error_proxy_invalid));
            }
        }

        try {
            if (isudpmode()) {
                serverAddr = servidorIP = getIPv4Addresses(InetAddress.getAllByName(prefs.getString(Settings.SERVIDOR_KEY, ""))).getHostAddress();
            } else {
                InetAddress servidorAddr = TransportManager.createInetAddress(servidorIP);
                serverAddr = servidorIP = servidorAddr.getHostAddress(); }
        } catch (UnknownHostException e) {
            throw new IOException(mContext.getString(R.string.error_server_ip_invalid));
        }

        String[] m_excludeIps = {servidorIP};*/
        
		String[] m_dnsResolvers = null;
		if (m_dnsForward) {
			if (mConfig.getVpnDnsResolver2().isEmpty()) {
				m_dnsResolvers = new String[] { mConfig.getVpnDnsResolver1() };
			} else {
				m_dnsResolvers = new String[] { mConfig.getVpnDnsResolver1(), mConfig.getVpnDnsResolver2() };
			}
		} else {
			List<String> lista = VpnUtils.getNetworkDnsServer(mContext);
			m_dnsResolvers = new String[] { lista.get(0) };
		}

		/*if (!m_udpForward && !m_dnsForward && !m_autoPing) {
		          VpnStatus.logWarning(
		                  "UDPGW &amp; DNS &amp; AutoPing " + mContext.getText(R.string.disabled));
		      } else if (!m_udpForward && !m_dnsForward) {
		          VpnStatus.logWarning("UDPGW &amp; DNS " + mContext.getText(R.string.disabled));
		      } else if (!m_udpForward && !m_autoPing) {
		          VpnStatus.logWarning("UDPGW &amp; AutoPing " + mContext.getText(R.string.disabled));
		      } else if (!m_dnsForward && !m_udpForward) {
		          VpnStatus.logWarning("DNS &amp; UDPGW " + mContext.getText(R.string.disabled));
		      } else if (!m_dnsForward && !m_autoPing) {
		          VpnStatus.logWarning("DNS &amp; AutoPing " + mContext.getText(R.string.disabled));
		      } else if (!m_autoPing && !m_udpForward) {
		          VpnStatus.logWarning("UDPGW &amp; AutoPing " + mContext.getText(R.string.disabled));
		      } else if (!m_autoPing && !m_dnsForward) {
		          VpnStatus.logWarning("DNS &amp; AutoPing " + mContext.getText(R.string.disabled));
		      } else if (!m_udpForward) {
		          VpnStatus.logWarning("UDPGW " + mContext.getText(R.string.disabled));
		      } else if (!m_dnsForward) {
		          VpnStatus.logWarning("DNS " + mContext.getText(R.string.disabled));
		      } else if (!m_autoPing) {
		          VpnStatus.logWarning("AutoPing " + mContext.getText(R.string.disabled));
		      }
		*/

		if (m_udpForward) {
			VpnStatus.logWarning("UDP GateWay : <strong><font color='#2AFF0D'>" + getUdpLastString + "</font></strong>");
		}  

		if (m_dnsForward) {
			String primDns = mPref.getString(Settings.DNSRESOLVER_KEY1, "");
			String secDns = mPref.getString(Settings.DNSRESOLVER_KEY2, "");
			if (!primDns.isEmpty() || !secDns.isEmpty()) {
			//	VpnStatus.logWarning("DNS Enabled: Custom DNS");
				VpnStatus.logWarning("DNS 1 : <strong><font color='#2AFF0D'>" + primDns + "</font></strong>");
			    VpnStatus.logWarning("DNS 2 : <strong><font color='#2AFF0D'>" + secDns + "</font></strong>");
			} else {
			//	VpnStatus.logWarning("DNS Enabled: Default Google DNS");
			}
		}

		if (isServiceVpnRunning()) {
			Log.d(TAG, "already running service");

			TunnelVpnManager tunnelManager = TunnelState.getTunnelState()
				.getTunnelManager();
			
			if (tunnelManager != null) {
				tunnelManager.restartTunnel(m_socksServerAddress);
			}

			return;
		}

		Intent startTunnelVpn = new Intent(mContext, TunnelVpnService.class);
		startTunnelVpn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		TunnelVpnSettings settings = new TunnelVpnSettings(m_socksServerAddress, m_dnsForward, m_dnsResolvers,
														   (m_dnsForward && m_udpResolver == null || !m_dnsForward && m_udpResolver != null), m_udpResolver, m_excludeIps,
														   mConfig.getIsFilterApps(), mConfig.getIsFilterBypassMode(), mConfig.getFilterApps(), mConfig.getIsTetheringSubnet(), mConfig.getBypass());
		startTunnelVpn.putExtra(TunnelVpnManager.VPN_SETTINGS, settings);
		if (mContext.startService(startTunnelVpn) == null) {
			VpnStatus.logWarning("failed to start tunnel vpn service");

			throw new IOException("Failed to start VPN Service");
		}

		TunnelState.getTunnelState().setStartingTunnelManager();
	}

	public static boolean isServiceVpnRunning() {
        TunnelState tunnelState = TunnelState.getTunnelState();
        return tunnelState.getStartingTunnelManager() || tunnelState.getTunnelManager() != null;
    }

    public static Inet4Address getIPv4Addresses(InetAddress[] inetAddressArr) {
        for (InetAddress inetAddress : inetAddressArr) {
            if (inetAddress instanceof Inet4Address) {
                return (Inet4Address) inetAddress;
            }
        }
        return null;
    }

    protected synchronized void stopTunnelVpnService() {
        if (!isServiceVpnRunning()) {
            return;
        }


        TunnelVpnManager currentTunnelManager = TunnelState.getTunnelState()
                .getTunnelManager();

        if (currentTunnelManager != null) {
            currentTunnelManager.signalStopService();
        }
        // Parando Broadcast
        LocalBroadcastManager.getInstance(mContext)
                .unregisterReceiver(m_vpnTunnelBroadcastReceiver);
    }

    //private Thread mThreadLocation;

    // Local BroadcastReceiver
    private BroadcastReceiver m_vpnTunnelBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (TunnelVpnService.TUNNEL_VPN_START_BROADCAST.equals(action)) {
                boolean startSuccess = intent.getBooleanExtra(TunnelVpnService.TUNNEL_VPN_START_SUCCESS_EXTRA, true);

                if (!startSuccess) {
                    stopAll();
                }

            } else if (TunnelVpnService.TUNNEL_VPN_DISCONNECT_BROADCAST.equals(action)) {
                stopAll();
            }
        }
    };

    private boolean isudpmode() {
        return prefs.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_TYPE_UDP; }
    private boolean isv2raymode() {
        return prefs.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_TYPE_V2RAY; }

    private void setCallback() {
        connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest request = new NetworkRequest.Builder().build();
        callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
            }
            @Override
            public void onLost(Network network) {
                if (runningatesccn) {
                    if(isudpmode() || isv2raymode()){
                        stopAll();
                    }
                }
            }
        };
        connectivityManager.registerNetworkCallback(request, callback); }

}

