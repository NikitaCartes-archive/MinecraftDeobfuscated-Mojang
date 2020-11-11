package net.minecraft.world.level.storage.loot.providers.nbt;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

public class NbtProviders {
	public static final LootNbtProviderType STORAGE = register("storage", new StorageNbtProvider.Serializer());
	public static final LootNbtProviderType CONTEXT = register("context", new ContextNbtProvider.Serializer());

	private static LootNbtProviderType register(String string, Serializer<? extends NbtProvider> serializer) {
		return Registry.register(Registry.LOOT_NBT_PROVIDER_TYPE, new ResourceLocation(string), new LootNbtProviderType(serializer));
	}

	public static Object createGsonAdapter() {
		return GsonAdapterFactory.<NbtProvider, LootNbtProviderType>builder(Registry.LOOT_NBT_PROVIDER_TYPE, "provider", "type", NbtProvider::getType)
			.withDefaultSerializer(CONTEXT, new ContextNbtProvider.DefaultSerializer())
			.build();
	}
}
