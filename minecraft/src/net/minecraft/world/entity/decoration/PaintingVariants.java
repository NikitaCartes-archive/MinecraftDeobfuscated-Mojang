package net.minecraft.world.entity.decoration;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class PaintingVariants {
	public static final ResourceKey<PaintingVariant> KEBAB = create("kebab");
	public static final ResourceKey<PaintingVariant> AZTEC = create("aztec");
	public static final ResourceKey<PaintingVariant> ALBAN = create("alban");
	public static final ResourceKey<PaintingVariant> AZTEC2 = create("aztec2");
	public static final ResourceKey<PaintingVariant> BOMB = create("bomb");
	public static final ResourceKey<PaintingVariant> PLANT = create("plant");
	public static final ResourceKey<PaintingVariant> WASTELAND = create("wasteland");
	public static final ResourceKey<PaintingVariant> POOL = create("pool");
	public static final ResourceKey<PaintingVariant> COURBET = create("courbet");
	public static final ResourceKey<PaintingVariant> SEA = create("sea");
	public static final ResourceKey<PaintingVariant> SUNSET = create("sunset");
	public static final ResourceKey<PaintingVariant> CREEBET = create("creebet");
	public static final ResourceKey<PaintingVariant> WANDERER = create("wanderer");
	public static final ResourceKey<PaintingVariant> GRAHAM = create("graham");
	public static final ResourceKey<PaintingVariant> MATCH = create("match");
	public static final ResourceKey<PaintingVariant> BUST = create("bust");
	public static final ResourceKey<PaintingVariant> STAGE = create("stage");
	public static final ResourceKey<PaintingVariant> VOID = create("void");
	public static final ResourceKey<PaintingVariant> SKULL_AND_ROSES = create("skull_and_roses");
	public static final ResourceKey<PaintingVariant> WITHER = create("wither");
	public static final ResourceKey<PaintingVariant> FIGHTERS = create("fighters");
	public static final ResourceKey<PaintingVariant> POINTER = create("pointer");
	public static final ResourceKey<PaintingVariant> PIGSCENE = create("pigscene");
	public static final ResourceKey<PaintingVariant> BURNING_SKULL = create("burning_skull");
	public static final ResourceKey<PaintingVariant> SKELETON = create("skeleton");
	public static final ResourceKey<PaintingVariant> DONKEY_KONG = create("donkey_kong");
	public static final ResourceKey<PaintingVariant> EARTH = create("earth");
	public static final ResourceKey<PaintingVariant> WIND = create("wind");
	public static final ResourceKey<PaintingVariant> WATER = create("water");
	public static final ResourceKey<PaintingVariant> FIRE = create("fire");
	public static final ResourceKey<PaintingVariant> BAROQUE = create("baroque");
	public static final ResourceKey<PaintingVariant> HUMBLE = create("humble");
	public static final ResourceKey<PaintingVariant> MEDITATIVE = create("meditative");
	public static final ResourceKey<PaintingVariant> PRAIRIE_RIDE = create("prairie_ride");
	public static final ResourceKey<PaintingVariant> UNPACKED = create("unpacked");
	public static final ResourceKey<PaintingVariant> BACKYARD = create("backyard");
	public static final ResourceKey<PaintingVariant> BOUQUET = create("bouquet");
	public static final ResourceKey<PaintingVariant> CAVEBIRD = create("cavebird");
	public static final ResourceKey<PaintingVariant> CHANGING = create("changing");
	public static final ResourceKey<PaintingVariant> COTAN = create("cotan");
	public static final ResourceKey<PaintingVariant> ENDBOSS = create("endboss");
	public static final ResourceKey<PaintingVariant> FERN = create("fern");
	public static final ResourceKey<PaintingVariant> FINDING = create("finding");
	public static final ResourceKey<PaintingVariant> LOWMIST = create("lowmist");
	public static final ResourceKey<PaintingVariant> ORB = create("orb");
	public static final ResourceKey<PaintingVariant> OWLEMONS = create("owlemons");
	public static final ResourceKey<PaintingVariant> PASSAGE = create("passage");
	public static final ResourceKey<PaintingVariant> POND = create("pond");
	public static final ResourceKey<PaintingVariant> SUNFLOWERS = create("sunflowers");
	public static final ResourceKey<PaintingVariant> TIDES = create("tides");

	public static void bootstrap(BootstrapContext<PaintingVariant> bootstrapContext) {
		register(bootstrapContext, KEBAB, 1, 1);
		register(bootstrapContext, AZTEC, 1, 1);
		register(bootstrapContext, ALBAN, 1, 1);
		register(bootstrapContext, AZTEC2, 1, 1);
		register(bootstrapContext, BOMB, 1, 1);
		register(bootstrapContext, PLANT, 1, 1);
		register(bootstrapContext, WASTELAND, 1, 1);
		register(bootstrapContext, POOL, 2, 1);
		register(bootstrapContext, COURBET, 2, 1);
		register(bootstrapContext, SEA, 2, 1);
		register(bootstrapContext, SUNSET, 2, 1);
		register(bootstrapContext, CREEBET, 2, 1);
		register(bootstrapContext, WANDERER, 1, 2);
		register(bootstrapContext, GRAHAM, 1, 2);
		register(bootstrapContext, MATCH, 2, 2);
		register(bootstrapContext, BUST, 2, 2);
		register(bootstrapContext, STAGE, 2, 2);
		register(bootstrapContext, VOID, 2, 2);
		register(bootstrapContext, SKULL_AND_ROSES, 2, 2);
		register(bootstrapContext, WITHER, 2, 2);
		register(bootstrapContext, FIGHTERS, 4, 2);
		register(bootstrapContext, POINTER, 4, 4);
		register(bootstrapContext, PIGSCENE, 4, 4);
		register(bootstrapContext, BURNING_SKULL, 4, 4);
		register(bootstrapContext, SKELETON, 4, 3);
		register(bootstrapContext, EARTH, 2, 2);
		register(bootstrapContext, WIND, 2, 2);
		register(bootstrapContext, WATER, 2, 2);
		register(bootstrapContext, FIRE, 2, 2);
		register(bootstrapContext, DONKEY_KONG, 4, 3);
		register(bootstrapContext, BAROQUE, 2, 2);
		register(bootstrapContext, HUMBLE, 2, 2);
		register(bootstrapContext, MEDITATIVE, 1, 1);
		register(bootstrapContext, PRAIRIE_RIDE, 1, 2);
		register(bootstrapContext, UNPACKED, 4, 4);
		register(bootstrapContext, BACKYARD, 3, 4);
		register(bootstrapContext, BOUQUET, 3, 3);
		register(bootstrapContext, CAVEBIRD, 3, 3);
		register(bootstrapContext, CHANGING, 4, 2);
		register(bootstrapContext, COTAN, 3, 3);
		register(bootstrapContext, ENDBOSS, 3, 3);
		register(bootstrapContext, FERN, 3, 3);
		register(bootstrapContext, FINDING, 4, 2);
		register(bootstrapContext, LOWMIST, 4, 2);
		register(bootstrapContext, ORB, 4, 4);
		register(bootstrapContext, OWLEMONS, 3, 3);
		register(bootstrapContext, PASSAGE, 4, 2);
		register(bootstrapContext, POND, 3, 4);
		register(bootstrapContext, SUNFLOWERS, 3, 3);
		register(bootstrapContext, TIDES, 3, 3);
	}

	private static void register(BootstrapContext<PaintingVariant> bootstrapContext, ResourceKey<PaintingVariant> resourceKey, int i, int j) {
		bootstrapContext.register(resourceKey, new PaintingVariant(i, j, resourceKey.location()));
	}

	private static ResourceKey<PaintingVariant> create(String string) {
		return ResourceKey.create(Registries.PAINTING_VARIANT, new ResourceLocation(string));
	}
}
