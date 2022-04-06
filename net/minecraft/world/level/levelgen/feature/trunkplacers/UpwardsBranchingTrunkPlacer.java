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
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class UpwardsBranchingTrunkPlacer
extends TrunkPlacer {
    public static final Codec<UpwardsBranchingTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> UpwardsBranchingTrunkPlacer.trunkPlacerParts(instance).and(instance.group(((MapCodec)IntProvider.POSITIVE_CODEC.fieldOf("extra_branch_steps")).forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.extraBranchSteps), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("place_branch_per_log_probability")).forGetter(upwardsBranchingTrunkPlacer -> Float.valueOf(upwardsBranchingTrunkPlacer.placeBranchPerLogProbability)), ((MapCodec)IntProvider.NON_NEGATIVE_CODEC.fieldOf("extra_branch_length")).forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.extraBranchLength), ((MapCodec)RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("can_grow_through")).forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.canGrowThrough))).apply((Applicative<UpwardsBranchingTrunkPlacer, ?>)instance, UpwardsBranchingTrunkPlacer::new));
    private final IntProvider extraBranchSteps;
    private final float placeBranchPerLogProbability;
    private final IntProvider extraBranchLength;
    private final HolderSet<Block> canGrowThrough;

    public UpwardsBranchingTrunkPlacer(int i, int j, int k, IntProvider intProvider, float f, IntProvider intProvider2, HolderSet<Block> holderSet) {
        super(i, j, k);
        this.extraBranchSteps = intProvider;
        this.placeBranchPerLogProbability = f;
        this.extraBranchLength = intProvider2;
        this.canGrowThrough = holderSet;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.UPWARDS_BRANCHING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        ArrayList<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int j = 0; j < i; ++j) {
            int k = blockPos.getY() + j;
            if (this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.set(blockPos.getX(), k, blockPos.getZ()), treeConfiguration) && j < i - 1 && randomSource.nextFloat() < this.placeBranchPerLogProbability) {
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
                int l = this.extraBranchLength.sample(randomSource);
                int m = l - this.extraBranchLength.sample(randomSource) - 1;
                int n = this.extraBranchSteps.sample(randomSource);
                this.placeBranch(levelSimulatedReader, biConsumer, randomSource, i, treeConfiguration, list, mutableBlockPos, k, direction, m, n);
            }
            if (j != i - 1) continue;
            list.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.set(blockPos.getX(), k + 1, blockPos.getZ()), 0, false));
        }
        return list;
    }

    private void placeBranch(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, int i, TreeConfiguration treeConfiguration, List<FoliagePlacer.FoliageAttachment> list, BlockPos.MutableBlockPos mutableBlockPos, int j, Direction direction, int k, int l) {
        int m = 0;
        int n = mutableBlockPos.getX();
        int o = mutableBlockPos.getZ();
        for (int p = k; p < i && l > 0; ++p, --l) {
            if (p < 1) continue;
            int q = j + p;
            if (this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.set(n += direction.getStepX(), q, o += direction.getStepZ()), treeConfiguration)) {
                m = q + 1;
            }
            list.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.immutable(), 0, false));
        }
        if (m > 1) {
            BlockPos blockPos = new BlockPos(n, m, o);
            list.add(new FoliagePlacer.FoliageAttachment(blockPos, 0, false));
            list.add(new FoliagePlacer.FoliageAttachment(blockPos.below(2), 0, false));
        }
    }

    @Override
    protected boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return super.validTreePos(levelSimulatedReader, blockPos) || levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(this.canGrowThrough));
    }
}

