/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BendingTrunkPlacer
extends TrunkPlacer {
    public static final Codec<BendingTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> BendingTrunkPlacer.trunkPlacerParts(instance).and(instance.group(Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("min_height_for_leaves", 1).forGetter(bendingTrunkPlacer -> bendingTrunkPlacer.minHeightForLeaves), ((MapCodec)UniformInt.codec(1, 32, 32).fieldOf("bend_length")).forGetter(bendingTrunkPlacer -> bendingTrunkPlacer.bendLength))).apply((Applicative<BendingTrunkPlacer, ?>)instance, BendingTrunkPlacer::new));
    private final int minHeightForLeaves;
    private final UniformInt bendLength;

    public BendingTrunkPlacer(int i, int j, int k, int l, UniformInt uniformInt) {
        super(i, j, k);
        this.minHeightForLeaves = l;
        this.bendLength = uniformInt;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.BENDING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        int k;
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int j = i - 1;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        Vec3i blockPos2 = mutableBlockPos.below();
        BendingTrunkPlacer.setDirtAt(levelSimulatedRW, random, (BlockPos)blockPos2, treeConfiguration);
        ArrayList<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
        for (k = 0; k <= j; ++k) {
            if (k + 1 >= j + random.nextInt(2)) {
                mutableBlockPos.move(direction);
            }
            if (TreeFeature.validTreePos(levelSimulatedRW, mutableBlockPos)) {
                BendingTrunkPlacer.placeLog(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration);
            }
            if (k >= this.minHeightForLeaves) {
                list.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.immutable(), 0, false));
            }
            mutableBlockPos.move(Direction.UP);
        }
        k = this.bendLength.sample(random);
        for (int l = 0; l <= k; ++l) {
            if (TreeFeature.validTreePos(levelSimulatedRW, mutableBlockPos)) {
                BendingTrunkPlacer.placeLog(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration);
            }
            list.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.immutable(), 0, false));
            mutableBlockPos.move(direction);
        }
        return list;
    }
}

