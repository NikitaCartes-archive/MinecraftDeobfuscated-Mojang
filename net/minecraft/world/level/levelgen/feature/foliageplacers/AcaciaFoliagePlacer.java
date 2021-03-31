/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class AcaciaFoliagePlacer
extends FoliagePlacer {
    public static final Codec<AcaciaFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> AcaciaFoliagePlacer.foliagePlacerParts(instance).apply(instance, AcaciaFoliagePlacer::new));

    public AcaciaFoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
        super(intProvider, intProvider2);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.ACACIA_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int j, int k, int l) {
        boolean bl = foliageAttachment.doubleTrunk();
        BlockPos blockPos = foliageAttachment.pos().above(l);
        this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, k + foliageAttachment.radiusOffset(), -1 - j, bl);
        this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, k - 1, -j, bl);
        this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, k + foliageAttachment.radiusOffset() - 1, 0, bl);
    }

    @Override
    public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
        if (j == 0) {
            return (i > 1 || k > 1) && i != 0 && k != 0;
        }
        return i == l && k == l && l > 0;
    }
}

