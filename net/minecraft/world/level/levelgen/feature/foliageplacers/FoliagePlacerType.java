/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.PineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;

public class FoliagePlacerType<P extends FoliagePlacer> {
    public static final FoliagePlacerType<BlobFoliagePlacer> BLOB_FOLIAGE_PLACER = FoliagePlacerType.register("blob_foliage_placer", BlobFoliagePlacer::new);
    public static final FoliagePlacerType<SpruceFoliagePlacer> SPRUCE_FOLIAGE_PLACER = FoliagePlacerType.register("spruce_foliage_placer", SpruceFoliagePlacer::new);
    public static final FoliagePlacerType<PineFoliagePlacer> PINE_FOLIAGE_PLACER = FoliagePlacerType.register("pine_foliage_placer", PineFoliagePlacer::new);
    public static final FoliagePlacerType<AcaciaFoliagePlacer> ACACIA_FOLIAGE_PLACER = FoliagePlacerType.register("acacia_foliage_placer", AcaciaFoliagePlacer::new);
    private final Function<Dynamic<?>, P> deserializer;

    private static <P extends FoliagePlacer> FoliagePlacerType<P> register(String string, Function<Dynamic<?>, P> function) {
        return Registry.register(Registry.FOLIAGE_PLACER_TYPES, string, new FoliagePlacerType<P>(function));
    }

    private FoliagePlacerType(Function<Dynamic<?>, P> function) {
        this.deserializer = function;
    }

    public P deserialize(Dynamic<?> dynamic) {
        return (P)((FoliagePlacer)this.deserializer.apply(dynamic));
    }
}

