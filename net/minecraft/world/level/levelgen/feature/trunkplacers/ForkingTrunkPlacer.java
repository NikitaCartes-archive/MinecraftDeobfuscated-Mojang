/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class ForkingTrunkPlacer
extends TrunkPlacer {
    public static final Codec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> ForkingTrunkPlacer.trunkPlacerParts(instance).apply(instance, ForkingTrunkPlacer::new));

    public ForkingTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FORKING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        int p;
        ForkingTrunkPlacer.setDirtAt(levelSimulatedRW, random, blockPos.below(), treeConfiguration);
        ArrayList<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int j = i - random.nextInt(4) - 1;
        int k = 3 - random.nextInt(3);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int l = blockPos.getX();
        int m = blockPos.getZ();
        int n = 0;
        for (int o = 0; o < i; ++o) {
            p = blockPos.getY() + o;
            if (o >= j && k > 0) {
                l += direction.getStepX();
                m += direction.getStepZ();
                --k;
            }
            if (!ForkingTrunkPlacer.placeLog(levelSimulatedRW, random, mutableBlockPos.set(l, p, m), set, boundingBox, treeConfiguration)) continue;
            n = p + 1;
        }
        list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(l, n, m), 1, false));
        l = blockPos.getX();
        m = blockPos.getZ();
        Direction direction2 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        if (direction2 != direction) {
            p = j - random.nextInt(2) - 1;
            int q = 1 + random.nextInt(3);
            n = 0;
            for (int r = p; r < i && q > 0; ++r, --q) {
                if (r < 1) continue;
                int s = blockPos.getY() + r;
                if (!ForkingTrunkPlacer.placeLog(levelSimulatedRW, random, mutableBlockPos.set(l += direction2.getStepX(), s, m += direction2.getStepZ()), set, boundingBox, treeConfiguration)) continue;
                n = s + 1;
            }
            if (n > 1) {
                list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(l, n, m), 0, false));
            }
        }
        return list;
    }
}

