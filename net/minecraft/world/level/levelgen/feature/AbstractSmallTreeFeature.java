/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public abstract class AbstractSmallTreeFeature<T extends SmallTreeConfiguration>
extends AbstractTreeFeature<T> {
    public AbstractSmallTreeFeature(Function<Dynamic<?>, ? extends T> function) {
        super(function);
    }

    public Optional<BlockPos> getProjectedOrigin(LevelSimulatedRW levelSimulatedRW, int i, int j, BlockPos blockPos, SmallTreeConfiguration smallTreeConfiguration) {
        BlockPos blockPos2;
        int l;
        int k;
        if (!smallTreeConfiguration.fromSapling) {
            k = levelSimulatedRW.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, blockPos).getY();
            l = levelSimulatedRW.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
            blockPos2 = new BlockPos(blockPos.getX(), k, blockPos.getZ());
            if (l - k > smallTreeConfiguration.maxWaterDepth) {
                return Optional.empty();
            }
        } else {
            blockPos2 = blockPos;
        }
        if (blockPos2.getY() < 1 || blockPos2.getY() + i + 1 > 256) {
            return Optional.empty();
        }
        for (k = 0; k <= i + 1; ++k) {
            l = smallTreeConfiguration.foliagePlacer.getTreeRadiusForHeight(i, j, k);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int m = -l; m <= l; ++m) {
                for (int n = -l; n <= l; ++n) {
                    if (k + blockPos2.getY() >= 0 && k + blockPos2.getY() < 256) {
                        mutableBlockPos.set(m + blockPos2.getX(), k + blockPos2.getY(), n + blockPos2.getZ());
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

