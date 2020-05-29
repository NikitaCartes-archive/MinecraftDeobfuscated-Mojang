/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class FoliagePlacer {
    public static final Codec<FoliagePlacer> CODEC = Registry.FOLIAGE_PLACER_TYPES.dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
    protected final int radius;
    protected final int radiusRandom;
    protected final int offset;
    protected final int offsetRandom;

    protected static <P extends FoliagePlacer> Products.P4<RecordCodecBuilder.Mu<P>, Integer, Integer, Integer, Integer> foliagePlacerParts(RecordCodecBuilder.Instance<P> instance) {
        return instance.group(((MapCodec)Codec.INT.fieldOf("radius")).forGetter(foliagePlacer -> foliagePlacer.radius), ((MapCodec)Codec.INT.fieldOf("radius_random")).forGetter(foliagePlacer -> foliagePlacer.radiusRandom), ((MapCodec)Codec.INT.fieldOf("offset")).forGetter(foliagePlacer -> foliagePlacer.offset), ((MapCodec)Codec.INT.fieldOf("offset_random")).forGetter(foliagePlacer -> foliagePlacer.offsetRandom));
    }

    public FoliagePlacer(int i, int j, int k, int l) {
        this.radius = i;
        this.radiusRandom = j;
        this.offset = k;
        this.offsetRandom = l;
    }

    protected abstract FoliagePlacerType<?> type();

    public void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliageAttachment foliageAttachment, int j, int k, Set<BlockPos> set, BoundingBox boundingBox) {
        this.createFoliage(levelSimulatedRW, random, treeConfiguration, i, foliageAttachment, j, k, set, this.offset(random), boundingBox);
    }

    protected abstract void createFoliage(LevelSimulatedRW var1, Random var2, TreeConfiguration var3, int var4, FoliageAttachment var5, int var6, int var7, Set<BlockPos> var8, int var9, BoundingBox var10);

    public abstract int foliageHeight(Random var1, int var2, TreeConfiguration var3);

    public int foliageRadius(Random random, int i) {
        return this.radius + random.nextInt(this.radiusRandom + 1);
    }

    private int offset(Random random) {
        return this.offset + random.nextInt(this.offsetRandom + 1);
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

    protected void placeLeavesRow(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, BlockPos blockPos, int i, Set<BlockPos> set, int j, boolean bl, BoundingBox boundingBox) {
        int k = bl ? 1 : 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int l = -i; l <= i + k; ++l) {
            for (int m = -i; m <= i + k; ++m) {
                if (this.shouldSkipLocationSigned(random, l, j, m, i, bl)) continue;
                mutableBlockPos.setWithOffset(blockPos, l, j, m);
                if (!TreeFeature.validTreePos(levelSimulatedRW, mutableBlockPos)) continue;
                levelSimulatedRW.setBlock(mutableBlockPos, treeConfiguration.leavesProvider.getState(random, mutableBlockPos), 19);
                boundingBox.expand(new BoundingBox(mutableBlockPos, mutableBlockPos));
                set.add(mutableBlockPos.immutable());
            }
        }
    }

    public static final class FoliageAttachment {
        private final BlockPos foliagePos;
        private final int radiusOffset;
        private final boolean doubleTrunk;

        public FoliageAttachment(BlockPos blockPos, int i, boolean bl) {
            this.foliagePos = blockPos;
            this.radiusOffset = i;
            this.doubleTrunk = bl;
        }

        public BlockPos foliagePos() {
            return this.foliagePos;
        }

        public int radiusOffset() {
            return this.radiusOffset;
        }

        public boolean doubleTrunk() {
            return this.doubleTrunk;
        }
    }
}

