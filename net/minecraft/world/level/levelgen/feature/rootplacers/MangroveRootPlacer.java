/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.rootplacers.AboveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacement;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class MangroveRootPlacer
extends RootPlacer {
    public static final int ROOT_WIDTH_LIMIT = 8;
    public static final int ROOT_LENGTH_LIMIT = 15;
    public static final Codec<MangroveRootPlacer> CODEC = RecordCodecBuilder.create(instance -> MangroveRootPlacer.rootPlacerParts(instance).and(((MapCodec)MangroveRootPlacement.CODEC.fieldOf("mangrove_root_placement")).forGetter(mangroveRootPlacer -> mangroveRootPlacer.mangroveRootPlacement)).apply((Applicative<MangroveRootPlacer, ?>)instance, MangroveRootPlacer::new));
    private final MangroveRootPlacement mangroveRootPlacement;

    public MangroveRootPlacer(IntProvider intProvider, BlockStateProvider blockStateProvider, Optional<AboveRootPlacement> optional, MangroveRootPlacement mangroveRootPlacement) {
        super(intProvider, blockStateProvider, optional);
        this.mangroveRootPlacement = mangroveRootPlacement;
    }

    @Override
    public boolean placeRoots(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos, BlockPos blockPos2, TreeConfiguration treeConfiguration) {
        ArrayList<BlockPos> list = Lists.newArrayList();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        while (mutableBlockPos.getY() < blockPos2.getY()) {
            if (!this.canPlaceRoot(levelSimulatedReader, mutableBlockPos)) {
                return false;
            }
            mutableBlockPos.move(Direction.UP);
        }
        list.add(blockPos2.below());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            ArrayList<BlockPos> list2;
            BlockPos blockPos3 = blockPos2.relative(direction);
            if (!this.simulateRoots(levelSimulatedReader, randomSource, blockPos3, direction, blockPos2, list2 = Lists.newArrayList(), 0)) {
                return false;
            }
            list.addAll(list2);
            list.add(blockPos2.relative(direction));
        }
        for (BlockPos blockPos4 : list) {
            this.placeRoot(levelSimulatedReader, biConsumer, randomSource, blockPos4, treeConfiguration);
        }
        return true;
    }

    private boolean simulateRoots(LevelSimulatedReader levelSimulatedReader, RandomSource randomSource, BlockPos blockPos, Direction direction, BlockPos blockPos2, List<BlockPos> list, int i) {
        int j = this.mangroveRootPlacement.maxRootLength();
        if (i == j || list.size() > j) {
            return false;
        }
        List<BlockPos> list2 = this.potentialRootPositions(blockPos, direction, randomSource, blockPos2);
        for (BlockPos blockPos3 : list2) {
            if (!this.canPlaceRoot(levelSimulatedReader, blockPos3)) continue;
            list.add(blockPos3);
            if (this.simulateRoots(levelSimulatedReader, randomSource, blockPos3, direction, blockPos2, list, i + 1)) continue;
            return false;
        }
        return true;
    }

    protected List<BlockPos> potentialRootPositions(BlockPos blockPos, Direction direction, RandomSource randomSource, BlockPos blockPos2) {
        BlockPos blockPos3 = blockPos.below();
        BlockPos blockPos4 = blockPos.relative(direction);
        int i = blockPos.distManhattan(blockPos2);
        int j = this.mangroveRootPlacement.maxRootWidth();
        float f = this.mangroveRootPlacement.randomSkewChance();
        if (i > j - 3 && i <= j) {
            return randomSource.nextFloat() < f ? List.of(blockPos3, blockPos4.below()) : List.of(blockPos3);
        }
        if (i > j) {
            return List.of(blockPos3);
        }
        if (randomSource.nextFloat() < f) {
            return List.of(blockPos3);
        }
        return randomSource.nextBoolean() ? List.of(blockPos4) : List.of(blockPos3);
    }

    @Override
    protected boolean canPlaceRoot(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return super.canPlaceRoot(levelSimulatedReader, blockPos) || levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(this.mangroveRootPlacement.canGrowThrough()));
    }

    @Override
    protected void placeRoot(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        if (levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(this.mangroveRootPlacement.muddyRootsIn()))) {
            BlockState blockState2 = this.mangroveRootPlacement.muddyRootsProvider().getState(randomSource, blockPos);
            biConsumer.accept(blockPos, this.getPotentiallyWaterloggedState(levelSimulatedReader, blockPos, blockState2));
        } else {
            super.placeRoot(levelSimulatedReader, biConsumer, randomSource, blockPos, treeConfiguration);
        }
    }

    @Override
    protected RootPlacerType<?> type() {
        return RootPlacerType.MANGROVE_ROOT_PLACER;
    }
}

