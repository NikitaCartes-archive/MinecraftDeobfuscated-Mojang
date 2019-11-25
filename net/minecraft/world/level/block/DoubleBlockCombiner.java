/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class DoubleBlockCombiner {
    public static <S extends BlockEntity> NeighborCombineResult<S> combineWithNeigbour(BlockEntityType<S> blockEntityType, Function<BlockState, BlockType> function, Function<BlockState, Direction> function2, DirectionProperty directionProperty, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, BiPredicate<LevelAccessor, BlockPos> biPredicate) {
        BlockType blockType2;
        boolean bl2;
        S blockEntity = blockEntityType.getBlockEntity(levelAccessor, blockPos);
        if (blockEntity == null) {
            return Combiner::acceptNone;
        }
        if (biPredicate.test(levelAccessor, blockPos)) {
            return Combiner::acceptNone;
        }
        BlockType blockType = function.apply(blockState);
        boolean bl = blockType == BlockType.SINGLE;
        boolean bl3 = bl2 = blockType == BlockType.FIRST;
        if (bl) {
            return new NeighborCombineResult.Single<S>(blockEntity);
        }
        BlockPos blockPos2 = blockPos.relative(function2.apply(blockState));
        BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
        if (blockState2.getBlock() == blockState.getBlock() && (blockType2 = function.apply(blockState2)) != BlockType.SINGLE && blockType != blockType2 && blockState2.getValue(directionProperty) == blockState.getValue(directionProperty)) {
            if (biPredicate.test(levelAccessor, blockPos2)) {
                return Combiner::acceptNone;
            }
            S blockEntity2 = blockEntityType.getBlockEntity(levelAccessor, blockPos2);
            if (blockEntity2 != null) {
                S blockEntity3 = bl2 ? blockEntity : blockEntity2;
                S blockEntity4 = bl2 ? blockEntity2 : blockEntity;
                return new NeighborCombineResult.Double<S>(blockEntity3, blockEntity4);
            }
        }
        return new NeighborCombineResult.Single<S>(blockEntity);
    }

    public static interface NeighborCombineResult<S> {
        public <T> T apply(Combiner<? super S, T> var1);

        public static final class Single<S>
        implements NeighborCombineResult<S> {
            private final S single;

            public Single(S object) {
                this.single = object;
            }

            @Override
            public <T> T apply(Combiner<? super S, T> combiner) {
                return combiner.acceptSingle(this.single);
            }
        }

        public static final class Double<S>
        implements NeighborCombineResult<S> {
            private final S first;
            private final S second;

            public Double(S object, S object2) {
                this.first = object;
                this.second = object2;
            }

            @Override
            public <T> T apply(Combiner<? super S, T> combiner) {
                return combiner.acceptDouble(this.first, this.second);
            }
        }
    }

    public static interface Combiner<S, T> {
        public T acceptDouble(S var1, S var2);

        public T acceptSingle(S var1);

        public T acceptNone();
    }

    public static enum BlockType {
        SINGLE,
        FIRST,
        SECOND;

    }
}

