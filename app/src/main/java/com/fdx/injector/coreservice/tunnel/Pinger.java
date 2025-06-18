package com.fdx.injector.coreservice.tunnel;

import de.blinkt.openvpn.core.VpnStatus;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;
import com.trilead.ssh2.*;
import com.fdx.injector.coreservice.logger.*;

public class Pinger extends Thread {
    private final Connection a;
    private final String b;
    private LocalPortForwarder c;
    private boolean d;
    private Socket f;

    public Pinger(Connection aVar, String str) {
        this.a = aVar;
        this.b = str;
    }

    private int b() {
        return (new Random().nextInt(6) + 5) * 1000;
    }

    public synchronized void close() {
        this.d = false;
        interrupt();
    }
	
    public void run() {
		VpnStatus.logWarning("Ping server: " + this.b);
        try {
			//VpnStatus.logWarning("pinger started");
			this.c = this.a.createLocalPortForwarder(9395, this.b, 80);
            this.d = true;
            while (this.d) {
                try {
                    this.f = new Socket("127.0.0.1", 9395);
                    this.f.setSoTimeout(20000);
                    OutputStream outputStream = this.f.getOutputStream();
                    outputStream.write(("GET http://" + this.b + "/ HTTP/1.1\r\nHost: " + this.b + "\r\n\r\n").getBytes());
                    outputStream.flush();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.f.getInputStream()));
                    String readLine = bufferedReader.readLine();
                    if (readLine != null) {
						VpnStatus.logWarning("Pinger response code: " + readLine);
                    } else {
						VpnStatus.logWarning("Pinger: No Data");
                    }
                    bufferedReader.close();
                    outputStream.close();
                    this.f.close();
                } catch (Exception e) {
					VpnStatus.logWarning("request time out");
                }
                try {
                    sleep((long) b());
                } catch (Exception e2) {
					//VpnStatus.logWarning("pinger stopped");
					this.c.close(); // fixed (address already in use) bug
					this.c = null; // This is required
                    return;
                }
            }
        } catch (Exception e3) {
			VpnStatus.logWarning("Pinger: " + e3.toString());
		}
    }
}
