/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.AbstractSmallTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class TreeFeature
extends AbstractSmallTreeFeature<SmallTreeConfiguration> {
    public TreeFeature(Function<Dynamic<?>, ? extends SmallTreeConfiguration> function) {
        super(function);
    }

    @Override
    public boolean doPlace(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos2, Set<BlockPos> set, Set<BlockPos> set2, BoundingBox boundingBox, SmallTreeConfiguration smallTreeConfiguration) {
        int j;
        int k;
        int l;
        int i = smallTreeConfiguration.trunkPlacer.getTreeHeight(random, smallTreeConfiguration);
        Optional<BlockPos> optional = this.getProjectedOrigin(levelSimulatedRW, i, l = smallTreeConfiguration.foliagePlacer.foliageRadius(random, k = i - (j = smallTreeConfiguration.foliagePlacer.foliageHeight(random, i)), smallTreeConfiguration), blockPos2, smallTreeConfiguration);
        if (!optional.isPresent()) {
            return false;
        }
        BlockPos blockPos22 = optional.get();
        this.setDirtAt(levelSimulatedRW, blockPos22.below());
        Map<BlockPos, Integer> map = smallTreeConfiguration.trunkPlacer.placeTrunk(levelSimulatedRW, random, i, blockPos22, l, set, boundingBox, smallTreeConfiguration);
        map.forEach((blockPos, integer) -> smallTreeConfiguration.foliagePlacer.createFoliage(levelSimulatedRW, random, smallTreeConfiguration, i, (BlockPos)blockPos, j, (int)integer, set2));
        return true;
    }
}

