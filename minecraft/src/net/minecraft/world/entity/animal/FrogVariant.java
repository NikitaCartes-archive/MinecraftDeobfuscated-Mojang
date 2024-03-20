package net.minecraft.world.entity.animal;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record FrogVariant(ResourceLocation texture) {
	public static final ResourceKey<FrogVariant> TEMPERATE = createKey("temperate");
	public static final ResourceKey<FrogVariant> WARM = createKey("warm");
	public static final ResourceKey<FrogVariant> COLD = createKey("cold");

	private static ResourceKey<FrogVariant> createKey(String string) {
		return ResourceKey.create(Registries.FROG_VARIANT, new ResourceLocation(string));
	}

	public static FrogVariant bootstrap(Registry<FrogVariant> registry) {
		register(registry, TEMPERATE, "textures/entity/frog/temperate_frog.png");
		register(registry, WARM, "textures/entity/frog/warm_frog.png");
		return register(registry, COLD, "textures/entity/frog/cold_frog.png");
	}

	private static FrogVariant register(Registry<FrogVariant> registry, ResourceKey<FrogVariant> resourceKey, String string) {
		return Registry.register(registry, resourceKey, new FrogVariant(new ResourceLocation(string)));
	}
}
