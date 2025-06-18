package com.fdx.injector.coreservice.tunnel.vpn;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Build;
import android.util.Log;

import com.fdx.injector.R;
import com.fdx.injector.coreservice.config.Settings;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import de.blinkt.openvpn.core.VpnStatus;

public class TunnelVpnManager implements Tunnel.HostService
{

	private static final String TAG = "TunnelManager";
	public static final String VPN_SETTINGS = "vpnSettings";
	
	private TunnelVpnService m_parentService = null;
	private CountDownLatch m_tunnelThreadStopSignal;
	private Thread m_tunnelThread;
	private AtomicBoolean m_isStopping;
	private Tunnel m_tunnel = null;
	private AtomicBoolean m_isReconnecting;
	private TunnelVpnSettings mSettings;
	
	public interface ManagerListener {
		public void onLog(String msg);
	}

	public TunnelVpnManager(TunnelVpnService parentService)
	{
		m_parentService = parentService;
		m_isStopping = new AtomicBoolean(false);
		m_isReconnecting = new AtomicBoolean(false);
		m_tunnel = Tunnel.newTunnel(this);
	}

	// Implementation of android.app.Service.onStartCommand
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "onStartCommand");
		
		if (intent == null) {
			Log.e(TAG, "Failed to receive intent");
			m_parentService.broadcastVpnStart(false);
			return 0;
		}
		
		mSettings = intent.getParcelableExtra(VPN_SETTINGS);
		if (mSettings == null) {
			Log.e(TAG, "Failed to receive the Vpn Settings.");
			m_parentService.broadcastVpnStart(false);
			return 0;
		}
		
		if (mSettings.mSocksServer == null)
		{
			Log.e(TAG, "Failed to receive the socks server address.");
			m_parentService.broadcastVpnStart(false);
			return 0;
		}
		
		if (mSettings.mDnsResolver == null)
		{
			Log.e(TAG, "Failed to receive the dns resolvers.");
			m_parentService.broadcastVpnStart(false);
			return 0;
		}
		
		try
		{
			if (!m_tunnel.startRouting(mSettings))
			{
				Log.e(TAG, "Failed to establish VPN");
				m_parentService.broadcastVpnStart(false);
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, String.format("Failed to establish VPN: %s", e.getMessage()));
			m_parentService.broadcastVpnStart(false);
		}
		
		return Service.START_NOT_STICKY;
	}

	// Implementation of android.app.Service.onDestroy
	public void onDestroy()
	{
		if (m_tunnelThread == null)
		{
			return;
		}
		// signalStopService should have been called, but in case is was not, call here.
		// If signalStopService was not already called, the join may block the calling
		// thread for some time.
		signalStopService();

		try
		{
			m_tunnelThread.join();
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		m_tunnelThreadStopSignal = null;
		m_tunnelThread = null;
	}

	// Signals the runTunnel thread to stop. The thread will self-stop the service.
	// This is the preferred method for stopping the tunnel service:
	// 1. VpnService doesn't respond to stopService calls
	// 2. The UI will not block while waiting for stopService to return
	public void signalStopService()
	{
		if (m_tunnelThreadStopSignal != null)
		{
			m_tunnelThreadStopSignal.countDown();
		}
	}

	// Stops the tunnel thread and restarts it with |socksServerAddress|.
	public void restartTunnel(final String socksServerAddress)
	{
		Log.i(TAG, "Restarting tunnel.");
		if (socksServerAddress == null ||
			socksServerAddress.equals(mSettings.mSocksServer))
		{
			// Don't reconnect if the socks server address hasn't changed.
			m_parentService.broadcastVpnStart(true);
			return;
		}
		mSettings.mSocksServer = socksServerAddress;
		m_isReconnecting.set(true);

		// Signaling stop to the tunnel thread with the reconnect flag set causes
		// the thread to stop the tunnel (but not the VPN or the service) and send
		// the new SOCKS server address to the DNS resolver before exiting itself.
		// When the DNS broadcasts its local address, the tunnel will restart.
		signalStopService();
	}

	private void startTunnel()
	{
		m_tunnelThreadStopSignal = new CountDownLatch(1);
		m_tunnelThread =
			new Thread(
            new Runnable() {
				@Override
				public void run()
				{
					runTunnel(mSettings.mSocksServer, mSettings.mDnsResolver, mSettings.mDnsForward, mSettings.mUdpResolver, mSettings.mUdpDnsRelay);
				}
            });
		m_tunnelThread.start();
	}

	private void runTunnel(String socksServerAddress, String[] dnsResolver, boolean forwardDns, String udpResolver, boolean udpDnsRelay)
	{
		m_isStopping.set(false);

		try
		{
			if (!m_tunnel.startTunneling(socksServerAddress, dnsResolver, forwardDns, udpResolver, udpDnsRelay))
			{
				throw new Exception("application is not prepared or revoked");
			}
			Log.i(TAG, "VPN service running");
			m_parentService.broadcastVpnStart(true);

			try
			{
				m_tunnelThreadStopSignal.await();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}

			m_isStopping.set(true);

		}
		catch (Exception e)
		{
			Log.e(TAG, String.format("Start tunnel failed: %s", e.getMessage()));
			m_parentService.broadcastVpnStart(false);
		}
		finally
		{
			if (m_isReconnecting.get())
			{
				// Stop tunneling only, not VPN, if reconnecting.
				Log.i(TAG, "Stopping tunnel.");
				m_tunnel.stopTunneling();
			}
			else
			{
				// Stop VPN tunnel and service only if not reconnecting.
				Log.i(TAG, "Stopping VPN and tunnel.");
				m_tunnel.stop();
				m_parentService.stopForeground(true);
				m_parentService.stopSelf();
			}
			m_isReconnecting.set(false);
		}
	}

	//----------------------------------------------------------------------------
	// Tunnel.HostService
	//----------------------------------------------------------------------------

	@Override
	public String getAppName()
	{
		return getContext().getString(R.string.app_name);
	}

	@Override
	public Context getContext()
	{
		return m_parentService;
	}

	@Override
	public VpnService getVpnService()
	{
		return m_parentService;
	}

	@Override
	public VpnService.Builder newVpnServiceBuilder()
	{
		return m_parentService.newBuilder();
	}

	@Override
	public void onDiagnosticMessage(String message)
	{
		SharedPreferences prefs = new Settings(getContext()).getPrefsPrivate();
		
		if (prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
			for (String ip : mSettings.mExcludeIps) {
				message = message.replace(ip, "********");
			}
		}
		
		VpnStatus.logWarning(message);
	}

	@Override
	public void onTunnelConnected()
	{
		//VpnStatus.logWarning("<strong>Tunnel Connected</strong>");
		VpnStatus.logWarning("<strong><font color='#2AFF0D'>Socket Injector Connected</font></strong>");
	}

	@Override
	@TargetApi(Build.VERSION_CODES.M)
	public void onVpnEstablished()
	{
		//VpnStatus.logWarning("<strong>VPN Established</strong>");
		//VpnStatus.logWarning("<strong><font color='#ef08ff'>VPN Established</font></strong>");
		startTunnel();
	}
	
}
