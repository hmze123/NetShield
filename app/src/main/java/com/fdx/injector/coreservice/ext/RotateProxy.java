package com.fdx.injector.coreservice.ext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RotateProxy extends Thread {
    public static final Pattern j = Pattern.compile("CONNECT (.+):(.+) HTTP/(1\\.[01])", 2);
    public final Socket k;
    public boolean l = false;

    public class a extends Thread {
        public final Socket j;

        public a(Socket socket) {
            this.j = socket;
        }

        @Override
        public void run() {
            RotateProxy.c(this.j, RotateProxy.this.k);
        }
    }

    public class sHandler extends Thread {
        public final Socket j;

        public sHandler(Socket socket) {
            this.j = socket;
        }

        @Override
        public void run() {
            RotateProxy.c(this.j, RotateProxy.this.k);
        }
    }

    public RotateProxy(Socket socket) {
        this.k = socket;
    }

    public static void c(Socket socket, Socket socket2) {
        int read;
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket2.getOutputStream();
            byte[] bArr = new byte[4096];
            do {
                read = inputStream.read(bArr);
                if (read > 0) {
                    outputStream.write(bArr, 0, read);
                    if (inputStream.available() < 1) {
                        outputStream.flush();
                        continue;
                    } else {
                        continue;
                    }
                }
            } while (read >= 0);
            if (!socket2.isOutputShutdown()) {
                socket2.shutdownOutput();
            }
            if (!socket.isInputShutdown()) {
                socket.shutdownInput();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public final void a(String str) {
        Exception e2;
        try {
            InputStream inputStream = this.k.getInputStream();
            OutputStream outputStream = this.k.getOutputStream();
            System.out.println(str);
            try {
                String str2 = str.split(" ")[1];
                System.out.println(str2);
                if (!str2.matches("(http://)?.+\\.\\w+(/.+)*/?")) {
                    inputStream.close();
                    outputStream.close();
                    this.k.close();
                    return;
                }
                OutputStreamWriter outputStreamWriter =
                        new OutputStreamWriter(
                                this.k.getOutputStream(), StandardCharsets.ISO_8859_1);
                String str3 =
                        str2.matches("http://.+\\.\\w+(/.+)*/?")
                                ? str2.split("/")[2]
                                : str2.split("/")[0];
                try {
                    Socket socket = new Socket(str3, 80);
                    System.out.println(socket);
                    a aVar = new a(socket);
                    aVar.start();
                    System.out.println(str2.replaceAll("http://.+\\.\\w+/", "/"));
                    OutputStream outputStream2 = socket.getOutputStream();
                    outputStream2.write(
                            ("GET " + str2.replaceAll("http://.+\\.\\w+/", "/") + " HTTP/1.1\r\n")
                                    .getBytes());
                    if (this.l) {
                        int read = this.k.getInputStream().read();
                        if (read != -1) {
                            if (read != 10) {
                                socket.getOutputStream().write(read);
                            }
                            c(this.k, socket);
                        } else {
                            if (!socket.isOutputShutdown()) {
                                socket.shutdownOutput();
                            }
                            if (!this.k.isInputShutdown()) {
                                this.k.shutdownInput();
                            }
                        }
                    } else {
                        c(this.k, socket);
                    }
                    try {
                        aVar.join();
                    } catch (InterruptedException e3) {
                        e3.printStackTrace();
                    }
                    socket.close();
                } catch (IOException e4) {
                    e2 = e4;
                    e2.printStackTrace();
                    outputStreamWriter.write("HTTP/" + str3 + " 502 Bad Gateway\r\n");
                    outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                    outputStreamWriter.write("\r\n");
                    outputStreamWriter.flush();
                } catch (NumberFormatException e5) {
                    e2 = e5;
                    e2.printStackTrace();
                    outputStreamWriter.write("HTTP/" + str3 + " 502 Bad Gateway\r\n");
                    outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                    outputStreamWriter.write("\r\n");
                    outputStreamWriter.flush();
                }
            } catch (ArrayIndexOutOfBoundsException e6) {
                e6.printStackTrace();
                this.k.close();
            }
        } catch (IOException e7) {
            e7.printStackTrace();
        }
    }

    public final void b(String str) throws IOException {
        Matcher matcher = j.matcher(str);
        if (matcher.matches()) {
            do {} while (!"".equals(d(this.k)));
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(this.k.getOutputStream(), StandardCharsets.ISO_8859_1);
            try {
                String group = matcher.group(1);
                String group2 = matcher.group(2);
                Objects.requireNonNull(group2);
                Socket socket = new Socket(group, Integer.parseInt(group2));
                System.out.println(socket);
                try {
                    outputStreamWriter.write(
                            "HTTP/" + matcher.group(3) + " 200 Connection established\r\n");
                    outputStreamWriter.write("\r\n");
                    outputStreamWriter.flush();
                    sHandler bVar = new sHandler(socket);
                    bVar.start();
                    if (this.l) {
                        int read = this.k.getInputStream().read();
                        if (read != -1) {
                            if (read != 10) {
                                socket.getOutputStream().write(read);
                            }
                            c(this.k, socket);
                        } else {
                            if (!socket.isOutputShutdown()) {
                                socket.shutdownOutput();
                            }
                            if (!this.k.isInputShutdown()) {
                                this.k.shutdownInput();
                            }
                        }
                    } else {
                        c(this.k, socket);
                    }
                    try {
                        bVar.join();
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                } finally {
                    socket.close();
                }
            } catch (IOException | NumberFormatException e3) {
                e3.printStackTrace();
                outputStreamWriter.write("HTTP/" + matcher.group(3) + " 502 Bad Gateway\r\n");
                outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                outputStreamWriter.write("\r\n");
                outputStreamWriter.flush();
            }
        }
    }

    public final String d(Socket socket) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int read = socket.getInputStream().read();
            if (read == -1) {
                break;
            } else if (!this.l || read != 10) {
                this.l = false;
                if (read != 10) {
                    if (read == 13) {
                        this.l = true;
                        break;
                    }
                    byteArrayOutputStream.write(read);
                } else {
                    break;
                }
            } else {
                this.l = false;
            }
        }
        return byteArrayOutputStream.toString("ISO-8859-1");
    }

    @Override
    public void run() {
        try {
            try {
                try {
                    String d2 = d(this.k);
                    System.out.println(d2);
                    if (d2.startsWith("CONNECT ")) {
                        b(d2);
                    } else {
                        a(d2);
                    }
                    this.k.close();
                } catch (Throwable th) {
                    try {
                        this.k.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    throw th;
                }
            } catch (IOException e3) {
                e3.printStackTrace();
                this.k.close();
            }
        } catch (IOException e4) {
            while (true) {
                e4.printStackTrace();
                return;
            }
        }
    }
}
