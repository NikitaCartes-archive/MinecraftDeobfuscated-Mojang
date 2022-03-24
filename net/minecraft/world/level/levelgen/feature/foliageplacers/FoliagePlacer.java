/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.material.Fluids;

public abstract class FoliagePlacer {
    public static final Codec<FoliagePlacer> CODEC = Registry.FOLIAGE_PLACER_TYPES.byNameCodec().dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
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

    public void createFoliage(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, int i, FoliageAttachment foliageAttachment, int j, int k) {
        this.createFoliage(levelSimulatedReader, biConsumer, random, treeConfiguration, i, foliageAttachment, j, k, this.offset(random));
    }

    protected abstract void createFoliage(LevelSimulatedReader var1, BiConsumer<BlockPos, BlockState> var2, Random var3, TreeConfiguration var4, int var5, FoliageAttachment var6, int var7, int var8, int var9);

    public abstract int foliageHeight(Random var1, int var2, TreeConfiguration var3);

    public int foliageRadius(Random random, int i) {
        return this.radius.sample(random);
    }

    private int offset(Random random) {
        return this.offset.sample(random);
    }

    protected abstract boolean shouldSkipLocation(Random var1, int var2, int var3, int var4, int var5, boolean var6);

    protected boolean shouldSkipLocationSigned(Random random, int i, int j, int k, int l, boolean bl) {
        int n;
        int m;
        if (bl) {
            m = Math.min(Math.abs(i), Math.abs(i - 1));
            n = Math.min(Math.abs(k), Math.abs(k - 1));
        } else {
            m = Math.abs(i);
            n = Math.abs(k);
        }
        return this.shouldSkipLocation(random, m, j, n, l, bl);
    }

    protected void placeLeavesRow(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, BlockPos blockPos, int i, int j, boolean bl) {
        int k = bl ? 1 : 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int l = -i; l <= i + k; ++l) {
            for (int m = -i; m <= i + k; ++m) {
                if (this.shouldSkipLocationSigned(random, l, j, m, i, bl)) continue;
                mutableBlockPos.setWithOffset(blockPos, l, j, m);
                FoliagePlacer.tryPlaceLeaf(levelSimulatedReader, biConsumer, random, treeConfiguration, mutableBlockPos);
            }
        }
    }

    protected static void tryPlaceLeaf(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, TreeConfiguration treeConfiguration, BlockPos blockPos) {
        if (TreeFeature.validTreePos(levelSimulatedReader, blockPos)) {
            BlockState blockState = treeConfiguration.foliageProvider.getState(random, blockPos);
            if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                blockState = (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, levelSimulatedReader.isFluidAtPosition(blockPos, fluidState -> fluidState.isSourceOfType(Fluids.WATER)));
            }
            biConsumer.accept(blockPos, blockState);
        }
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

