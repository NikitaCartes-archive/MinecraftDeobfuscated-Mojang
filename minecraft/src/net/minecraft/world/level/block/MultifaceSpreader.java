package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
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

	public long spreadAll(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, boolean bl) {
		return (Long)Direction.stream()
			.filter(direction -> this.config.canSpreadFrom(blockState, direction))
			.map(direction -> this.spreadFromFaceTowardAllDirections(blockState, levelAccessor, blockPos, direction, bl))
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

	private long spreadFromFaceTowardAllDirections(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, boolean bl) {
		return Direction.stream()
			.map(direction2 -> this.spreadFromFaceTowardDirection(blockState, levelAccessor, blockPos, direction, direction2, bl))
			.filter(Optional::isPresent)
			.count();
	}

	@VisibleForTesting
	public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardDirection(
		BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, Direction direction2, boolean bl
	) {
		return this.getSpreadFromFaceTowardDirection(blockState, levelAccessor, blockPos, direction, direction2, this.config::canSpreadInto)
			.flatMap(spreadPos -> this.spreadToFace(levelAccessor, spreadPos, bl));
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
				MultifaceSpreader.SpreadPos spreadPos = spreadType.getSpreadPos(blockPos, direction2, direction);
				if (spreadPredicate.test(blockGetter, blockPos, spreadPos)) {
					return Optional.of(spreadPos);
				}
			}

			return Optional.empty();
		} else {
			return Optional.empty();
		}
	}

	public Optional<MultifaceSpreader.SpreadPos> spreadToFace(LevelAccessor levelAccessor, MultifaceSpreader.SpreadPos spreadPos, boolean bl) {
		BlockState blockState = levelAccessor.getBlockState(spreadPos.pos());
		return this.config.placeBlock(levelAccessor, spreadPos, blockState, bl) ? Optional.of(spreadPos) : Optional.empty();
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
		public boolean canSpreadInto(BlockGetter blockGetter, BlockPos blockPos, MultifaceSpreader.SpreadPos spreadPos) {
			BlockState blockState = blockGetter.getBlockState(spreadPos.pos());
			return this.stateCanBeReplaced(blockGetter, blockPos, spreadPos.pos(), spreadPos.face(), blockState)
				&& this.block.isValidStateForPlacement(blockGetter, blockState, spreadPos.pos(), spreadPos.face());
		}
	}

	public interface SpreadConfig {
		@Nullable
		BlockState getStateForPlacement(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction);

		boolean canSpreadInto(BlockGetter blockGetter, BlockPos blockPos, MultifaceSpreader.SpreadPos spreadPos);

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

		default boolean placeBlock(LevelAccessor levelAccessor, MultifaceSpreader.SpreadPos spreadPos, BlockState blockState, boolean bl) {
			BlockState blockState2 = this.getStateForPlacement(blockState, levelAccessor, spreadPos.pos(), spreadPos.face());
			if (blockState2 != null) {
				if (bl) {
					levelAccessor.getChunk(spreadPos.pos()).markPosForPostprocessing(spreadPos.pos());
				}

				return levelAccessor.setBlock(spreadPos.pos(), blockState2, 2);
			} else {
				return false;
			}
		}
	}

	public static record SpreadPos(BlockPos pos, Direction face) {
	}

	@FunctionalInterface
	public interface SpreadPredicate {
		boolean test(BlockGetter blockGetter, BlockPos blockPos, MultifaceSpreader.SpreadPos spreadPos);
	}

	public static enum SpreadType {
		SAME_POSITION {
			@Override
			public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos blockPos, Direction direction, Direction direction2) {
				return new MultifaceSpreader.SpreadPos(blockPos, direction);
			}
		},
		SAME_PLANE {
			@Override
			public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos blockPos, Direction direction, Direction direction2) {
				return new MultifaceSpreader.SpreadPos(blockPos.relative(direction), direction2);
			}
		},
		WRAP_AROUND {
			@Override
			public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos blockPos, Direction direction, Direction direction2) {
				return new MultifaceSpreader.SpreadPos(blockPos.relative(direction).relative(direction2), direction.getOpposite());
			}
		};

		public abstract MultifaceSpreader.SpreadPos getSpreadPos(BlockPos blockPos, Direction direction, Direction direction2);
	}
}
