package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class NbtProviders {
	private static final Codec<NbtProvider> TYPED_CODEC = BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE
		.byNameCodec()
		.dispatch(NbtProvider::getType, LootNbtProviderType::codec);
	public static final Codec<NbtProvider> CODEC = ExtraCodecs.lazyInitializedCodec(
		() -> Codec.either(ContextNbtProvider.INLINE_CODEC, TYPED_CODEC)
				.xmap(
					either -> either.map(Function.identity(), Function.identity()),
					nbtProvider -> nbtProvider instanceof ContextNbtProvider contextNbtProvider ? Either.left(contextNbtProvider) : Either.right(nbtProvider)
				)
	);
	public static final LootNbtProviderType STORAGE = register("storage", StorageNbtProvider.CODEC);
	public static final LootNbtProviderType CONTEXT = register("context", ContextNbtProvider.CODEC);

	private static LootNbtProviderType register(String string, Codec<? extends NbtProvider> codec) {
		return Registry.register(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, new ResourceLocation(string), new LootNbtProviderType(codec));
	}
}
