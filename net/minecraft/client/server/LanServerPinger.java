/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DefaultUncaughtExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class LanServerPinger
extends Thread {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger();
    private final String motd;
    private final DatagramSocket socket;
    private boolean isRunning = true;
    private final String serverAddress;

    public LanServerPinger(String string, String string2) throws IOException {
        super("LanServerPinger #" + UNIQUE_THREAD_ID.incrementAndGet());
        this.motd = string;
        this.serverAddress = string2;
        this.setDaemon(true);
        this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        this.socket = new DatagramSocket();
    }

    @Override
    public void run() {
        String string = LanServerPinger.createPingString(this.motd, this.serverAddress);
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        while (!this.isInterrupted() && this.isRunning) {
            try {
                InetAddress inetAddress = InetAddress.getByName("224.0.2.60");
                DatagramPacket datagramPacket = new DatagramPacket(bs, bs.length, inetAddress, 4445);
                this.socket.send(datagramPacket);
            } catch (IOException iOException) {
                LOGGER.warn("LanServerPinger: {}", (Object)iOException.getMessage());
                break;
            }
            try {
                LanServerPinger.sleep(1500L);
            } catch (InterruptedException interruptedException) {}
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        this.isRunning = false;
    }

    public static String createPingString(String string, String string2) {
        return "[MOTD]" + string + "[/MOTD][AD]" + string2 + "[/AD]";
    }

    public static String parseMotd(String string) {
        int i = string.indexOf("[MOTD]");
        if (i < 0) {
            return "missing no";
        }
        int j = string.indexOf("[/MOTD]", i + "[MOTD]".length());
        if (j < i) {
            return "missing no";
        }
        return string.substring(i + "[MOTD]".length(), j);
    }

    public static String parseAddress(String string) {
        int i = string.indexOf("[/MOTD]");
        if (i < 0) {
            return null;
        }
        int j = string.indexOf("[/MOTD]", i + "[/MOTD]".length());
        if (j >= 0) {
            return null;
        }
        int k = string.indexOf("[AD]", i + "[/MOTD]".length());
        if (k < 0) {
            return null;
        }
        int l = string.indexOf("[/AD]", k + "[AD]".length());
        if (l < k) {
            return null;
        }
        return string.substring(k + "[AD]".length(), l);
    }
}

