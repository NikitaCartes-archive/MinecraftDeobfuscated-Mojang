package net.minecraft.client.multiplayer.resolver;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ServerNameResolver {
	public static final ServerNameResolver DEFAULT = new ServerNameResolver(
		ServerAddressResolver.SYSTEM, ServerRedirectHandler.createDnsSrvRedirectHandler(), AddressCheck.createFromService()
	);
	private final ServerAddressResolver resolver;
	private final ServerRedirectHandler redirectHandler;
	private final AddressCheck addressCheck;

	@VisibleForTesting
	ServerNameResolver(ServerAddressResolver serverAddressResolver, ServerRedirectHandler serverRedirectHandler, AddressCheck addressCheck) {
		this.resolver = serverAddressResolver;
		this.redirectHandler = serverRedirectHandler;
		this.addressCheck = addressCheck;
	}

	public Optional<ResolvedServerAddress> resolveAddress(ServerAddress serverAddress) {
		Optional<ResolvedServerAddress> optional = this.resolver.resolve(serverAddress);
		if ((!optional.isPresent() || this.addressCheck.isAllowed((ResolvedServerAddress)optional.get())) && this.addressCheck.isAllowed(serverAddress)) {
			Optional<ServerAddress> optional2 = this.redirectHandler.lookupRedirect(serverAddress);
			if (optional2.isPresent()) {
				optional = this.resolver.resolve((ServerAddress)optional2.get()).filter(this.addressCheck::isAllowed);
			}

			return optional;
		} else {
			return Optional.empty();
		}
	}
}
