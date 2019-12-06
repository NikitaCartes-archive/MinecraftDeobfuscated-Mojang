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
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public abstract class FoliagePlacer
implements Serializable {
    protected final int radius;
    protected final int radiusRandom;
    protected final FoliagePlacerType<?> type;

    public FoliagePlacer(int i, int j, FoliagePlacerType<?> foliagePlacerType) {
        this.radius = i;
        this.radiusRandom = j;
        this.type = foliagePlacerType;
    }

    public abstract void createFoliage(LevelSimulatedRW var1, Random var2, SmallTreeConfiguration var3, int var4, int var5, int var6, BlockPos var7, Set<BlockPos> var8);

    public abstract int foliageRadius(Random var1, int var2, int var3, SmallTreeConfiguration var4);

    protected abstract boolean shouldSkipLocation(Random var1, int var2, int var3, int var4, int var5, int var6);

    public abstract int getTreeRadiusForHeight(int var1, int var2, int var3, int var4);

    protected void placeLeavesRow(LevelSimulatedRW levelSimulatedRW, Random random, SmallTreeConfiguration smallTreeConfiguration, int i, BlockPos blockPos, int j, int k, Set<BlockPos> set) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int l = -k; l <= k; ++l) {
            for (int m = -k; m <= k; ++m) {
                if (this.shouldSkipLocation(random, i, l, j, m, k)) continue;
                mutableBlockPos.set(l + blockPos.getX(), j + blockPos.getY(), m + blockPos.getZ());
                this.placeLeaf(levelSimulatedRW, random, mutableBlockPos, smallTreeConfiguration, set);
            }
        }
    }

    protected void placeLeaf(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, SmallTreeConfiguration smallTreeConfiguration, Set<BlockPos> set) {
        if (AbstractTreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos) || AbstractTreeFeature.isReplaceablePlant(levelSimulatedRW, blockPos) || AbstractTreeFeature.isBlockWater(levelSimulatedRW, blockPos)) {
            levelSimulatedRW.setBlock(blockPos, smallTreeConfiguration.leavesProvider.getState(random, blockPos), 19);
            set.add(blockPos.immutable());
        }
    }

    @Override
    public <T> T serialize(DynamicOps<T> dynamicOps) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(dynamicOps.createString("type"), dynamicOps.createString(Registry.FOLIAGE_PLACER_TYPES.getKey(this.type).toString())).put(dynamicOps.createString("radius"), dynamicOps.createInt(this.radius)).put(dynamicOps.createString("radius_random"), dynamicOps.createInt(this.radiusRandom));
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(builder.build())).getValue();
    }
}

