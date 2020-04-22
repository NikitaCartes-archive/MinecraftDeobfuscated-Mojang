/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

public class TrunkPlacerType<P extends TrunkPlacer> {
    public static final TrunkPlacerType<StraightTrunkPlacer> STRAIGHT_TRUNK_PLACER = TrunkPlacerType.register("straight_trunk_placer", StraightTrunkPlacer::new);
    public static final TrunkPlacerType<ForkingTrunkPlacer> FORKING_TRUNK_PLACER = TrunkPlacerType.register("forking_trunk_placer", ForkingTrunkPlacer::new);
    public static final TrunkPlacerType<GiantTrunkPlacer> GIANT_TRUNK_PLACER = TrunkPlacerType.register("giant_trunk_placer", GiantTrunkPlacer::new);
    public static final TrunkPlacerType<MegaJungleTrunkPlacer> MEGA_JUNGLE_TRUNK_PLACER = TrunkPlacerType.register("mega_jungle_trunk_placer", MegaJungleTrunkPlacer::new);
    public static final TrunkPlacerType<DarkOakTrunkPlacer> DARK_OAK_TRUNK_PLACER = TrunkPlacerType.register("dark_oak_trunk_placer", DarkOakTrunkPlacer::new);
    public static final TrunkPlacerType<FancyTrunkPlacer> FANCY_TRUNK_PLACER = TrunkPlacerType.register("fancy_trunk_placer", FancyTrunkPlacer::new);
    private final Function<Dynamic<?>, P> deserializer;

    private static <P extends TrunkPlacer> TrunkPlacerType<P> register(String string, Function<Dynamic<?>, P> function) {
        return Registry.register(Registry.TRUNK_PLACER_TYPES, string, new TrunkPlacerType<P>(function));
    }

    private TrunkPlacerType(Function<Dynamic<?>, P> function) {
        this.deserializer = function;
    }

    public P deserialize(Dynamic<?> dynamic) {
        return (P)((TrunkPlacer)this.deserializer.apply(dynamic));
    }
}

