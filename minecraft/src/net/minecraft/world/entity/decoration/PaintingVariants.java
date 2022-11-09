package net.minecraft.world.entity.decoration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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

	public static PaintingVariant bootstrap(Registry<PaintingVariant> registry) {
		Registry.register(registry, KEBAB, new PaintingVariant(16, 16));
		Registry.register(registry, AZTEC, new PaintingVariant(16, 16));
		Registry.register(registry, ALBAN, new PaintingVariant(16, 16));
		Registry.register(registry, AZTEC2, new PaintingVariant(16, 16));
		Registry.register(registry, BOMB, new PaintingVariant(16, 16));
		Registry.register(registry, PLANT, new PaintingVariant(16, 16));
		Registry.register(registry, WASTELAND, new PaintingVariant(16, 16));
		Registry.register(registry, POOL, new PaintingVariant(32, 16));
		Registry.register(registry, COURBET, new PaintingVariant(32, 16));
		Registry.register(registry, SEA, new PaintingVariant(32, 16));
		Registry.register(registry, SUNSET, new PaintingVariant(32, 16));
		Registry.register(registry, CREEBET, new PaintingVariant(32, 16));
		Registry.register(registry, WANDERER, new PaintingVariant(16, 32));
		Registry.register(registry, GRAHAM, new PaintingVariant(16, 32));
		Registry.register(registry, MATCH, new PaintingVariant(32, 32));
		Registry.register(registry, BUST, new PaintingVariant(32, 32));
		Registry.register(registry, STAGE, new PaintingVariant(32, 32));
		Registry.register(registry, VOID, new PaintingVariant(32, 32));
		Registry.register(registry, SKULL_AND_ROSES, new PaintingVariant(32, 32));
		Registry.register(registry, WITHER, new PaintingVariant(32, 32));
		Registry.register(registry, FIGHTERS, new PaintingVariant(64, 32));
		Registry.register(registry, POINTER, new PaintingVariant(64, 64));
		Registry.register(registry, PIGSCENE, new PaintingVariant(64, 64));
		Registry.register(registry, BURNING_SKULL, new PaintingVariant(64, 64));
		Registry.register(registry, SKELETON, new PaintingVariant(64, 48));
		Registry.register(registry, EARTH, new PaintingVariant(32, 32));
		Registry.register(registry, WIND, new PaintingVariant(32, 32));
		Registry.register(registry, WATER, new PaintingVariant(32, 32));
		Registry.register(registry, FIRE, new PaintingVariant(32, 32));
		return Registry.register(registry, DONKEY_KONG, new PaintingVariant(64, 48));
	}

	private static ResourceKey<PaintingVariant> create(String string) {
		return ResourceKey.create(Registries.PAINTING_VARIANT, new ResourceLocation(string));
	}
}
