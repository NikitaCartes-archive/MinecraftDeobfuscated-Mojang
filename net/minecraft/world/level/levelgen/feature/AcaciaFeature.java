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
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.AbstractSmallTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class AcaciaFeature
extends AbstractSmallTreeFeature<SmallTreeConfiguration> {
    public AcaciaFeature(Function<Dynamic<?>, ? extends SmallTreeConfiguration> function) {
        super(function);
    }

    @Override
    public boolean doPlace(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, Set<BlockPos> set, Set<BlockPos> set2, BoundingBox boundingBox, SmallTreeConfiguration smallTreeConfiguration) {
        int r;
        int k;
        int j;
        int i = smallTreeConfiguration.baseHeight + random.nextInt(smallTreeConfiguration.heightRandA + 1) + random.nextInt(smallTreeConfiguration.heightRandB + 1);
        Optional<BlockPos> optional = this.getProjectedOrigin(levelSimulatedRW, i, j = smallTreeConfiguration.trunkHeight >= 0 ? smallTreeConfiguration.trunkHeight + random.nextInt(smallTreeConfiguration.trunkHeightRandom + 1) : i - (smallTreeConfiguration.foliageHeight + random.nextInt(smallTreeConfiguration.foliageHeightRandom + 1)), k = smallTreeConfiguration.foliagePlacer.foliageRadius(random, j, i, smallTreeConfiguration), blockPos, smallTreeConfiguration);
        if (!optional.isPresent()) {
            return false;
        }
        BlockPos blockPos2 = optional.get();
        this.setDirtAt(levelSimulatedRW, blockPos2.below());
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int l = i - random.nextInt(4) - 1;
        int m = 3 - random.nextInt(3);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n = blockPos2.getX();
        int o = blockPos2.getZ();
        int p = 0;
        for (int q = 0; q < i; ++q) {
            r = blockPos2.getY() + q;
            if (q >= l && m > 0) {
                n += direction.getStepX();
                o += direction.getStepZ();
                --m;
            }
            if (!this.placeLog(levelSimulatedRW, random, mutableBlockPos.set(n, r, o), set, boundingBox, smallTreeConfiguration)) continue;
            p = r;
        }
        BlockPos blockPos3 = new BlockPos(n, p, o);
        smallTreeConfiguration.foliagePlacer.createFoliage(levelSimulatedRW, random, smallTreeConfiguration, i, j, k + 1, blockPos3, set2);
        n = blockPos2.getX();
        o = blockPos2.getZ();
        Direction direction2 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        if (direction2 != direction) {
            r = l - random.nextInt(2) - 1;
            int s = 1 + random.nextInt(3);
            p = 0;
            for (int t = r; t < i && s > 0; ++t, --s) {
                if (t < 1) continue;
                int u = blockPos2.getY() + t;
                if (!this.placeLog(levelSimulatedRW, random, mutableBlockPos.set(n += direction2.getStepX(), u, o += direction2.getStepZ()), set, boundingBox, smallTreeConfiguration)) continue;
                p = u;
            }
            if (p > 0) {
                BlockPos blockPos4 = new BlockPos(n, p, o);
                smallTreeConfiguration.foliagePlacer.createFoliage(levelSimulatedRW, random, smallTreeConfiguration, i, j, k, blockPos4, set2);
            }
        }
        return true;
    }
}

