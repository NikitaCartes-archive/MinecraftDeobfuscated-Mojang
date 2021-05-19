/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.resolver;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.resolver.AddressCheck;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddressResolver;
import net.minecraft.client.multiplayer.resolver.ServerRedirectHandler;

@Environment(value=EnvType.CLIENT)
public class ServerNameResolver {
    public static final ServerNameResolver DEFAULT = new ServerNameResolver(ServerAddressResolver.SYSTEM, ServerRedirectHandler.createDnsSrvRedirectHandler(), AddressCheck.createFromService());
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
        if (optional.isPresent() && !this.addressCheck.isAllowed(optional.get()) || !this.addressCheck.isAllowed(serverAddress)) {
            return Optional.empty();
        }
        Optional<ServerAddress> optional2 = this.redirectHandler.lookupRedirect(serverAddress);
        if (optional2.isPresent()) {
            optional = this.resolver.resolve(optional2.get()).filter(this.addressCheck::isAllowed);
        }
        return optional;
    }
}

