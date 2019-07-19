package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonBaseBlock extends DirectionalBlock {
	public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
	protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
	protected static final VoxelShape WEST_AABB = Block.box(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
	protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape UP_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
	protected static final VoxelShape DOWN_AABB = Block.box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
	private final boolean isSticky;

	public PistonBaseBlock(boolean bl, Block.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EXTENDED, Boolean.valueOf(false)));
		this.isSticky = bl;
	}

	@Override
	public boolean isViewBlocking(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return !(Boolean)blockState.getValue(EXTENDED);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		if ((Boolean)blockState.getValue(EXTENDED)) {
			switch ((Direction)blockState.getValue(FACING)) {
				case DOWN:
					return DOWN_AABB;
				case UP:
				default:
					return UP_AABB;
				case NORTH:
					return NORTH_AABB;
				case SOUTH:
					return SOUTH_AABB;
				case WEST:
					return WEST_AABB;
				case EAST:
					return EAST_AABB;
			}
		} else {
			return Shapes.block();
		}
	}

	@Override
	public boolean isRedstoneConductor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if (!level.isClientSide) {
			this.checkIfExtend(level, blockPos, blockState);
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (!level.isClientSide) {
			this.checkIfExtend(level, blockPos, blockState);
		}
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (blockState2.getBlock() != blockState.getBlock()) {
			if (!level.isClientSide && level.getBlockEntity(blockPos) == null) {
				this.checkIfExtend(level, blockPos, blockState);
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite()).setValue(EXTENDED, Boolean.valueOf(false));
	}

	private void checkIfExtend(Level level, BlockPos blockPos, BlockState blockState) {
		Direction direction = blockState.getValue(FACING);
		boolean bl = this.getNeighborSignal(level, blockPos, direction);
		if (bl && !(Boolean)blockState.getValue(EXTENDED)) {
			if (new PistonStructureResolver(level, blockPos, direction, true).resolve()) {
				level.blockEvent(blockPos, this, 0, direction.get3DDataValue());
			}
		} else if (!bl && (Boolean)blockState.getValue(EXTENDED)) {
			BlockPos blockPos2 = blockPos.relative(direction, 2);
			BlockState blockState2 = level.getBlockState(blockPos2);
			int i = 1;
			if (blockState2.getBlock() == Blocks.MOVING_PISTON && blockState2.getValue(FACING) == direction) {
				BlockEntity blockEntity = level.getBlockEntity(blockPos2);
				if (blockEntity instanceof PistonMovingBlockEntity) {
					PistonMovingBlockEntity pistonMovingBlockEntity = (PistonMovingBlockEntity)blockEntity;
					if (pistonMovingBlockEntity.isExtending()
						&& (
							pistonMovingBlockEntity.getProgress(0.0F) < 0.5F
								|| level.getGameTime() == pistonMovingBlockEntity.getLastTicked()
								|| ((ServerLevel)level).isHandlingTick()
						)) {
						i = 2;
					}
				}
			}

			level.blockEvent(blockPos, this, i, direction.get3DDataValue());
		}
	}

	private boolean getNeighborSignal(Level level, BlockPos blockPos, Direction direction) {
		for (Direction direction2 : Direction.values()) {
			if (direction2 != direction && level.hasSignal(blockPos.relative(direction2), direction2)) {
				return true;
			}
		}

		if (level.hasSignal(blockPos, Direction.DOWN)) {
			return true;
		} else {
			BlockPos blockPos2 = blockPos.above();

			for (Direction direction3 : Direction.values()) {
				if (direction3 != Direction.DOWN && level.hasSignal(blockPos2.relative(direction3), direction3)) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
		Direction direction = blockState.getValue(FACING);
		if (!level.isClientSide) {
			boolean bl = this.getNeighborSignal(level, blockPos, direction);
			if (bl && (i == 1 || i == 2)) {
				level.setBlock(blockPos, blockState.setValue(EXTENDED, Boolean.valueOf(true)), 2);
				return false;
			}

			if (!bl && i == 0) {
				return false;
			}
		}

		if (i == 0) {
			if (!this.moveBlocks(level, blockPos, direction, true)) {
				return false;
			}

			level.setBlock(blockPos, blockState.setValue(EXTENDED, Boolean.valueOf(true)), 67);
			level.playSound(null, blockPos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.25F + 0.6F);
		} else if (i == 1 || i == 2) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos.relative(direction));
			if (blockEntity instanceof PistonMovingBlockEntity) {
				((PistonMovingBlockEntity)blockEntity).finalTick();
			}

			level.setBlock(
				blockPos,
				Blocks.MOVING_PISTON
					.defaultBlockState()
					.setValue(MovingPistonBlock.FACING, direction)
					.setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT),
				3
			);
			level.setBlockEntity(
				blockPos, MovingPistonBlock.newMovingBlockEntity(this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(j & 7)), direction, false, true)
			);
			if (this.isSticky) {
				BlockPos blockPos2 = blockPos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
				BlockState blockState2 = level.getBlockState(blockPos2);
				Block block = blockState2.getBlock();
				boolean bl2 = false;
				if (block == Blocks.MOVING_PISTON) {
					BlockEntity blockEntity2 = level.getBlockEntity(blockPos2);
					if (blockEntity2 instanceof PistonMovingBlockEntity) {
						PistonMovingBlockEntity pistonMovingBlockEntity = (PistonMovingBlockEntity)blockEntity2;
						if (pistonMovingBlockEntity.getDirection() == direction && pistonMovingBlockEntity.isExtending()) {
							pistonMovingBlockEntity.finalTick();
							bl2 = true;
						}
					}
				}

				if (!bl2) {
					if (i != 1
						|| blockState2.isAir()
						|| !isPushable(blockState2, level, blockPos2, direction.getOpposite(), false, direction)
						|| blockState2.getPistonPushReaction() != PushReaction.NORMAL && block != Blocks.PISTON && block != Blocks.STICKY_PISTON) {
						level.removeBlock(blockPos.relative(direction), false);
					} else {
						this.moveBlocks(level, blockPos, direction, false);
					}
				}
			} else {
				level.removeBlock(blockPos.relative(direction), false);
			}

			level.playSound(null, blockPos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.15F + 0.6F);
		}

		return true;
	}

	public static boolean isPushable(BlockState blockState, Level level, BlockPos blockPos, Direction direction, boolean bl, Direction direction2) {
		Block block = blockState.getBlock();
		if (block == Blocks.OBSIDIAN) {
			return false;
		} else if (!level.getWorldBorder().isWithinBounds(blockPos)) {
			return false;
		} else if (blockPos.getY() >= 0 && (direction != Direction.DOWN || blockPos.getY() != 0)) {
			if (blockPos.getY() <= level.getMaxBuildHeight() - 1 && (direction != Direction.UP || blockPos.getY() != level.getMaxBuildHeight() - 1)) {
				if (block != Blocks.PISTON && block != Blocks.STICKY_PISTON) {
					if (blockState.getDestroySpeed(level, blockPos) == -1.0F) {
						return false;
					}

					switch (blockState.getPistonPushReaction()) {
						case BLOCK:
							return false;
						case DESTROY:
							return bl;
						case PUSH_ONLY:
							return direction == direction2;
					}
				} else if ((Boolean)blockState.getValue(EXTENDED)) {
					return false;
				}

				return !block.isEntityBlock();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean moveBlocks(Level level, BlockPos blockPos, Direction direction, boolean bl) {
		BlockPos blockPos2 = blockPos.relative(direction);
		if (!bl && level.getBlockState(blockPos2).getBlock() == Blocks.PISTON_HEAD) {
			level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 20);
		}

		PistonStructureResolver pistonStructureResolver = new PistonStructureResolver(level, blockPos, direction, bl);
		if (!pistonStructureResolver.resolve()) {
			return false;
		} else {
			List<BlockPos> list = pistonStructureResolver.getToPush();
			List<BlockState> list2 = Lists.<BlockState>newArrayList();

			for (int i = 0; i < list.size(); i++) {
				BlockPos blockPos3 = (BlockPos)list.get(i);
				list2.add(level.getBlockState(blockPos3));
			}

			List<BlockPos> list3 = pistonStructureResolver.getToDestroy();
			int j = list.size() + list3.size();
			BlockState[] blockStates = new BlockState[j];
			Direction direction2 = bl ? direction : direction.getOpposite();
			Set<BlockPos> set = Sets.<BlockPos>newHashSet(list);

			for (int k = list3.size() - 1; k >= 0; k--) {
				BlockPos blockPos4 = (BlockPos)list3.get(k);
				BlockState blockState = level.getBlockState(blockPos4);
				BlockEntity blockEntity = blockState.getBlock().isEntityBlock() ? level.getBlockEntity(blockPos4) : null;
				dropResources(blockState, level, blockPos4, blockEntity);
				level.setBlock(blockPos4, Blocks.AIR.defaultBlockState(), 18);
				j--;
				blockStates[j] = blockState;
			}

			for (int k = list.size() - 1; k >= 0; k--) {
				BlockPos blockPos4 = (BlockPos)list.get(k);
				BlockState blockState = level.getBlockState(blockPos4);
				blockPos4 = blockPos4.relative(direction2);
				set.remove(blockPos4);
				level.setBlock(blockPos4, Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, direction), 68);
				level.setBlockEntity(blockPos4, MovingPistonBlock.newMovingBlockEntity((BlockState)list2.get(k), direction, bl, false));
				j--;
				blockStates[j] = blockState;
			}

			if (bl) {
				PistonType pistonType = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
				BlockState blockState2 = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, direction).setValue(PistonHeadBlock.TYPE, pistonType);
				BlockState blockState = Blocks.MOVING_PISTON
					.defaultBlockState()
					.setValue(MovingPistonBlock.FACING, direction)
					.setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
				set.remove(blockPos2);
				level.setBlock(blockPos2, blockState, 68);
				level.setBlockEntity(blockPos2, MovingPistonBlock.newMovingBlockEntity(blockState2, direction, true, true));
			}

			for (BlockPos blockPos4 : set) {
				level.setBlock(blockPos4, Blocks.AIR.defaultBlockState(), 66);
			}

			for (int k = list3.size() - 1; k >= 0; k--) {
				BlockState blockState2 = blockStates[j++];
				BlockPos blockPos5 = (BlockPos)list3.get(k);
				blockState2.updateIndirectNeighbourShapes(level, blockPos5, 2);
				level.updateNeighborsAt(blockPos5, blockState2.getBlock());
			}

			for (int k = list.size() - 1; k >= 0; k--) {
				level.updateNeighborsAt((BlockPos)list.get(k), blockStates[j++].getBlock());
			}

			if (bl) {
				level.updateNeighborsAt(blockPos2, Blocks.PISTON_HEAD);
			}

			return true;
		}
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, EXTENDED);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return (Boolean)blockState.getValue(EXTENDED);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
