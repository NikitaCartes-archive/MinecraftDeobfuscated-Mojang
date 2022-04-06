/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class AttachedToLeavesDecorator
extends TreeDecorator {
    public static final Codec<AttachedToLeavesDecorator> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(attachedToLeavesDecorator -> Float.valueOf(attachedToLeavesDecorator.probability)), ((MapCodec)Codec.intRange(0, 16).fieldOf("exclusion_radius_xz")).forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.exclusionRadiusXZ), ((MapCodec)Codec.intRange(0, 16).fieldOf("exclusion_radius_y")).forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.exclusionRadiusY), ((MapCodec)BlockStateProvider.CODEC.fieldOf("block_provider")).forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.blockProvider), ((MapCodec)Codec.intRange(1, 16).fieldOf("required_empty_blocks")).forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.requiredEmptyBlocks), ((MapCodec)ExtraCodecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions")).forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.directions)).apply((Applicative<AttachedToLeavesDecorator, ?>)instance, AttachedToLeavesDecorator::new));
    protected final float probability;
    protected final int exclusionRadiusXZ;
    protected final int exclusionRadiusY;
    protected final BlockStateProvider blockProvider;
    protected final int requiredEmptyBlocks;
    protected final List<Direction> directions;

    public AttachedToLeavesDecorator(float f, int i, int j, BlockStateProvider blockStateProvider, int k, List<Direction> list) {
        this.probability = f;
        this.exclusionRadiusXZ = i;
        this.exclusionRadiusY = j;
        this.blockProvider = blockStateProvider;
        this.requiredEmptyBlocks = k;
        this.directions = list;
    }

    @Override
    public void place(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, List<BlockPos> list, List<BlockPos> list2, List<BlockPos> list3) {
        HashSet<BlockPos> set = new HashSet<BlockPos>();
        ArrayList<BlockPos> list4 = new ArrayList<BlockPos>(list2);
        Util.shuffle(list4, randomSource);
        for (BlockPos blockPos : list4) {
            Direction direction;
            BlockPos blockPos2 = blockPos.relative(direction = Util.getRandom(this.directions, randomSource));
            if (set.contains(blockPos2) || !(randomSource.nextFloat() < this.probability) || !this.hasRequiredEmptyBlocks(levelSimulatedReader, blockPos, direction)) continue;
            BlockPos blockPos3 = blockPos2.offset(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
            BlockPos blockPos4 = blockPos2.offset(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);
            for (BlockPos blockPos5 : BlockPos.betweenClosed(blockPos3, blockPos4)) {
                set.add(blockPos5.immutable());
            }
            biConsumer.accept(blockPos2, this.blockProvider.getState(randomSource, blockPos2));
        }
    }

    private boolean hasRequiredEmptyBlocks(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, Direction direction) {
        for (int i = 1; i <= this.requiredEmptyBlocks; ++i) {
            BlockPos blockPos2 = blockPos.relative(direction, i);
            if (Feature.isAir(levelSimulatedReader, blockPos2)) continue;
            return false;
        }
        return true;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.ATTACHED_TO_LEAVES;
    }
}

