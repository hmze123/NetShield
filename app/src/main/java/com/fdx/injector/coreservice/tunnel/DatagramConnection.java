package com.fdx.injector.coreservice.tunnel;

import com.fdx.injector.coreservice.config.Settings;
import com.fdx.injector.coreservice.logger.SkStatus;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DatagramConnection {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private boolean isConnected;
    private Settings mConfig;

    public DatagramConnection(String serverIP, int serverPort) {
        try {

            String udp_server = mConfig.getPrivString(Settings.SERVIDOR_KEY);
            this.serverAddress = InetAddress.getByName(udp_server);
            this.serverPort = 22;
            // Leer el valor del servidor desde mConfig y asignarlo a udp_server

            this.socket = new DatagramSocket();
            this.isConnected = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startConnection() {

        
        Thread sendThread = new Thread(() -> {
            try {
                while (isConnected) {
                    byte[] sendData = "Ping".getBytes(); 
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
                    socket.send(sendPacket);
                    System.out.println("Datagram sent: " + new String(sendData));

                    String packetData = new String(sendPacket.getData(), sendPacket.getOffset(), sendPacket.getLength());
                    SkStatus.logInfo("Datagram sent" + packetData);
                    Thread.sleep(1000); 
                }
            } catch (Exception e) {
                e.printStackTrace();
                SkStatus.logInfo("ERROR Datagram sent" + e);
            }
        });
        sendThread.start();

        
        Thread receiveThread = new Thread(() -> {
            try {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                while (isConnected) {
                    socket.receive(receivePacket);
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Response received: " + response);
                    SkStatus.logInfo("Response received: " + response);

                }
            } catch (Exception e) {
                e.printStackTrace();
                SkStatus.logInfo("ERROR Datagram sent" + e);
            }
        });
        receiveThread.start();
    }

    public void stopConnection() {
        isConnected = false;
        socket.close();
    }
}