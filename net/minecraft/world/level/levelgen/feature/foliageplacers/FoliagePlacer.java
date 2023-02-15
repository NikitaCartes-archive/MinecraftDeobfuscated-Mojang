/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.material.Fluids;

public abstract class FoliagePlacer {
    public static final Codec<FoliagePlacer> CODEC = BuiltInRegistries.FOLIAGE_PLACER_TYPE.byNameCodec().dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
    protected final IntProvider radius;
    protected final IntProvider offset;

    protected static <P extends FoliagePlacer> Products.P2<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider> foliagePlacerParts(RecordCodecBuilder.Instance<P> instance) {
        return instance.group(((MapCodec)IntProvider.codec(0, 16).fieldOf("radius")).forGetter(foliagePlacer -> foliagePlacer.radius), ((MapCodec)IntProvider.codec(0, 16).fieldOf("offset")).forGetter(foliagePlacer -> foliagePlacer.offset));
    }

    public FoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
        this.radius = intProvider;
        this.offset = intProvider2;
    }

    protected abstract FoliagePlacerType<?> type();

    public void createFoliage(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int i, FoliageAttachment foliageAttachment, int j, int k) {
        this.createFoliage(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, i, foliageAttachment, j, k, this.offset(randomSource));
    }

    protected abstract void createFoliage(LevelSimulatedReader var1, FoliageSetter var2, RandomSource var3, TreeConfiguration var4, int var5, FoliageAttachment var6, int var7, int var8, int var9);

    public abstract int foliageHeight(RandomSource var1, int var2, TreeConfiguration var3);

    public int foliageRadius(RandomSource randomSource, int i) {
        return this.radius.sample(randomSource);
    }

    private int offset(RandomSource randomSource) {
        return this.offset.sample(randomSource);
    }

    protected abstract boolean shouldSkipLocation(RandomSource var1, int var2, int var3, int var4, int var5, boolean var6);

    protected boolean shouldSkipLocationSigned(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
        int n;
        int m;
        if (bl) {
            m = Math.min(Math.abs(i), Math.abs(i - 1));
            n = Math.min(Math.abs(k), Math.abs(k - 1));
        } else {
            m = Math.abs(i);
            n = Math.abs(k);
        }
        return this.shouldSkipLocation(randomSource, m, j, n, l, bl);
    }

    protected void placeLeavesRow(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, BlockPos blockPos, int i, int j, boolean bl) {
        int k = bl ? 1 : 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int l = -i; l <= i + k; ++l) {
            for (int m = -i; m <= i + k; ++m) {
                if (this.shouldSkipLocationSigned(randomSource, l, j, m, i, bl)) continue;
                mutableBlockPos.setWithOffset(blockPos, l, j, m);
                FoliagePlacer.tryPlaceLeaf(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, mutableBlockPos);
            }
        }
    }

    protected final void placeLeavesRowWithHangingLeavesBelow(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, BlockPos blockPos, int i, int j, boolean bl, float f, float g) {
        this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, i, j, bl);
        int k = bl ? 1 : 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Direction direction2 = direction.getClockWise();
            int l = direction2.getAxisDirection() == Direction.AxisDirection.POSITIVE ? i + k : i;
            mutableBlockPos.setWithOffset(blockPos, 0, j - 1, 0).move(direction2, l).move(direction, -i);
            for (int m = -i; m < i + k; ++m) {
                boolean bl2 = foliageSetter.isSet(mutableBlockPos.move(Direction.UP));
                mutableBlockPos.move(Direction.DOWN);
                if (bl2 && !(randomSource.nextFloat() > f) && FoliagePlacer.tryPlaceLeaf(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, mutableBlockPos) && !(randomSource.nextFloat() > g)) {
                    FoliagePlacer.tryPlaceLeaf(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, mutableBlockPos.move(Direction.DOWN));
                    mutableBlockPos.move(Direction.UP);
                }
                mutableBlockPos.move(direction);
            }
        }
    }

    protected static boolean tryPlaceLeaf(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, BlockPos blockPos) {
        if (!TreeFeature.validTreePos(levelSimulatedReader, blockPos)) {
            return false;
        }
        BlockState blockState = treeConfiguration.foliageProvider.getState(randomSource, blockPos);
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            blockState = (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, levelSimulatedReader.isFluidAtPosition(blockPos, fluidState -> fluidState.isSourceOfType(Fluids.WATER)));
        }
        foliageSetter.set(blockPos, blockState);
        return true;
    }

    public static interface FoliageSetter {
        public void set(BlockPos var1, BlockState var2);

        public boolean isSet(BlockPos var1);
    }

    public static final class FoliageAttachment {
        private final BlockPos pos;
        private final int radiusOffset;
        private final boolean doubleTrunk;

        public FoliageAttachment(BlockPos blockPos, int i, boolean bl) {
            this.pos = blockPos;
            this.radiusOffset = i;
            this.doubleTrunk = bl;
        }

        public BlockPos pos() {
            return this.pos;
        }

        public int radiusOffset() {
            return this.radiusOffset;
        }

        public boolean doubleTrunk() {
            return this.doubleTrunk;
        }
    }
}

