package com.fdx.injector.coreservice.tunnel.vpn;

import java.io.File;
import android.util.Log;
import java.io.BufferedReader;
import android.content.Context;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;

import com.fdx.injector.*;
import com.fdx.injector.coreservice.util.StreamGobbler;
import com.fdx.injector.coreservice.util.FileUtils;
import com.fdx.injector.coreservice.logger.SkStatus;
import com.fdx.injector.coreservice.util.CustomNativeLoader;

public class Dnsd extends Thread {
	private final static String TAG = "DnsdThread";
	private final static String PDNSD_SERVER = "server {\n label= \"%1$s\";\n ip = %2$s;\n port = %3$d;\n uptest = none;\n }\n";
	private final static String PDNSD_SERVER_TEST = "server {\n label= \"%1$s\";\n ip = %2$s;\n port = %3$d;\n reject = ::/0;\n reject_policy = negate;\n reject_recursively = on;\n timeout = 5;\n }\n";
	private final static String PDNSD_BIN = "libpdnsd";
	
	private OnDnsdListener mListener;
	public interface OnDnsdListener {
		public void onStart();
		public void onStop();
	}
	
	private Process mProcess;
	private File fileDnsd;
	
	private Context mContext;
	private String[] mDnsHosts;
	private int mDnsPort;
	private String mDnsdHost;
	private int mDnsdPort;
	
	public Dnsd(Context context, String[] dnsHosts, int dnsPort, String pdnsdHost, int pdnsdPort) {
		mContext = context;
		
		mDnsHosts = dnsHosts;
		mDnsPort = dnsPort;
		mDnsdHost = pdnsdHost;
		mDnsdPort = pdnsdPort;
	}

	@Override
	public void run() {
		
		if (mListener != null) {
			mListener.onStart();
		}
		
		try {
			
			//File fileDnsd = CustomNativeLoader.loadExecutableBinary(mContext, "libpdnsd.so");
			fileDnsd = CustomNativeLoader.loadNativeBinary(mContext, PDNSD_BIN, new File(mContext.getFilesDir(), PDNSD_BIN));
			
			if (fileDnsd == null) {
				throw new IOException("Dnsd bin not found");
			}
			
			File fileConf = makeDnsdConf(mContext.getFilesDir(), mDnsHosts, mDnsPort, mDnsdHost, mDnsdPort);
			
			String cmdString = fileDnsd.getCanonicalPath() + " -v9 -c " + fileConf.toString();
			
			mProcess = Runtime.getRuntime().exec(cmdString);
			
			StreamGobbler.OnLineListener onLineListener = new StreamGobbler.OnLineListener(){
				@Override
				public void onLine(String log){
					SkStatus.logDebug("Dnsd: " + log);
				}
			};

			StreamGobbler stdoutGobbler = new StreamGobbler(mProcess.getInputStream(), onLineListener);
			StreamGobbler stderrGobbler = new StreamGobbler(mProcess.getErrorStream(), onLineListener);

			stdoutGobbler.start();
			stderrGobbler.start();

			mProcess.waitFor();

		} catch (IOException e) {
			SkStatus.logException("Dnsd Error", e);
		} catch(Exception e){
			SkStatus.logDebug("Dnsd Error: " + e);
		}
		
		mProcess = null;
		if (mListener != null) {
			mListener.onStop();
		}

	}

	@Override
	public synchronized void interrupt()
	{
		// TODO: Implement this method
		super.interrupt();
		
		if (mProcess != null)
			mProcess.destroy();
			
		try {
			if (fileDnsd != null)
				VpnUtils.killProcess(fileDnsd);
		} catch(Exception e) {}
		
		mProcess = null;
		fileDnsd = null;
	}

    private File makeDnsdConf(File fileDir, String[] dnsHosts, int dnsPort, String pdnsdHost, int pdnsdPort) throws FileNotFoundException, IOException {
        String content = FileUtils.readFromRaw(mContext, R.raw.pdnsd_local);
		
		// dns servers
		StringBuilder server_dns = new StringBuilder();
		for (int i = 0; i < dnsHosts.length; i++){
			String dnsHost = dnsHosts[i];
			server_dns.append(String.format(PDNSD_SERVER, "server" + Integer.toString(i+1), dnsHost, dnsPort));
			//server_dns.append(String.format(PDNSD_SERVER_TEST, "server" + Integer.toString(i+1), "127.0.0.1", 8865));
		}
		
		String conf = String.format(content, server_dns.toString(), fileDir.getCanonicalPath(), pdnsdHost, pdnsdPort);
		
        Log.d(TAG,"pdnsd conf:" + conf);

        File f = new File(fileDir,"pdnsd.conf");
        if (f.exists()) {
			f.delete();
		}
		FileUtils.saveTextFile(f, conf);
		
        File cache = new File(fileDir,"pdnsd.cache");
        if (!cache.exists()) {
        	try {
            	cache.createNewFile();
            } catch (Exception e) {}
        }

        return f;
	}
	
	public void setOnDnsdListener(OnDnsdListener listener){
		this.mListener = listener;
	}
}
