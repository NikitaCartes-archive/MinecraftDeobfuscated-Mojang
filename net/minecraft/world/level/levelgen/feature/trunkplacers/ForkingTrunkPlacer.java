/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

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
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        int o;
        ForkingTrunkPlacer.setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos.below(), treeConfiguration);
        ArrayList<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
        int j = i - randomSource.nextInt(4) - 1;
        int k = 3 - randomSource.nextInt(3);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int l = blockPos.getX();
        int m = blockPos.getZ();
        OptionalInt optionalInt = OptionalInt.empty();
        for (int n = 0; n < i; ++n) {
            o = blockPos.getY() + n;
            if (n >= j && k > 0) {
                l += direction.getStepX();
                m += direction.getStepZ();
                --k;
            }
            if (!this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.set(l, o, m), treeConfiguration)) continue;
            optionalInt = OptionalInt.of(o + 1);
        }
        if (optionalInt.isPresent()) {
            list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(l, optionalInt.getAsInt(), m), 1, false));
        }
        l = blockPos.getX();
        m = blockPos.getZ();
        Direction direction2 = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
        if (direction2 != direction) {
            o = j - randomSource.nextInt(2) - 1;
            int p = 1 + randomSource.nextInt(3);
            optionalInt = OptionalInt.empty();
            for (int q = o; q < i && p > 0; ++q, --p) {
                if (q < 1) continue;
                int r = blockPos.getY() + q;
                if (!this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.set(l += direction2.getStepX(), r, m += direction2.getStepZ()), treeConfiguration)) continue;
                optionalInt = OptionalInt.of(r + 1);
            }
            if (optionalInt.isPresent()) {
                list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(l, optionalInt.getAsInt(), m), 0, false));
            }
        }
        return list;
    }
}

