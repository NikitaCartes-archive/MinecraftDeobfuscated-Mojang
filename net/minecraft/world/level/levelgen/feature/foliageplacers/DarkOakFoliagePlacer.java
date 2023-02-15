/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class DarkOakFoliagePlacer
extends FoliagePlacer {
    public static final Codec<DarkOakFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> DarkOakFoliagePlacer.foliagePlacerParts(instance).apply(instance, DarkOakFoliagePlacer::new));

    public DarkOakFoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
        super(intProvider, intProvider2);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.DARK_OAK_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, FoliagePlacer.FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int j, int k, int l) {
        BlockPos blockPos = foliageAttachment.pos().above(l);
        boolean bl = foliageAttachment.doubleTrunk();
        if (bl) {
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, k + 2, -1, bl);
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, k + 3, 0, bl);
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, k + 2, 1, bl);
            if (randomSource.nextBoolean()) {
                this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, k, 2, bl);
            }
        } else {
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, k + 2, -1, bl);
            this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, k + 1, 0, bl);
        }
    }

    @Override
    public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
        return 4;
    }

    @Override
    protected boolean shouldSkipLocationSigned(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
        if (!(j != 0 || !bl || i != -l && i < l || k != -l && k < l)) {
            return true;
        }
        return super.shouldSkipLocationSigned(randomSource, i, j, k, l, bl);
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
        if (j == -1 && !bl) {
            return i == l && k == l;
        }
        if (j == 1) {
            return i + k > l * 2 - 2;
        }
        return false;
    }
}

