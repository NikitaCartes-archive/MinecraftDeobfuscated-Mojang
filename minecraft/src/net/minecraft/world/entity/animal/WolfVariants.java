package net.minecraft.world.entity.animal;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
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
		register(bootstrapContext, resourceKey, string, HolderSet.direct(bootstrapContext.lookup(Registries.BIOME).getOrThrow(resourceKey2)));
	}

	static void register(BootstrapContext<WolfVariant> bootstrapContext, ResourceKey<WolfVariant> resourceKey, String string, TagKey<Biome> tagKey) {
		register(bootstrapContext, resourceKey, string, bootstrapContext.lookup(Registries.BIOME).getOrThrow(tagKey));
	}

	static void register(BootstrapContext<WolfVariant> bootstrapContext, ResourceKey<WolfVariant> resourceKey, String string, HolderSet<Biome> holderSet) {
		ResourceLocation resourceLocation = new ResourceLocation("entity/wolf/" + string);
		ResourceLocation resourceLocation2 = new ResourceLocation("entity/wolf/" + string + "_tame");
		ResourceLocation resourceLocation3 = new ResourceLocation("entity/wolf/" + string + "_angry");
		bootstrapContext.register(resourceKey, new WolfVariant(resourceLocation, resourceLocation2, resourceLocation3, holderSet));
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
		register(bootstrapContext, SPOTTED, "wolf_spotted", BiomeTags.IS_SAVANNA);
		register(bootstrapContext, SNOWY, "wolf_snowy", Biomes.GROVE);
		register(bootstrapContext, BLACK, "wolf_black", Biomes.OLD_GROWTH_PINE_TAIGA);
		register(bootstrapContext, ASHEN, "wolf_ashen", Biomes.SNOWY_TAIGA);
		register(bootstrapContext, RUSTY, "wolf_rusty", BiomeTags.IS_JUNGLE);
		register(bootstrapContext, WOODS, "wolf_woods", Biomes.FOREST);
		register(bootstrapContext, CHESTNUT, "wolf_chestnut", Biomes.OLD_GROWTH_SPRUCE_TAIGA);
		register(bootstrapContext, STRIPED, "wolf_striped", BiomeTags.IS_BADLANDS);
	}
}
