package net.minecraft.client.multiplayer.resolver;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mojang.blocklist.BlockListSupplier;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ServerNameResolver {
	public static final ServerNameResolver DEFAULT = new ServerNameResolver(
		ServerAddressResolver.SYSTEM, ServerRedirectHandler.createDnsSrvRedirectHandler(), createBlockCheckFromService()
	);
	private final ServerAddressResolver resolver;
	private final ServerRedirectHandler redirectHandler;
	private final Predicate<ResolvedServerAddress> allowCheck;

	@VisibleForTesting
	ServerNameResolver(ServerAddressResolver serverAddressResolver, ServerRedirectHandler serverRedirectHandler, Predicate<ResolvedServerAddress> predicate) {
		this.resolver = serverAddressResolver;
		this.redirectHandler = serverRedirectHandler;
		this.allowCheck = predicate.negate();
	}

	private static Predicate<ResolvedServerAddress> createBlockCheckFromService() {
		ImmutableList<Predicate<String>> immutableList = (ImmutableList<Predicate<String>>)Streams.stream(ServiceLoader.load(BlockListSupplier.class))
			.map(BlockListSupplier::createBlockList)
			.filter(Objects::nonNull)
			.collect(ImmutableList.toImmutableList());
		return resolvedServerAddress -> immutableList.stream()
				.anyMatch(predicate -> predicate.test(resolvedServerAddress.getHostName()) || predicate.test(resolvedServerAddress.getHostIp()));
	}

	public Optional<ResolvedServerAddress> resolveAddress(ServerAddress serverAddress) {
		Optional<ResolvedServerAddress> optional = this.resolveAndFilter(serverAddress);
		if (!optional.isPresent()) {
			return Optional.empty();
		} else {
			Optional<ServerAddress> optional2 = this.redirectHandler.lookupRedirect(serverAddress);
			if (optional2.isPresent()) {
				optional = this.resolveAndFilter((ServerAddress)optional2.get());
			}

			return optional;
		}
	}

	private Optional<ResolvedServerAddress> resolveAndFilter(ServerAddress serverAddress) {
		return this.resolver.resolve(serverAddress).filter(this.allowCheck);
	}
}
