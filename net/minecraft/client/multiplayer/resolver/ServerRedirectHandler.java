/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.resolver;

import java.util.Hashtable;
import java.util.Optional;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface ServerRedirectHandler {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ServerRedirectHandler EMPTY = serverAddress -> Optional.empty();

    public Optional<ServerAddress> lookupRedirect(ServerAddress var1);

    public static ServerRedirectHandler createDnsSrvRedirectHandler() {
        InitialDirContext dirContext;
        try {
            String string = "com.sun.jndi.dns.DnsContextFactory";
            Class.forName("com.sun.jndi.dns.DnsContextFactory");
            Hashtable<String, String> hashtable = new Hashtable<String, String>();
            hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            hashtable.put("java.naming.provider.url", "dns:");
            hashtable.put("com.sun.jndi.dns.timeout.retries", "1");
            dirContext = new InitialDirContext(hashtable);
        } catch (Throwable throwable) {
            LOGGER.error("Failed to initialize SRV redirect resolved, some servers might not work", throwable);
            return EMPTY;
        }
        return serverAddress -> {
            if (serverAddress.getPort() == 25565) {
                try {
                    Attributes attributes = dirContext.getAttributes("_minecraft._tcp." + serverAddress.getHost(), new String[]{"SRV"});
                    Attribute attribute = attributes.get("srv");
                    if (attribute != null) {
                        String[] strings = attribute.get().toString().split(" ", 4);
                        return Optional.of(new ServerAddress(strings[3], ServerAddress.parsePort(strings[2])));
                    }
                } catch (Throwable throwable) {
                    // empty catch block
                }
            }
            return Optional.empty();
        };
    }
}

