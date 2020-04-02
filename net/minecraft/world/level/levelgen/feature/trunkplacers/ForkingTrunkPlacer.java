/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class ForkingTrunkPlacer
extends TrunkPlacer {
    public ForkingTrunkPlacer(int i, int j, int k) {
        super(i, j, k, TrunkPlacerType.FORKING_TRUNK_PLACER);
    }

    public <T> ForkingTrunkPlacer(Dynamic<T> dynamic) {
        this(dynamic.get("base_height").asInt(0), dynamic.get("height_rand_a").asInt(0), dynamic.get("height_rand_b").asInt(0));
    }

    @Override
    public Map<BlockPos, Integer> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, int j, Set<BlockPos> set, BoundingBox boundingBox, SmallTreeConfiguration smallTreeConfiguration) {
        int q;
        Object2ObjectLinkedOpenHashMap<BlockPos, Integer> map = new Object2ObjectLinkedOpenHashMap<BlockPos, Integer>();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int k = i - random.nextInt(4) - 1;
        int l = 3 - random.nextInt(3);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int m = blockPos.getX();
        int n = blockPos.getZ();
        int o = 0;
        for (int p = 0; p < i; ++p) {
            q = blockPos.getY() + p;
            if (p >= k && l > 0) {
                m += direction.getStepX();
                n += direction.getStepZ();
                --l;
            }
            if (!AbstractTreeFeature.placeLog(levelSimulatedRW, random, mutableBlockPos.set(m, q, n), set, boundingBox, smallTreeConfiguration)) continue;
            o = q + 1;
        }
        map.put(new BlockPos(m, o, n), j + 1);
        m = blockPos.getX();
        n = blockPos.getZ();
        Direction direction2 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        if (direction2 != direction) {
            q = k - random.nextInt(2) - 1;
            int r = 1 + random.nextInt(3);
            o = 0;
            for (int s = q; s < i && r > 0; ++s, --r) {
                if (s < 1) continue;
                int t = blockPos.getY() + s;
                if (!AbstractTreeFeature.placeLog(levelSimulatedRW, random, mutableBlockPos.set(m += direction2.getStepX(), t, n += direction2.getStepZ()), set, boundingBox, smallTreeConfiguration)) continue;
                o = t + 1;
            }
            if (o > 1) {
                map.put(new BlockPos(m, o, n), j);
            }
        }
        return map;
    }
}

