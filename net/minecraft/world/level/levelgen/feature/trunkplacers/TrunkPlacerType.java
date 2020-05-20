/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

public class TrunkPlacerType<P extends TrunkPlacer> {
    public static final TrunkPlacerType<StraightTrunkPlacer> STRAIGHT_TRUNK_PLACER = TrunkPlacerType.register("straight_trunk_placer", StraightTrunkPlacer.CODEC);
    public static final TrunkPlacerType<ForkingTrunkPlacer> FORKING_TRUNK_PLACER = TrunkPlacerType.register("forking_trunk_placer", ForkingTrunkPlacer.CODEC);
    public static final TrunkPlacerType<GiantTrunkPlacer> GIANT_TRUNK_PLACER = TrunkPlacerType.register("giant_trunk_placer", GiantTrunkPlacer.CODEC);
    public static final TrunkPlacerType<MegaJungleTrunkPlacer> MEGA_JUNGLE_TRUNK_PLACER = TrunkPlacerType.register("mega_jungle_trunk_placer", MegaJungleTrunkPlacer.CODEC);
    public static final TrunkPlacerType<DarkOakTrunkPlacer> DARK_OAK_TRUNK_PLACER = TrunkPlacerType.register("dark_oak_trunk_placer", DarkOakTrunkPlacer.CODEC);
    public static final TrunkPlacerType<FancyTrunkPlacer> FANCY_TRUNK_PLACER = TrunkPlacerType.register("fancy_trunk_placer", FancyTrunkPlacer.CODEC);
    private final Codec<P> codec;

    private static <P extends TrunkPlacer> TrunkPlacerType<P> register(String string, Codec<P> codec) {
        return Registry.register(Registry.TRUNK_PLACER_TYPES, string, new TrunkPlacerType<P>(codec));
    }

    private TrunkPlacerType(Codec<P> codec) {
        this.codec = codec;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}

