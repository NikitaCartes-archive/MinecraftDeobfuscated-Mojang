package net.minecraft.world.level.block;

import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;

public class SculkVeinBlock extends MultifaceBlock implements SculkBehaviour, SimpleWaterloggedBlock {
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private final MultifaceSpreader veinSpreader = new MultifaceSpreader(new SculkVeinBlock.SculkVeinSpreaderConfig(MultifaceSpreader.DEFAULT_SPREAD_ORDER));
	private final MultifaceSpreader sameSpaceSpreader = new MultifaceSpreader(
		new SculkVeinBlock.SculkVeinSpreaderConfig(MultifaceSpreader.SpreadType.SAME_POSITION)
	);

	public SculkVeinBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	public MultifaceSpreader getSpreader() {
		return this.veinSpreader;
	}

	public MultifaceSpreader getSameSpaceSpreader() {
		return this.sameSpaceSpreader;
	}

	public static boolean regrow(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Collection<Direction> collection) {
		boolean bl = false;
		BlockState blockState2 = Blocks.SCULK_VEIN.defaultBlockState();

		for (Direction direction : collection) {
			BlockPos blockPos2 = blockPos.relative(direction);
			if (canAttachTo(levelAccessor, direction, blockPos2, levelAccessor.getBlockState(blockPos2))) {
				blockState2 = blockState2.setValue(getFaceProperty(direction), Boolean.valueOf(true));
				bl = true;
			}
		}

		if (!bl) {
			return false;
		} else {
			if (!blockState.getFluidState().isEmpty()) {
				blockState2 = blockState2.setValue(WATERLOGGED, Boolean.valueOf(true));
			}

			levelAccessor.setBlock(blockPos, blockState2, 3);
			return true;
		}
	}

	@Override
	public void onDischarged(LevelAccessor levelAccessor, BlockState blockState, BlockPos blockPos, RandomSource randomSource) {
		if (blockState.is(this)) {
			for (Direction direction : DIRECTIONS) {
				BooleanProperty booleanProperty = getFaceProperty(direction);
				if ((Boolean)blockState.getValue(booleanProperty) && levelAccessor.getBlockState(blockPos.relative(direction)).is(Blocks.SCULK)) {
					blockState = blockState.setValue(booleanProperty, Boolean.valueOf(false));
				}
			}

			if (!hasAnyFace(blockState)) {
				FluidState fluidState = levelAccessor.getFluidState(blockPos);
				blockState = (fluidState.isEmpty() ? Blocks.AIR : Blocks.WATER).defaultBlockState();
			}

			levelAccessor.setBlock(blockPos, blockState, 3);
			SculkBehaviour.super.onDischarged(levelAccessor, blockState, blockPos, randomSource);
		}
	}

	@Override
	public int attemptUseCharge(
		SculkSpreader.ChargeCursor chargeCursor, LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, SculkSpreader sculkSpreader, boolean bl
	) {
		if (bl && this.attemptPlaceSculk(sculkSpreader, levelAccessor, chargeCursor.getPos(), randomSource)) {
			return chargeCursor.getCharge() - 1;
		} else {
			return randomSource.nextInt(sculkSpreader.chargeDecayRate()) == 0 ? Mth.floor((float)chargeCursor.getCharge() * 0.5F) : chargeCursor.getCharge();
		}
	}

	private boolean attemptPlaceSculk(SculkSpreader sculkSpreader, LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		TagKey<Block> tagKey = sculkSpreader.replaceableBlocks();

		for (Direction direction : Direction.allShuffled(randomSource)) {
			if (hasFace(blockState, direction)) {
				BlockPos blockPos2 = blockPos.relative(direction);
				if (levelAccessor.getBlockState(blockPos2).is(tagKey)) {
					BlockState blockState2 = Blocks.SCULK.defaultBlockState();
					levelAccessor.setBlock(blockPos2, blockState2, 3);
					levelAccessor.playSound(null, blockPos2, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
					this.veinSpreader.spreadAll(blockState2, levelAccessor, blockPos2, sculkSpreader.isWorldGeneration());
					Direction direction2 = direction.getOpposite();

					for (Direction direction3 : DIRECTIONS) {
						if (direction3 != direction2) {
							BlockPos blockPos3 = blockPos2.relative(direction3);
							BlockState blockState3 = levelAccessor.getBlockState(blockPos3);
							if (blockState3.is(this)) {
								this.onDischarged(levelAccessor, blockState3, blockPos3, randomSource);
							}
						}
					}

					return true;
				}
			}
		}

		return false;
	}

	public static boolean hasSubstrateAccess(LevelAccessor levelAccessor, BlockState blockState, BlockPos blockPos) {
		if (!blockState.is(Blocks.SCULK_VEIN)) {
			return false;
		} else {
			for (Direction direction : DIRECTIONS) {
				if (hasFace(blockState, direction) && levelAccessor.getBlockState(blockPos.relative(direction)).is(BlockTags.SCULK_REPLACEABLE)) {
					return true;
				}
			}

			return false;
		}
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

		public SculkVeinSpreaderConfig(MultifaceSpreader.SpreadType... spreadTypes) {
			super(SculkVeinBlock.this);
			this.spreadTypes = spreadTypes;
		}

		@Override
		public boolean stateCanBeReplaced(BlockGetter blockGetter, BlockPos blockPos, BlockPos blockPos2, Direction direction, BlockState blockState) {
			BlockState blockState2 = blockGetter.getBlockState(blockPos2.relative(direction));
			if (!blockState2.is(Blocks.SCULK) && !blockState2.is(Blocks.SCULK_CATALYST) && !blockState2.is(Blocks.MOVING_PISTON)) {
				if (blockPos.distManhattan(blockPos2) == 2) {
					BlockPos blockPos3 = blockPos.relative(direction.getOpposite());
					if (blockGetter.getBlockState(blockPos3).isFaceSturdy(blockGetter, blockPos3, direction)) {
						return false;
					}
				}

				FluidState fluidState = blockState.getFluidState();
				if (!fluidState.isEmpty() && !fluidState.is(Fluids.WATER)) {
					return false;
				} else {
					Material material = blockState.getMaterial();
					return material == Material.FIRE ? false : material.isReplaceable() || super.stateCanBeReplaced(blockGetter, blockPos, blockPos2, direction, blockState);
				}
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
