/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class RandomSpreadFoliagePlacer
extends FoliagePlacer {
    public static final Codec<RandomSpreadFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> RandomSpreadFoliagePlacer.foliagePlacerParts(instance).and(instance.group(((MapCodec)IntProvider.codec(1, 512).fieldOf("foliage_height")).forGetter(randomSpreadFoliagePlacer -> randomSpreadFoliagePlacer.foliageHeight), ((MapCodec)Codec.intRange(0, 256).fieldOf("leaf_placement_attempts")).forGetter(randomSpreadFoliagePlacer -> randomSpreadFoliagePlacer.leafPlacementAttempts))).apply((Applicative<RandomSpreadFoliagePlacer, ?>)instance, RandomSpreadFoliagePlacer::new));
    private final IntProvider foliageHeight;
    private final int leafPlacementAttempts;

    public RandomSpreadFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3, int i) {
        super(intProvider, intProvider2);
        this.foliageHeight = intProvider3;
        this.leafPlacementAttempts = i;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return FoliagePlacerType.RANDOM_SPREAD_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, TreeConfiguration treeConfiguration, int i, FoliagePlacer.FoliageAttachment foliageAttachment, int j, int k, int l) {
        BlockPos blockPos = foliageAttachment.pos();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (int m = 0; m < this.leafPlacementAttempts; ++m) {
            mutableBlockPos.setWithOffset(blockPos, randomSource.nextInt(k) - randomSource.nextInt(k), randomSource.nextInt(j) - randomSource.nextInt(j), randomSource.nextInt(k) - randomSource.nextInt(k));
            RandomSpreadFoliagePlacer.tryPlaceLeaf(levelSimulatedReader, biConsumer, randomSource, treeConfiguration, mutableBlockPos);
        }
    }

    @Override
    public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
        return this.foliageHeight.sample(randomSource);
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
        return false;
    }
}

