package net.minecraft.client.multiplayer;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;

@Environment(EnvType.CLIENT)
public enum ClientRegistryLayer {
	STATIC,
	REMOTE;

	private static final List<ClientRegistryLayer> VALUES = List.of(values());
	private static final RegistryAccess.Frozen STATIC_ACCESS = RegistryAccess.fromRegistryOfRegistries(Registry.REGISTRY);

	public static LayeredRegistryAccess<ClientRegistryLayer> createRegistryAccess() {
		return new LayeredRegistryAccess<ClientRegistryLayer>(VALUES).replaceFrom(STATIC, STATIC_ACCESS);
	}
}
