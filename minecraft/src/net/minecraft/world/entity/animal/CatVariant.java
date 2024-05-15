package net.minecraft.world.entity.animal;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record CatVariant(ResourceLocation texture) {
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<CatVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.CAT_VARIANT);
	public static final ResourceKey<CatVariant> TABBY = createKey("tabby");
	public static final ResourceKey<CatVariant> BLACK = createKey("black");
	public static final ResourceKey<CatVariant> RED = createKey("red");
	public static final ResourceKey<CatVariant> SIAMESE = createKey("siamese");
	public static final ResourceKey<CatVariant> BRITISH_SHORTHAIR = createKey("british_shorthair");
	public static final ResourceKey<CatVariant> CALICO = createKey("calico");
	public static final ResourceKey<CatVariant> PERSIAN = createKey("persian");
	public static final ResourceKey<CatVariant> RAGDOLL = createKey("ragdoll");
	public static final ResourceKey<CatVariant> WHITE = createKey("white");
	public static final ResourceKey<CatVariant> JELLIE = createKey("jellie");
	public static final ResourceKey<CatVariant> ALL_BLACK = createKey("all_black");

	private static ResourceKey<CatVariant> createKey(String string) {
		return ResourceKey.create(Registries.CAT_VARIANT, new ResourceLocation(string));
	}

	public static CatVariant bootstrap(Registry<CatVariant> registry) {
		register(registry, TABBY, "textures/entity/cat/tabby.png");
		register(registry, BLACK, "textures/entity/cat/black.png");
		register(registry, RED, "textures/entity/cat/red.png");
		register(registry, SIAMESE, "textures/entity/cat/siamese.png");
		register(registry, BRITISH_SHORTHAIR, "textures/entity/cat/british_shorthair.png");
		register(registry, CALICO, "textures/entity/cat/calico.png");
		register(registry, PERSIAN, "textures/entity/cat/persian.png");
		register(registry, RAGDOLL, "textures/entity/cat/ragdoll.png");
		register(registry, WHITE, "textures/entity/cat/white.png");
		register(registry, JELLIE, "textures/entity/cat/jellie.png");
		return register(registry, ALL_BLACK, "textures/entity/cat/all_black.png");
	}

	private static CatVariant register(Registry<CatVariant> registry, ResourceKey<CatVariant> resourceKey, String string) {
		return Registry.register(registry, resourceKey, new CatVariant(new ResourceLocation(string)));
	}
}
