/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.rcon.thread;

import com.mojang.logging.LogUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.rcon.PktUtils;
import net.minecraft.server.rcon.thread.GenericThread;
import org.slf4j.Logger;

public class RconClient
extends GenericThread {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SERVERDATA_AUTH = 3;
    private static final int SERVERDATA_EXECCOMMAND = 2;
    private static final int SERVERDATA_RESPONSE_VALUE = 0;
    private static final int SERVERDATA_AUTH_RESPONSE = 2;
    private static final int SERVERDATA_AUTH_FAILURE = -1;
    private boolean authed;
    private final Socket client;
    private final byte[] buf = new byte[1460];
    private final String rconPassword;
    private final ServerInterface serverInterface;

    RconClient(ServerInterface serverInterface, String string, Socket socket) {
        super("RCON Client " + socket.getInetAddress());
        this.serverInterface = serverInterface;
        this.client = socket;
        try {
            this.client.setSoTimeout(0);
        } catch (Exception exception) {
            this.running = false;
        }
        this.rconPassword = string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        try {
            while (this.running) {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(this.client.getInputStream());
                int i = bufferedInputStream.read(this.buf, 0, 1460);
                if (10 > i) {
                    return;
                }
                int j = 0;
                int k = PktUtils.intFromByteArray(this.buf, 0, i);
                if (k != i - 4) {
                    return;
                }
                int l = PktUtils.intFromByteArray(this.buf, j += 4, i);
                int m = PktUtils.intFromByteArray(this.buf, j += 4);
                j += 4;
                switch (m) {
                    case 3: {
                        String string = PktUtils.stringFromByteArray(this.buf, j, i);
                        j += string.length();
                        if (!string.isEmpty() && string.equals(this.rconPassword)) {
                            this.authed = true;
                            this.send(l, 2, "");
                            break;
                        }
                        this.authed = false;
                        this.sendAuthFailure();
                        break;
                    }
                    case 2: {
                        if (this.authed) {
                            String string2 = PktUtils.stringFromByteArray(this.buf, j, i);
                            try {
                                this.sendCmdResponse(l, this.serverInterface.runCommand(string2));
                            } catch (Exception exception) {
                                this.sendCmdResponse(l, "Error executing: " + string2 + " (" + exception.getMessage() + ")");
                            }
                            break;
                        }
                        this.sendAuthFailure();
                        break;
                    }
                    default: {
                        this.sendCmdResponse(l, String.format(Locale.ROOT, "Unknown request %s", Integer.toHexString(m)));
                    }
                }
            }
        } catch (IOException bufferedInputStream) {
        } catch (Exception exception2) {
            LOGGER.error("Exception whilst parsing RCON input", exception2);
        } finally {
            this.closeSocket();
            LOGGER.info("Thread {} shutting down", (Object)this.name);
            this.running = false;
        }
    }

    private void send(int i, int j, String string) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1248);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        dataOutputStream.writeInt(Integer.reverseBytes(bs.length + 10));
        dataOutputStream.writeInt(Integer.reverseBytes(i));
        dataOutputStream.writeInt(Integer.reverseBytes(j));
        dataOutputStream.write(bs);
        dataOutputStream.write(0);
        dataOutputStream.write(0);
        this.client.getOutputStream().write(byteArrayOutputStream.toByteArray());
    }

    private void sendAuthFailure() throws IOException {
        this.send(-1, 2, "");
    }

    private void sendCmdResponse(int i, String string) throws IOException {
        int k;
        int j = string.length();
        do {
            k = 4096 <= j ? 4096 : j;
            this.send(i, 0, string.substring(0, k));
        } while (0 != (j = (string = string.substring(k)).length()));
    }

    @Override
    public void stop() {
        this.running = false;
        this.closeSocket();
        super.stop();
    }

    private void closeSocket() {
        try {
            this.client.close();
        } catch (IOException iOException) {
            LOGGER.warn("Failed to close socket", iOException);
        }
    }
}

