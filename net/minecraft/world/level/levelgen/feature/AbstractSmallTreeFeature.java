/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class AbstractSmallTreeFeature<T extends SmallTreeConfiguration>
extends AbstractTreeFeature<T> {
    public AbstractSmallTreeFeature(Function<Dynamic<?>, ? extends T> function) {
        super(function);
    }

    protected void placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, int j, Set<BlockPos> set, BoundingBox boundingBox, SmallTreeConfiguration smallTreeConfiguration) {
        for (int k = 0; k < i - j; ++k) {
            this.placeLog(levelSimulatedRW, random, blockPos.above(k), set, boundingBox, smallTreeConfiguration);
        }
    }

    public Optional<BlockPos> getProjectedOrigin(LevelSimulatedRW levelSimulatedRW, int i, int j, int k, BlockPos blockPos, SmallTreeConfiguration smallTreeConfiguration) {
        BlockPos blockPos2;
        int m;
        int l;
        if (!smallTreeConfiguration.fromSapling) {
            l = levelSimulatedRW.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, blockPos).getY();
            m = levelSimulatedRW.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
            blockPos2 = new BlockPos(blockPos.getX(), l, blockPos.getZ());
            if (m - l > smallTreeConfiguration.maxWaterDepth) {
                return Optional.empty();
            }
        } else {
            blockPos2 = blockPos;
        }
        if (blockPos2.getY() < 1 || blockPos2.getY() + i + 1 > 256) {
            return Optional.empty();
        }
        for (l = 0; l <= i + 1; ++l) {
            m = smallTreeConfiguration.foliagePlacer.getTreeRadiusForHeight(j, i, k, l);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int n = -m; n <= m; ++n) {
                for (int o = -m; o <= m; ++o) {
                    if (l + blockPos2.getY() >= 0 && l + blockPos2.getY() < 256) {
                        mutableBlockPos.set(n + blockPos2.getX(), l + blockPos2.getY(), o + blockPos2.getZ());
                        if (AbstractSmallTreeFeature.isFree(levelSimulatedRW, mutableBlockPos) && (smallTreeConfiguration.ignoreVines || !AbstractSmallTreeFeature.isVine(levelSimulatedRW, mutableBlockPos))) continue;
                        return Optional.empty();
                    }
                    return Optional.empty();
                }
            }
        }
        if (!AbstractSmallTreeFeature.isGrassOrDirtOrFarmland(levelSimulatedRW, blockPos2.below()) || blockPos2.getY() >= 256 - i - 1) {
            return Optional.empty();
        }
        return Optional.of(blockPos2);
    }
}

