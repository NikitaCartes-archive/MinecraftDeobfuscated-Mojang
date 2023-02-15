/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BushFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.DarkOakFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.PineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.RandomSpreadFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;

public class FoliagePlacerType<P extends FoliagePlacer> {
    public static final FoliagePlacerType<BlobFoliagePlacer> BLOB_FOLIAGE_PLACER = FoliagePlacerType.register("blob_foliage_placer", BlobFoliagePlacer.CODEC);
    public static final FoliagePlacerType<SpruceFoliagePlacer> SPRUCE_FOLIAGE_PLACER = FoliagePlacerType.register("spruce_foliage_placer", SpruceFoliagePlacer.CODEC);
    public static final FoliagePlacerType<PineFoliagePlacer> PINE_FOLIAGE_PLACER = FoliagePlacerType.register("pine_foliage_placer", PineFoliagePlacer.CODEC);
    public static final FoliagePlacerType<AcaciaFoliagePlacer> ACACIA_FOLIAGE_PLACER = FoliagePlacerType.register("acacia_foliage_placer", AcaciaFoliagePlacer.CODEC);
    public static final FoliagePlacerType<BushFoliagePlacer> BUSH_FOLIAGE_PLACER = FoliagePlacerType.register("bush_foliage_placer", BushFoliagePlacer.CODEC);
    public static final FoliagePlacerType<FancyFoliagePlacer> FANCY_FOLIAGE_PLACER = FoliagePlacerType.register("fancy_foliage_placer", FancyFoliagePlacer.CODEC);
    public static final FoliagePlacerType<MegaJungleFoliagePlacer> MEGA_JUNGLE_FOLIAGE_PLACER = FoliagePlacerType.register("jungle_foliage_placer", MegaJungleFoliagePlacer.CODEC);
    public static final FoliagePlacerType<MegaPineFoliagePlacer> MEGA_PINE_FOLIAGE_PLACER = FoliagePlacerType.register("mega_pine_foliage_placer", MegaPineFoliagePlacer.CODEC);
    public static final FoliagePlacerType<DarkOakFoliagePlacer> DARK_OAK_FOLIAGE_PLACER = FoliagePlacerType.register("dark_oak_foliage_placer", DarkOakFoliagePlacer.CODEC);
    public static final FoliagePlacerType<RandomSpreadFoliagePlacer> RANDOM_SPREAD_FOLIAGE_PLACER = FoliagePlacerType.register("random_spread_foliage_placer", RandomSpreadFoliagePlacer.CODEC);
    public static final FoliagePlacerType<CherryFoliagePlacer> CHERRY_FOLIAGE_PLACER = FoliagePlacerType.register("cherry_foliage_placer", CherryFoliagePlacer.CODEC);
    private final Codec<P> codec;

    private static <P extends FoliagePlacer> FoliagePlacerType<P> register(String string, Codec<P> codec) {
        return Registry.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE, string, new FoliagePlacerType<P>(codec));
    }

    private FoliagePlacerType(Codec<P> codec) {
        this.codec = codec;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}

