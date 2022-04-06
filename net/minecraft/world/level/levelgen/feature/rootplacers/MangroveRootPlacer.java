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
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class MangroveRootPlacer
extends RootPlacer {
    public static final int ROOT_WIDTH_LIMIT = 8;
    public static final int ROOT_LENGTH_LIMIT = 15;
    public static final Codec<MangroveRootPlacer> CODEC = RecordCodecBuilder.create(instance -> MangroveRootPlacer.rootPlacerParts(instance).and(instance.group(((MapCodec)RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("can_grow_through")).forGetter(mangroveRootPlacer -> mangroveRootPlacer.canGrowThrough), ((MapCodec)RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("muddy_roots_in")).forGetter(mangroveRootPlacer -> mangroveRootPlacer.muddyRootsIn), ((MapCodec)BlockStateProvider.CODEC.fieldOf("muddy_roots_provider")).forGetter(mangroveRootPlacer -> mangroveRootPlacer.muddyRootsProvider), ((MapCodec)Codec.intRange(1, 12).fieldOf("max_root_width")).forGetter(mangroveRootPlacer -> mangroveRootPlacer.maxRootWidth), ((MapCodec)Codec.intRange(1, 64).fieldOf("max_root_length")).forGetter(mangroveRootPlacer -> mangroveRootPlacer.maxRootLength), ((MapCodec)IntProvider.CODEC.fieldOf("y_offset")).forGetter(mangroveRootPlacer -> mangroveRootPlacer.yOffset), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("random_skew_chance")).forGetter(mangroveRootPlacer -> Float.valueOf(mangroveRootPlacer.randomSkewChance)))).apply((Applicative<MangroveRootPlacer, ?>)instance, MangroveRootPlacer::new));
    private final HolderSet<Block> canGrowThrough;
    private final HolderSet<Block> muddyRootsIn;
    private final BlockStateProvider muddyRootsProvider;
    private final int maxRootWidth;
    private final int maxRootLength;
    private final IntProvider yOffset;
    private final float randomSkewChance;

    public MangroveRootPlacer(BlockStateProvider blockStateProvider, HolderSet<Block> holderSet, HolderSet<Block> holderSet2, BlockStateProvider blockStateProvider2, int i, int j, IntProvider intProvider, float f) {
        super(blockStateProvider);
        this.canGrowThrough = holderSet;
        this.muddyRootsIn = holderSet2;
        this.muddyRootsProvider = blockStateProvider2;
        this.maxRootWidth = i;
        this.maxRootLength = j;
        this.yOffset = intProvider;
        this.randomSkewChance = f;
    }

    @Override
    public Optional<BlockPos> placeRoots(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos blockPos2 = blockPos.offset(0, this.yOffset.sample(randomSource), 0);
        ArrayList<BlockPos> list = Lists.newArrayList();
        if (!this.canPlaceRoot(levelSimulatedReader, blockPos2)) {
            return Optional.empty();
        }
        list.add(blockPos2.below());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            ArrayList<BlockPos> list2;
            BlockPos blockPos3 = blockPos2.relative(direction);
            if (!this.simulateRoots(levelSimulatedReader, randomSource, blockPos3, direction, blockPos2, list2 = Lists.newArrayList(), 0)) {
                return Optional.empty();
            }
            list.addAll(list2);
            list.add(blockPos2.relative(direction));
        }
        for (BlockPos blockPos4 : list) {
            this.placeRoot(levelSimulatedReader, biConsumer, randomSource, blockPos4, treeConfiguration);
        }
        return Optional.of(blockPos2);
    }

    private boolean simulateRoots(LevelSimulatedReader levelSimulatedReader, RandomSource randomSource, BlockPos blockPos, Direction direction, BlockPos blockPos2, List<BlockPos> list, int i) {
        if (i == this.maxRootLength || list.size() > this.maxRootLength) {
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
        if (i > this.maxRootWidth - 3 && i <= this.maxRootWidth) {
            return randomSource.nextFloat() < this.randomSkewChance ? List.of(blockPos3, blockPos4.below()) : List.of(blockPos3);
        }
        if (i > this.maxRootWidth) {
            return List.of(blockPos3);
        }
        if (randomSource.nextFloat() < this.randomSkewChance) {
            return List.of(blockPos3);
        }
        return randomSource.nextBoolean() ? List.of(blockPos4) : List.of(blockPos3);
    }

    protected boolean canPlaceRoot(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return TreeFeature.validTreePos(levelSimulatedReader, blockPos) || levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(this.canGrowThrough));
    }

    @Override
    protected void placeRoot(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        if (levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(this.muddyRootsIn))) {
            BlockState blockState2 = this.muddyRootsProvider.getState(randomSource, blockPos);
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

