/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.resolver;

import com.google.common.net.HostAndPort;
import com.mojang.logging.LogUtils;
import java.net.IDN;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public final class ServerAddress {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final HostAndPort hostAndPort;
    private static final ServerAddress INVALID = new ServerAddress(HostAndPort.fromParts("server.invalid", 25565));

    public ServerAddress(String string, int i) {
        this(HostAndPort.fromParts(string, i));
    }

    private ServerAddress(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public String getHost() {
        try {
            return IDN.toASCII(this.hostAndPort.getHost());
        } catch (IllegalArgumentException illegalArgumentException) {
            return "";
        }
    }

    public int getPort() {
        return this.hostAndPort.getPort();
    }

    public static ServerAddress parseString(String string) {
        if (string == null) {
            return INVALID;
        }
        try {
            HostAndPort hostAndPort = HostAndPort.fromString(string).withDefaultPort(25565);
            if (hostAndPort.getHost().isEmpty()) {
                return INVALID;
            }
            return new ServerAddress(hostAndPort);
        } catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.info("Failed to parse URL {}", (Object)string, (Object)illegalArgumentException);
            return INVALID;
        }
    }

    public static boolean isValidAddress(String string) {
        try {
            HostAndPort hostAndPort = HostAndPort.fromString(string);
            String string2 = hostAndPort.getHost();
            if (!string2.isEmpty()) {
                IDN.toASCII(string2);
                return true;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        return false;
    }

    static int parsePort(String string) {
        try {
            return Integer.parseInt(string.trim());
        } catch (Exception exception) {
            return 25565;
        }
    }

    public String toString() {
        return this.hostAndPort.toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ServerAddress) {
            return this.hostAndPort.equals(((ServerAddress)object).hostAndPort);
        }
        return false;
    }

    public int hashCode() {
        return this.hostAndPort.hashCode();
    }
}

