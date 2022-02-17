package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class MultifaceSpreader {
	public static final MultifaceSpreader.SpreadType[] DEFAULT_SPREAD_ORDER = new MultifaceSpreader.SpreadType[]{
		MultifaceSpreader.SpreadType.SAME_POSITION, MultifaceSpreader.SpreadType.SAME_PLANE, MultifaceSpreader.SpreadType.WRAP_AROUND
	};
	private final MultifaceSpreader.SpreadConfig config;

	public MultifaceSpreader(MultifaceBlock multifaceBlock) {
		this(new MultifaceSpreader.DefaultSpreaderConfig(multifaceBlock));
	}

	public MultifaceSpreader(MultifaceSpreader.SpreadConfig spreadConfig) {
		this.config = spreadConfig;
	}

	public boolean canSpreadInAnyDirection(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return Direction.stream()
			.anyMatch(
				direction2 -> this.getSpreadFromFaceTowardDirection(blockState, blockGetter, blockPos, direction, direction2, this.config::canSpreadInto).isPresent()
			);
	}

	public Optional<MultifaceSpreader.SpreadPos> spreadFromRandomFaceTowardRandomDirection(
		BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Random random
	) {
		return (Optional<MultifaceSpreader.SpreadPos>)Direction.allShuffled(random)
			.stream()
			.filter(direction -> this.config.canSpreadFrom(blockState, direction))
			.map(direction -> this.spreadFromFaceTowardRandomDirection(blockState, levelAccessor, blockPos, direction, random, false))
			.filter(Optional::isPresent)
			.findFirst()
			.orElse(Optional.empty());
	}

	public long spreadAll(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
		return (Long)Direction.stream()
			.filter(direction -> this.config.canSpreadFrom(blockState, direction))
			.map(direction -> this.spreadFromFaceTowardAllDirections(blockState, levelAccessor, blockPos, direction, false))
			.reduce(0L, Long::sum);
	}

	public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardRandomDirection(
		BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, Random random, boolean bl
	) {
		return (Optional<MultifaceSpreader.SpreadPos>)Direction.allShuffled(random)
			.stream()
			.map(direction2 -> this.spreadFromFaceTowardDirection(blockState, levelAccessor, blockPos, direction, direction2, bl))
			.filter(Optional::isPresent)
			.findFirst()
			.orElse(Optional.empty());
	}

	public long spreadFromFaceTowardAllDirections(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, boolean bl) {
		return Direction.stream()
			.map(direction2 -> this.spreadFromFaceTowardDirection(blockState, levelAccessor, blockPos, direction, direction2, bl))
			.filter(Optional::isPresent)
			.count();
	}

	public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardDirection(
		BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, Direction direction2, boolean bl
	) {
		Optional<MultifaceSpreader.SpreadPos> optional = this.getSpreadFromFaceTowardDirection(blockState, levelAccessor, blockPos, direction, direction2);
		if (optional.isPresent()) {
			MultifaceSpreader.SpreadPos spreadPos = (MultifaceSpreader.SpreadPos)optional.get();
			return this.spreadToFace(levelAccessor, spreadPos.pos, spreadPos.face, bl);
		} else {
			return Optional.empty();
		}
	}

	public Optional<MultifaceSpreader.SpreadPos> getSpreadFromFaceTowardDirection(
		BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction, Direction direction2
	) {
		return this.getSpreadFromFaceTowardDirection(blockState, blockGetter, blockPos, direction, direction2, this.config::canSpreadInto);
	}

	public Optional<MultifaceSpreader.SpreadPos> getSpreadFromFaceTowardDirection(
		BlockState blockState,
		BlockGetter blockGetter,
		BlockPos blockPos,
		Direction direction,
		Direction direction2,
		MultifaceSpreader.SpreadPredicate spreadPredicate
	) {
		if (direction2.getAxis() == direction.getAxis()) {
			return Optional.empty();
		} else if (this.config.isOtherBlockValidAsSource(blockState) || this.config.hasFace(blockState, direction) && !this.config.hasFace(blockState, direction2)) {
			for (MultifaceSpreader.SpreadType spreadType : this.config.getSpreadTypes()) {
				BlockPos blockPos2 = spreadType.getSpreadPosition(blockPos, direction2, direction);
				Direction direction3 = spreadType.getSpreadFace(direction2, direction);
				if (spreadPredicate.test(blockGetter, blockPos, blockPos2, direction3)) {
					return Optional.of(new MultifaceSpreader.SpreadPos(blockPos2, direction3));
				}
			}

			return Optional.empty();
		} else {
			return Optional.empty();
		}
	}

	public Optional<MultifaceSpreader.SpreadPos> spreadToFace(LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, boolean bl) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		return this.config.placeBlock(levelAccessor, blockPos, direction, blockState, bl)
			? Optional.of(new MultifaceSpreader.SpreadPos(blockPos, direction))
			: Optional.empty();
	}

	public static class DefaultSpreaderConfig implements MultifaceSpreader.SpreadConfig {
		protected MultifaceBlock block;

		public DefaultSpreaderConfig(MultifaceBlock multifaceBlock) {
			this.block = multifaceBlock;
		}

		@Nullable
		@Override
		public BlockState getStateForPlacement(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
			return this.block.getStateForPlacement(blockState, blockGetter, blockPos, direction);
		}

		protected boolean stateCanBeReplaced(BlockGetter blockGetter, BlockPos blockPos, BlockPos blockPos2, Direction direction, BlockState blockState) {
			return blockState.isAir() || blockState.is(this.block) || blockState.is(Blocks.WATER) && blockState.getFluidState().isSource();
		}

		@Override
		public boolean canSpreadInto(BlockGetter blockGetter, BlockPos blockPos, BlockPos blockPos2, Direction direction) {
			BlockState blockState = blockGetter.getBlockState(blockPos2);
			return this.stateCanBeReplaced(blockGetter, blockPos, blockPos2, direction, blockState)
				&& this.block.isValidStateForPlacement(blockGetter, blockState, blockPos2, direction);
		}
	}

	public interface SpreadConfig {
		@Nullable
		BlockState getStateForPlacement(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction);

		boolean canSpreadInto(BlockGetter blockGetter, BlockPos blockPos, BlockPos blockPos2, Direction direction);

		default MultifaceSpreader.SpreadType[] getSpreadTypes() {
			return MultifaceSpreader.DEFAULT_SPREAD_ORDER;
		}

		default boolean hasFace(BlockState blockState, Direction direction) {
			return MultifaceBlock.hasFace(blockState, direction);
		}

		default boolean isOtherBlockValidAsSource(BlockState blockState) {
			return false;
		}

		default boolean canSpreadFrom(BlockState blockState, Direction direction) {
			return this.isOtherBlockValidAsSource(blockState) || this.hasFace(blockState, direction);
		}

		default boolean placeBlock(LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, BlockState blockState, boolean bl) {
			BlockState blockState2 = this.getStateForPlacement(blockState, levelAccessor, blockPos, direction);
			if (blockState2 != null) {
				if (bl) {
					levelAccessor.getChunk(blockPos).markPosForPostprocessing(blockPos);
				}

				return levelAccessor.setBlock(blockPos, blockState2, 2);
			} else {
				return false;
			}
		}
	}

	public static record SpreadPos(BlockPos pos, Direction face) {
	}

	@FunctionalInterface
	public interface SpreadPredicate {
		boolean test(BlockGetter blockGetter, BlockPos blockPos, BlockPos blockPos2, Direction direction);
	}

	public static enum SpreadType {
		SAME_POSITION {
			@Override
			public BlockPos getSpreadPosition(BlockPos blockPos, Direction direction, Direction direction2) {
				return blockPos;
			}

			@Override
			public Direction getSpreadFace(Direction direction, Direction direction2) {
				return direction;
			}
		},
		SAME_PLANE {
			@Override
			public BlockPos getSpreadPosition(BlockPos blockPos, Direction direction, Direction direction2) {
				return blockPos.relative(direction);
			}

			@Override
			public Direction getSpreadFace(Direction direction, Direction direction2) {
				return direction2;
			}
		},
		WRAP_AROUND {
			@Override
			public BlockPos getSpreadPosition(BlockPos blockPos, Direction direction, Direction direction2) {
				return blockPos.relative(direction).relative(direction2);
			}

			@Override
			public Direction getSpreadFace(Direction direction, Direction direction2) {
				return direction.getOpposite();
			}
		};

		public abstract BlockPos getSpreadPosition(BlockPos blockPos, Direction direction, Direction direction2);

		public abstract Direction getSpreadFace(Direction direction, Direction direction2);
	}
}
