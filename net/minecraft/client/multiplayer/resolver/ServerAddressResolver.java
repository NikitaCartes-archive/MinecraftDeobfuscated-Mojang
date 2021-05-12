/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.resolver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface ServerAddressResolver {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ServerAddressResolver SYSTEM = serverAddress -> {
        try {
            InetAddress inetAddress = InetAddress.getByName(serverAddress.getHost());
            return Optional.of(ResolvedServerAddress.from(new InetSocketAddress(inetAddress, serverAddress.getPort())));
        } catch (UnknownHostException unknownHostException) {
            LOGGER.debug("Couldn't resolve server {} address", (Object)serverAddress.getHost(), (Object)unknownHostException);
            return Optional.empty();
        }
    };

    public Optional<ResolvedServerAddress> resolve(ServerAddress var1);
}

