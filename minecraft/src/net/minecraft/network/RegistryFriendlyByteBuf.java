package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.core.RegistryAccess;

public class RegistryFriendlyByteBuf extends FriendlyByteBuf {
	private final RegistryAccess registryAccess;

	public RegistryFriendlyByteBuf(ByteBuf byteBuf, RegistryAccess registryAccess) {
		super(byteBuf);
		this.registryAccess = registryAccess;
	}

	public RegistryAccess registryAccess() {
		return this.registryAccess;
	}

	public static Function<ByteBuf, RegistryFriendlyByteBuf> decorator(RegistryAccess registryAccess) {
		return byteBuf -> new RegistryFriendlyByteBuf(byteBuf, registryAccess);
	}
}
