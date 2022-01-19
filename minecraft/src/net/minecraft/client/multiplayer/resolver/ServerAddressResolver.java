package net.minecraft.client.multiplayer.resolver;

import com.mojang.logging.LogUtils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ServerAddressResolver {
	Logger LOGGER = LogUtils.getLogger();
	ServerAddressResolver SYSTEM = serverAddress -> {
		try {
			InetAddress inetAddress = InetAddress.getByName(serverAddress.getHost());
			return Optional.of(ResolvedServerAddress.from(new InetSocketAddress(inetAddress, serverAddress.getPort())));
		} catch (UnknownHostException var2) {
			LOGGER.debug("Couldn't resolve server {} address", serverAddress.getHost(), var2);
			return Optional.empty();
		}
	};

	Optional<ResolvedServerAddress> resolve(ServerAddress serverAddress);
}
