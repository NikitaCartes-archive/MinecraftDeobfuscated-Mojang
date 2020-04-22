/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public abstract class FoliagePlacer
implements Serializable {
    private final int radius;
    private final int radiusRandom;
    private final int offset;
    private final int offsetRandom;
    private final FoliagePlacerType<?> type;

    public FoliagePlacer(int i, int j, int k, int l, FoliagePlacerType<?> foliagePlacerType) {
        this.radius = i;
        this.radiusRandom = j;
        this.offset = k;
        this.offsetRandom = l;
        this.type = foliagePlacerType;
    }

    public void createFoliage(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, int i, FoliageAttachment foliageAttachment, int j, int k, Set<BlockPos> set) {
        this.createFoliage(levelSimulatedRW, random, treeConfiguration, i, foliageAttachment, j, k, set, this.offset(random));
    }

    protected abstract void createFoliage(LevelSimulatedRW var1, Random var2, TreeConfiguration var3, int var4, FoliageAttachment var5, int var6, int var7, Set<BlockPos> var8, int var9);

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

    protected void placeLeavesRow(LevelSimulatedRW levelSimulatedRW, Random random, TreeConfiguration treeConfiguration, BlockPos blockPos, int i, Set<BlockPos> set, int j, boolean bl) {
        int k = bl ? 1 : 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int l = -i; l <= i + k; ++l) {
            for (int m = -i; m <= i + k; ++m) {
                if (this.shouldSkipLocationSigned(random, l, j, m, i, bl)) continue;
                mutableBlockPos.setWithOffset(blockPos, l, j, m);
                if (!TreeFeature.validTreePos(levelSimulatedRW, mutableBlockPos)) continue;
                levelSimulatedRW.setBlock(mutableBlockPos, treeConfiguration.leavesProvider.getState(random, mutableBlockPos), 19);
                set.add(mutableBlockPos.immutable());
            }
        }
    }

    @Override
    public <T> T serialize(DynamicOps<T> dynamicOps) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(dynamicOps.createString("type"), dynamicOps.createString(Registry.FOLIAGE_PLACER_TYPES.getKey(this.type).toString())).put(dynamicOps.createString("radius"), dynamicOps.createInt(this.radius)).put(dynamicOps.createString("radius_random"), dynamicOps.createInt(this.radiusRandom)).put(dynamicOps.createString("offset"), dynamicOps.createInt(this.offset)).put(dynamicOps.createString("offset_random"), dynamicOps.createInt(this.offsetRandom));
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(builder.build())).getValue();
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

