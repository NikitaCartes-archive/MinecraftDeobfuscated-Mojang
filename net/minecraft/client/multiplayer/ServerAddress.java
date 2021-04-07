/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer;

import com.google.common.net.HostAndPort;
import java.net.IDN;
import java.util.Hashtable;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ServerAddress {
    private static final Logger LOGGER = LogManager.getLogger();
    private final HostAndPort hostAndPort;
    private static final ServerAddress INVALID = new ServerAddress(HostAndPort.fromParts("server.invalid", 25565));

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
        HostAndPort hostAndPort;
        if (string == null) {
            return INVALID;
        }
        try {
            hostAndPort = HostAndPort.fromString(string).withDefaultPort(25565);
            if (hostAndPort.getHost().isEmpty()) {
                return INVALID;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.info("Failed to parse URL {}", (Object)string, (Object)illegalArgumentException);
            return INVALID;
        }
        return new ServerAddress(ServerAddress.lookupSrv(hostAndPort));
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

    private static HostAndPort lookupSrv(HostAndPort hostAndPort) {
        if (hostAndPort.getPort() != 25565) {
            return hostAndPort;
        }
        try {
            String string = "com.sun.jndi.dns.DnsContextFactory";
            Class.forName("com.sun.jndi.dns.DnsContextFactory");
            Hashtable<String, String> hashtable = new Hashtable<String, String>();
            hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            hashtable.put("java.naming.provider.url", "dns:");
            hashtable.put("com.sun.jndi.dns.timeout.retries", "1");
            InitialDirContext dirContext = new InitialDirContext(hashtable);
            Attributes attributes = dirContext.getAttributes("_minecraft._tcp." + hostAndPort.getHost(), new String[]{"SRV"});
            Attribute attribute = attributes.get("srv");
            if (attribute != null) {
                String[] strings = attribute.get().toString().split(" ", 4);
                return HostAndPort.fromParts(strings[3], ServerAddress.parseInt(strings[2], 25565));
            }
        } catch (Throwable throwable) {
            // empty catch block
        }
        return hostAndPort;
    }

    private static int parseInt(String string, int i) {
        try {
            return Integer.parseInt(string.trim());
        } catch (Exception exception) {
            return i;
        }
    }
}

