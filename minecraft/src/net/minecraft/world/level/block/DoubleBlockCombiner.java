package net.minecraft.world.level.block;

import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class DoubleBlockCombiner {
	public static <S extends BlockEntity> DoubleBlockCombiner.NeighborCombineResult<S> combineWithNeigbour(
		BlockEntityType<S> blockEntityType,
		Function<BlockState, DoubleBlockCombiner.BlockType> function,
		Function<BlockState, Direction> function2,
		Property<Direction> property,
		BlockState blockState,
		LevelAccessor levelAccessor,
		BlockPos blockPos,
		BiPredicate<LevelAccessor, BlockPos> biPredicate
	) {
		S blockEntity = blockEntityType.getBlockEntity(levelAccessor, blockPos);
		if (blockEntity == null) {
			return DoubleBlockCombiner.Combiner::acceptNone;
		} else if (biPredicate.test(levelAccessor, blockPos)) {
			return DoubleBlockCombiner.Combiner::acceptNone;
		} else {
			DoubleBlockCombiner.BlockType blockType = (DoubleBlockCombiner.BlockType)function.apply(blockState);
			boolean bl = blockType == DoubleBlockCombiner.BlockType.SINGLE;
			boolean bl2 = blockType == DoubleBlockCombiner.BlockType.FIRST;
			if (bl) {
				return new DoubleBlockCombiner.NeighborCombineResult.Single<>(blockEntity);
			} else {
				BlockPos blockPos2 = blockPos.relative((Direction)function2.apply(blockState));
				BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
				if (blockState2.is(blockState.getBlock())) {
					DoubleBlockCombiner.BlockType blockType2 = (DoubleBlockCombiner.BlockType)function.apply(blockState2);
					if (blockType2 != DoubleBlockCombiner.BlockType.SINGLE && blockType != blockType2 && blockState2.getValue(property) == blockState.getValue(property)) {
						if (biPredicate.test(levelAccessor, blockPos2)) {
							return DoubleBlockCombiner.Combiner::acceptNone;
						}

						S blockEntity2 = blockEntityType.getBlockEntity(levelAccessor, blockPos2);
						if (blockEntity2 != null) {
							S blockEntity3 = bl2 ? blockEntity : blockEntity2;
							S blockEntity4 = bl2 ? blockEntity2 : blockEntity;
							return new DoubleBlockCombiner.NeighborCombineResult.Double<>(blockEntity3, blockEntity4);
						}
					}
				}

				return new DoubleBlockCombiner.NeighborCombineResult.Single<>(blockEntity);
			}
		}
	}

	public static enum BlockType {
		SINGLE,
		FIRST,
		SECOND;
	}

	public interface Combiner<S, T> {
		T acceptDouble(S object, S object2);

		T acceptSingle(S object);

		T acceptNone();
	}

	public interface NeighborCombineResult<S> {
		<T> T apply(DoubleBlockCombiner.Combiner<? super S, T> combiner);

		public static final class Double<S> implements DoubleBlockCombiner.NeighborCombineResult<S> {
			private final S first;
			private final S second;

			public Double(S object, S object2) {
				this.first = object;
				this.second = object2;
			}

			@Override
			public <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> combiner) {
				return combiner.acceptDouble(this.first, this.second);
			}
		}

		public static final class Single<S> implements DoubleBlockCombiner.NeighborCombineResult<S> {
			private final S single;

			public Single(S object) {
				this.single = object;
			}

			@Override
			public <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> combiner) {
				return combiner.acceptSingle(this.single);
			}
		}
	}
}
