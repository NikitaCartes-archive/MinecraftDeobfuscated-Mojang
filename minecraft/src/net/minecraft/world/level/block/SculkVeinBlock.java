package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;

public class SculkVeinBlock extends MultifaceBlock implements SculkBehaviour, SimpleWaterloggedBlock {
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public final MultifaceSpreader veinSpreader = new MultifaceSpreader(new SculkVeinBlock.SculkVeinSpreaderConfig(MultifaceSpreader.DEFAULT_SPREAD_ORDER));
	public final MultifaceSpreader sameSpaceSpreader = new MultifaceSpreader(
		new SculkVeinBlock.SculkVeinSpreaderConfig(new MultifaceSpreader.SpreadType[]{MultifaceSpreader.SpreadType.SAME_POSITION})
	);

	public boolean regrow(Level level, BlockPos blockPos, BlockState blockState, byte b) {
		boolean bl = false;
		BlockState blockState2 = this.defaultBlockState();

		for (Direction direction : Direction.unpack(b)) {
			BlockPos blockPos2 = blockPos.relative(direction);
			if (canAttachTo(level, direction, blockPos2, level.getBlockState(blockPos2))) {
				blockState2 = blockState2.setValue(getFaceProperty(direction), Boolean.valueOf(true));
				bl = true;
			}
		}

		if (bl) {
			if (!blockState.getFluidState().isEmpty()) {
				blockState2 = blockState2.setValue(WATERLOGGED, Boolean.valueOf(true));
			}

			level.setBlock(blockPos, blockState2, 3);
			return true;
		} else {
			return false;
		}
	}

	public SculkVeinBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	public void onDischarged(Level level, BlockState blockState, BlockPos blockPos, Random random) {
		if (blockState.is(this)) {
			for (Direction direction : Direction.values()) {
				BooleanProperty booleanProperty = getFaceProperty(direction);
				if ((Boolean)blockState.getValue(booleanProperty) && level.getBlockState(blockPos.relative(direction)).is(Blocks.SCULK)) {
					blockState = blockState.setValue(booleanProperty, Boolean.valueOf(false));
				}
			}

			if (hasAnyFace(blockState)) {
				level.setBlock(blockPos, blockState, 3);
			} else {
				FluidState fluidState = level.getFluidState(blockPos);
				blockState = (fluidState.isEmpty() ? Blocks.AIR : Blocks.WATER).defaultBlockState();
				level.setBlock(blockPos, blockState, 3);
			}

			SculkBehaviour.super.onDischarged(level, blockState, blockPos, random);
		}
	}

	@Override
	public short attemptUseCharge(SculkSpreader.ChargeCursor chargeCursor, Level level, BlockPos blockPos, Random random) {
		return this.attemptPlaceSculk(level, chargeCursor.getPos(), random)
			? (short)(chargeCursor.getCharge() - 1)
			: (short)(random.nextInt(10) == 0 ? Mth.floor((float)chargeCursor.getCharge() * 0.5F) : chargeCursor.getCharge());
	}

	private boolean attemptPlaceSculk(Level level, BlockPos blockPos, Random random) {
		BlockState blockState = level.getBlockState(blockPos);

		for (Direction direction : Direction.allShuffled(random)) {
			if (hasFace(blockState, direction)) {
				BlockPos blockPos2 = blockPos.relative(direction);
				if (level.getBlockState(blockPos2).is(BlockTags.SCULK_REPLACEABLE)) {
					BlockState blockState2 = Blocks.SCULK.defaultBlockState();
					level.setBlock(blockPos2, blockState2, 3);
					level.playSound(null, blockPos2, SoundEvents.SCULK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
					Direction direction2 = direction.getOpposite();

					for (Direction direction3 : Direction.values()) {
						if (direction3 != direction2) {
							BlockPos blockPos3 = blockPos2.relative(direction3);
							BlockState blockState3 = level.getBlockState(blockPos3);
							if (blockState3.is(this)) {
								this.onDischarged(level, blockState3, blockPos3, random);
							}
						}
					}

					this.veinSpreader.spreadAll(blockState2, level, blockPos2);
					return true;
				}
			}
		}

		return false;
	}

	public static boolean hasSubstrateAccess(Level level, BlockState blockState, BlockPos blockPos) {
		if (!blockState.is(Blocks.SCULK_VEIN)) {
			return false;
		} else {
			for (Direction direction : Direction.values()) {
				if (hasFace(blockState, direction) && level.getBlockState(blockPos.relative(direction)).is(BlockTags.SCULK_REPLACEABLE)) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public MultifaceSpreader getSpreader() {
		return this.veinSpreader;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(WATERLOGGED);
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		return !blockPlaceContext.getItemInHand().is(Items.SCULK_VEIN) || super.canBeReplaced(blockState, blockPlaceContext);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return PushReaction.DESTROY;
	}

	class SculkVeinSpreaderConfig extends MultifaceSpreader.DefaultSpreaderConfig {
		private final MultifaceSpreader.SpreadType[] spreadTypes;

		public SculkVeinSpreaderConfig(MultifaceSpreader.SpreadType[] spreadTypes) {
			super(SculkVeinBlock.this);
			this.spreadTypes = spreadTypes;
		}

		@Override
		public boolean stateCanBeReplaced(BlockGetter blockGetter, BlockPos blockPos, BlockPos blockPos2, Direction direction, BlockState blockState) {
			BlockState blockState2 = blockGetter.getBlockState(blockPos2.relative(direction));
			if (!blockState2.is(Blocks.SCULK) && !blockState2.is(Blocks.SCULK_CATALYST) && !blockState2.is(Blocks.MOVING_PISTON)) {
				if (blockPos.distManhattan(blockPos2) == 2) {
					BlockState blockState3 = blockGetter.getBlockState(blockPos.relative(direction.getOpposite()));
					if (blockState3.isFaceSturdy(blockGetter, blockPos.relative(direction.getOpposite()), direction)) {
						return false;
					}
				}

				return blockState.getFluidState().isEmpty() || !blockState.is(Blocks.LAVA) && blockState.getFluidState().isSource()
					? blockState.getMaterial().isReplaceable() || super.stateCanBeReplaced(blockGetter, blockPos, blockPos2, direction, blockState)
					: false;
			} else {
				return false;
			}
		}

		@Override
		public MultifaceSpreader.SpreadType[] getSpreadTypes() {
			return this.spreadTypes;
		}

		@Override
		public boolean isOtherBlockValidAsSource(BlockState blockState) {
			return !blockState.is(Blocks.SCULK_VEIN);
		}
	}
}
