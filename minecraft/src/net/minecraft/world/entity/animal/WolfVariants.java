package net.minecraft.world.entity.animal;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class WolfVariants {
	public static final ResourceKey<WolfVariant> PALE = createKey("pale");
	public static final ResourceKey<WolfVariant> SPOTTED = createKey("spotted");
	public static final ResourceKey<WolfVariant> SNOWY = createKey("snowy");
	public static final ResourceKey<WolfVariant> BLACK = createKey("black");
	public static final ResourceKey<WolfVariant> ASHEN = createKey("ashen");
	public static final ResourceKey<WolfVariant> RUSTY = createKey("rusty");
	public static final ResourceKey<WolfVariant> WOODS = createKey("woods");
	public static final ResourceKey<WolfVariant> CHESTNUT = createKey("chestnut");
	public static final ResourceKey<WolfVariant> STRIPED = createKey("striped");

	private static ResourceKey<WolfVariant> createKey(String string) {
		return ResourceKey.create(Registries.WOLF_VARIANT, new ResourceLocation(string));
	}

	static void register(BootstrapContext<WolfVariant> bootstrapContext, ResourceKey<WolfVariant> resourceKey, String string, ResourceKey<Biome> resourceKey2) {
		ResourceLocation resourceLocation = new ResourceLocation("textures/entity/wolf/" + string + ".png");
		ResourceLocation resourceLocation2 = new ResourceLocation("textures/entity/wolf/" + string + "_tame.png");
		ResourceLocation resourceLocation3 = new ResourceLocation("textures/entity/wolf/" + string + "_angry.png");
		bootstrapContext.register(
			resourceKey,
			new WolfVariant(resourceLocation, resourceLocation2, resourceLocation3, HolderSet.direct(bootstrapContext.lookup(Registries.BIOME).getOrThrow(resourceKey2)))
		);
	}

	public static Holder<WolfVariant> getSpawnVariant(RegistryAccess registryAccess, Holder<Biome> holder) {
		Registry<WolfVariant> registry = registryAccess.registryOrThrow(Registries.WOLF_VARIANT);
		return (Holder<WolfVariant>)registry.holders()
			.filter(reference -> ((WolfVariant)reference.value()).biomes().contains(holder))
			.findFirst()
			.orElse(registry.getHolderOrThrow(PALE));
	}

	public static void bootstrap(BootstrapContext<WolfVariant> bootstrapContext) {
		register(bootstrapContext, PALE, "wolf", Biomes.TAIGA);
		register(bootstrapContext, SPOTTED, "wolf_spotted", Biomes.SAVANNA_PLATEAU);
		register(bootstrapContext, SNOWY, "wolf_snowy", Biomes.GROVE);
		register(bootstrapContext, BLACK, "wolf_black", Biomes.OLD_GROWTH_PINE_TAIGA);
		register(bootstrapContext, ASHEN, "wolf_ashen", Biomes.SNOWY_TAIGA);
		register(bootstrapContext, RUSTY, "wolf_rusty", Biomes.SPARSE_JUNGLE);
		register(bootstrapContext, WOODS, "wolf_woods", Biomes.FOREST);
		register(bootstrapContext, CHESTNUT, "wolf_chestnut", Biomes.OLD_GROWTH_SPRUCE_TAIGA);
		register(bootstrapContext, STRIPED, "wolf_striped", Biomes.WOODED_BADLANDS);
	}
}
