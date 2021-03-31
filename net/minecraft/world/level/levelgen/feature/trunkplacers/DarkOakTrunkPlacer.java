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
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class DarkOakTrunkPlacer
extends TrunkPlacer {
    public static final Codec<DarkOakTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> DarkOakTrunkPlacer.trunkPlacerParts(instance).apply(instance, DarkOakTrunkPlacer::new));

    public DarkOakTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.DARK_OAK_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        int s;
        int r;
        ArrayList<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
        BlockPos blockPos2 = blockPos.below();
        DarkOakTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2, treeConfiguration);
        DarkOakTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2.east(), treeConfiguration);
        DarkOakTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2.south(), treeConfiguration);
        DarkOakTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2.south().east(), treeConfiguration);
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int j = i - random.nextInt(4);
        int k = 2 - random.nextInt(3);
        int l = blockPos.getX();
        int m = blockPos.getY();
        int n = blockPos.getZ();
        int o = l;
        int p = n;
        int q = m + i - 1;
        for (r = 0; r < i; ++r) {
            BlockPos blockPos3;
            if (r >= j && k > 0) {
                o += direction.getStepX();
                p += direction.getStepZ();
                --k;
            }
            if (!TreeFeature.isAirOrLeaves(levelSimulatedReader, blockPos3 = new BlockPos(o, s = m + r, p))) continue;
            DarkOakTrunkPlacer.placeLog(levelSimulatedReader, biConsumer, random, blockPos3, treeConfiguration);
            DarkOakTrunkPlacer.placeLog(levelSimulatedReader, biConsumer, random, blockPos3.east(), treeConfiguration);
            DarkOakTrunkPlacer.placeLog(levelSimulatedReader, biConsumer, random, blockPos3.south(), treeConfiguration);
            DarkOakTrunkPlacer.placeLog(levelSimulatedReader, biConsumer, random, blockPos3.east().south(), treeConfiguration);
        }
        list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(o, q, p), 0, true));
        for (r = -1; r <= 2; ++r) {
            for (s = -1; s <= 2; ++s) {
                if (r >= 0 && r <= 1 && s >= 0 && s <= 1 || random.nextInt(3) > 0) continue;
                int t = random.nextInt(3) + 2;
                for (int u = 0; u < t; ++u) {
                    DarkOakTrunkPlacer.placeLog(levelSimulatedReader, biConsumer, random, new BlockPos(l + r, q - u - 1, n + s), treeConfiguration);
                }
                list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(o + r, q, p + s), 0, false));
            }
        }
        return list;
    }
}

