/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class FancyFoliagePlacer
extends BlobFoliagePlacer {
    public static final Codec<FancyFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> FancyFoliagePlacer.blobParts(instance).apply(instance, FancyFoliagePlacer::new));

    public FancyFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, int i) {
        super(intProvider, intProvider2, i);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.FANCY_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, FoliagePlacer.FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int j, int k, int l) {
        for (int m = l; m >= l - j; --m) {
            int n = k + (m == l || m == l - j ? 0 : 1);
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, foliageAttachment.pos(), n, m, foliageAttachment.doubleTrunk());
        }
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
        return Mth.square((float)i + 0.5f) + Mth.square((float)k + 0.5f) > (float)(l * l);
    }
}

