/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.decoration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class PaintingVariants {
    public static final ResourceKey<PaintingVariant> KEBAB = PaintingVariants.create("kebab");
    public static final ResourceKey<PaintingVariant> AZTEC = PaintingVariants.create("aztec");
    public static final ResourceKey<PaintingVariant> ALBAN = PaintingVariants.create("alban");
    public static final ResourceKey<PaintingVariant> AZTEC2 = PaintingVariants.create("aztec2");
    public static final ResourceKey<PaintingVariant> BOMB = PaintingVariants.create("bomb");
    public static final ResourceKey<PaintingVariant> PLANT = PaintingVariants.create("plant");
    public static final ResourceKey<PaintingVariant> WASTELAND = PaintingVariants.create("wasteland");
    public static final ResourceKey<PaintingVariant> POOL = PaintingVariants.create("pool");
    public static final ResourceKey<PaintingVariant> COURBET = PaintingVariants.create("courbet");
    public static final ResourceKey<PaintingVariant> SEA = PaintingVariants.create("sea");
    public static final ResourceKey<PaintingVariant> SUNSET = PaintingVariants.create("sunset");
    public static final ResourceKey<PaintingVariant> CREEBET = PaintingVariants.create("creebet");
    public static final ResourceKey<PaintingVariant> WANDERER = PaintingVariants.create("wanderer");
    public static final ResourceKey<PaintingVariant> GRAHAM = PaintingVariants.create("graham");
    public static final ResourceKey<PaintingVariant> MATCH = PaintingVariants.create("match");
    public static final ResourceKey<PaintingVariant> BUST = PaintingVariants.create("bust");
    public static final ResourceKey<PaintingVariant> STAGE = PaintingVariants.create("stage");
    public static final ResourceKey<PaintingVariant> VOID = PaintingVariants.create("void");
    public static final ResourceKey<PaintingVariant> SKULL_AND_ROSES = PaintingVariants.create("skull_and_roses");
    public static final ResourceKey<PaintingVariant> WITHER = PaintingVariants.create("wither");
    public static final ResourceKey<PaintingVariant> FIGHTERS = PaintingVariants.create("fighters");
    public static final ResourceKey<PaintingVariant> POINTER = PaintingVariants.create("pointer");
    public static final ResourceKey<PaintingVariant> PIGSCENE = PaintingVariants.create("pigscene");
    public static final ResourceKey<PaintingVariant> BURNING_SKULL = PaintingVariants.create("burning_skull");
    public static final ResourceKey<PaintingVariant> SKELETON = PaintingVariants.create("skeleton");
    public static final ResourceKey<PaintingVariant> DONKEY_KONG = PaintingVariants.create("donkey_kong");
    public static final ResourceKey<PaintingVariant> EARTH = PaintingVariants.create("earth");
    public static final ResourceKey<PaintingVariant> WIND = PaintingVariants.create("wind");
    public static final ResourceKey<PaintingVariant> WATER = PaintingVariants.create("water");
    public static final ResourceKey<PaintingVariant> FIRE = PaintingVariants.create("fire");

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
        return ResourceKey.create(Registry.PAINTING_VARIANT_REGISTRY, new ResourceLocation(string));
    }
}

