package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonBaseBlock extends DirectionalBlock {
	public static final MapCodec<PistonBaseBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Codec.BOOL.fieldOf("sticky").forGetter(pistonBaseBlock -> pistonBaseBlock.isSticky), propertiesCodec())
				.apply(instance, PistonBaseBlock::new)
	);
	public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
	public static final int TRIGGER_EXTEND = 0;
	public static final int TRIGGER_CONTRACT = 1;
	public static final int TRIGGER_DROP = 2;
	public static final float PLATFORM_THICKNESS = 4.0F;
	protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
	protected static final VoxelShape WEST_AABB = Block.box(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
	protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape UP_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
	protected static final VoxelShape DOWN_AABB = Block.box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
	private final boolean isSticky;

	@Override
	public MapCodec<PistonBaseBlock> codec() {
		return CODEC;
	}

	public PistonBaseBlock(boolean bl, BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EXTENDED, Boolean.valueOf(false)));
		this.isSticky = bl;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
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
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if (!level.isClientSide) {
			this.checkIfExtend(level, blockPos, blockState);
		}
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (!level.isClientSide) {
			this.checkIfExtend(level, blockPos, blockState);
		}
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
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
			if (blockState2.is(Blocks.MOVING_PISTON)
				&& blockState2.getValue(FACING) == direction
				&& level.getBlockEntity(blockPos2) instanceof PistonMovingBlockEntity pistonMovingBlockEntity
				&& pistonMovingBlockEntity.isExtending()
				&& (
					pistonMovingBlockEntity.getProgress(0.0F) < 0.5F
						|| level.getGameTime() == pistonMovingBlockEntity.getLastTicked()
						|| ((ServerLevel)level).isHandlingTick()
				)) {
				i = 2;
			}

			level.blockEvent(blockPos, this, i, direction.get3DDataValue());
		}
	}

	private boolean getNeighborSignal(SignalGetter signalGetter, BlockPos blockPos, Direction direction) {
		for (Direction direction2 : Direction.values()) {
			if (direction2 != direction && signalGetter.hasSignal(blockPos.relative(direction2), direction2)) {
				return true;
			}
		}

		if (signalGetter.hasSignal(blockPos, Direction.DOWN)) {
			return true;
		} else {
			BlockPos blockPos2 = blockPos.above();

			for (Direction direction3 : Direction.values()) {
				if (direction3 != Direction.DOWN && signalGetter.hasSignal(blockPos2.relative(direction3), direction3)) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	protected boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
		Direction direction = blockState.getValue(FACING);
		BlockState blockState2 = blockState.setValue(EXTENDED, Boolean.valueOf(true));
		if (!level.isClientSide) {
			boolean bl = this.getNeighborSignal(level, blockPos, direction);
			if (bl && (i == 1 || i == 2)) {
				level.setBlock(blockPos, blockState2, 2);
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

			level.setBlock(blockPos, blockState2, 67);
			level.playSound(null, blockPos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.25F + 0.6F);
			level.gameEvent(GameEvent.BLOCK_ACTIVATE, blockPos, GameEvent.Context.of(blockState2));
		} else if (i == 1 || i == 2) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos.relative(direction));
			if (blockEntity instanceof PistonMovingBlockEntity) {
				((PistonMovingBlockEntity)blockEntity).finalTick();
			}

			BlockState blockState3 = Blocks.MOVING_PISTON
				.defaultBlockState()
				.setValue(MovingPistonBlock.FACING, direction)
				.setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
			level.setBlock(blockPos, blockState3, 20);
			level.setBlockEntity(
				MovingPistonBlock.newMovingBlockEntity(
					blockPos, blockState3, this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(j & 7)), direction, false, true
				)
			);
			level.blockUpdated(blockPos, blockState3.getBlock());
			blockState3.updateNeighbourShapes(level, blockPos, 2);
			if (this.isSticky) {
				BlockPos blockPos2 = blockPos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
				BlockState blockState4 = level.getBlockState(blockPos2);
				boolean bl2 = false;
				if (blockState4.is(Blocks.MOVING_PISTON)
					&& level.getBlockEntity(blockPos2) instanceof PistonMovingBlockEntity pistonMovingBlockEntity
					&& pistonMovingBlockEntity.getDirection() == direction
					&& pistonMovingBlockEntity.isExtending()) {
					pistonMovingBlockEntity.finalTick();
					bl2 = true;
				}

				if (!bl2) {
					if (i != 1
						|| blockState4.isAir()
						|| !isPushable(blockState4, level, blockPos2, direction.getOpposite(), false, direction)
						|| blockState4.getPistonPushReaction() != PushReaction.NORMAL && !blockState4.is(Blocks.PISTON) && !blockState4.is(Blocks.STICKY_PISTON)) {
						level.removeBlock(blockPos.relative(direction), false);
					} else {
						this.moveBlocks(level, blockPos, direction, false);
					}
				}
			} else {
				level.removeBlock(blockPos.relative(direction), false);
			}

			level.playSound(null, blockPos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.15F + 0.6F);
			level.gameEvent(GameEvent.BLOCK_DEACTIVATE, blockPos, GameEvent.Context.of(blockState3));
		}

		return true;
	}

	public static boolean isPushable(BlockState blockState, Level level, BlockPos blockPos, Direction direction, boolean bl, Direction direction2) {
		if (blockPos.getY() < level.getMinBuildHeight() || blockPos.getY() > level.getMaxBuildHeight() - 1 || !level.getWorldBorder().isWithinBounds(blockPos)) {
			return false;
		} else if (blockState.isAir()) {
			return true;
		} else if (blockState.is(Blocks.OBSIDIAN)
			|| blockState.is(Blocks.CRYING_OBSIDIAN)
			|| blockState.is(Blocks.RESPAWN_ANCHOR)
			|| blockState.is(Blocks.REINFORCED_DEEPSLATE)) {
			return false;
		} else if (direction == Direction.DOWN && blockPos.getY() == level.getMinBuildHeight()) {
			return false;
		} else if (direction == Direction.UP && blockPos.getY() == level.getMaxBuildHeight() - 1) {
			return false;
		} else {
			if (!blockState.is(Blocks.PISTON) && !blockState.is(Blocks.STICKY_PISTON)) {
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

			return !blockState.hasBlockEntity();
		}
	}

	private boolean moveBlocks(Level level, BlockPos blockPos, Direction direction, boolean bl) {
		BlockPos blockPos2 = blockPos.relative(direction);
		if (!bl && level.getBlockState(blockPos2).is(Blocks.PISTON_HEAD)) {
			level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 20);
		}

		PistonStructureResolver pistonStructureResolver = new PistonStructureResolver(level, blockPos, direction, bl);
		if (!pistonStructureResolver.resolve()) {
			return false;
		} else {
			Map<BlockPos, BlockState> map = Maps.<BlockPos, BlockState>newHashMap();
			List<BlockPos> list = pistonStructureResolver.getToPush();
			List<BlockState> list2 = Lists.<BlockState>newArrayList();

			for (BlockPos blockPos3 : list) {
				BlockState blockState = level.getBlockState(blockPos3);
				list2.add(blockState);
				map.put(blockPos3, blockState);
			}

			List<BlockPos> list3 = pistonStructureResolver.getToDestroy();
			BlockState[] blockStates = new BlockState[list.size() + list3.size()];
			Direction direction2 = bl ? direction : direction.getOpposite();
			int i = 0;

			for (int j = list3.size() - 1; j >= 0; j--) {
				BlockPos blockPos4 = (BlockPos)list3.get(j);
				BlockState blockState2 = level.getBlockState(blockPos4);
				BlockEntity blockEntity = blockState2.hasBlockEntity() ? level.getBlockEntity(blockPos4) : null;
				dropResources(blockState2, level, blockPos4, blockEntity);
				level.setBlock(blockPos4, Blocks.AIR.defaultBlockState(), 18);
				level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos4, GameEvent.Context.of(blockState2));
				if (!blockState2.is(BlockTags.FIRE)) {
					level.addDestroyBlockEffect(blockPos4, blockState2);
				}

				blockStates[i++] = blockState2;
			}

			for (int j = list.size() - 1; j >= 0; j--) {
				BlockPos blockPos4 = (BlockPos)list.get(j);
				BlockState blockState2 = level.getBlockState(blockPos4);
				blockPos4 = blockPos4.relative(direction2);
				map.remove(blockPos4);
				BlockState blockState3 = Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, direction);
				level.setBlock(blockPos4, blockState3, 68);
				level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos4, blockState3, (BlockState)list2.get(j), direction, bl, false));
				blockStates[i++] = blockState2;
			}

			if (bl) {
				PistonType pistonType = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
				BlockState blockState4 = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, direction).setValue(PistonHeadBlock.TYPE, pistonType);
				BlockState blockState2 = Blocks.MOVING_PISTON
					.defaultBlockState()
					.setValue(MovingPistonBlock.FACING, direction)
					.setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
				map.remove(blockPos2);
				level.setBlock(blockPos2, blockState2, 68);
				level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos2, blockState2, blockState4, direction, true, true));
			}

			BlockState blockState5 = Blocks.AIR.defaultBlockState();

			for (BlockPos blockPos5 : map.keySet()) {
				level.setBlock(blockPos5, blockState5, 82);
			}

			for (Entry<BlockPos, BlockState> entry : map.entrySet()) {
				BlockPos blockPos6 = (BlockPos)entry.getKey();
				BlockState blockState6 = (BlockState)entry.getValue();
				blockState6.updateIndirectNeighbourShapes(level, blockPos6, 2);
				blockState5.updateNeighbourShapes(level, blockPos6, 2);
				blockState5.updateIndirectNeighbourShapes(level, blockPos6, 2);
			}

			i = 0;

			for (int k = list3.size() - 1; k >= 0; k--) {
				BlockState blockState2 = blockStates[i++];
				BlockPos blockPos6 = (BlockPos)list3.get(k);
				blockState2.updateIndirectNeighbourShapes(level, blockPos6, 2);
				level.updateNeighborsAt(blockPos6, blockState2.getBlock());
			}

			for (int k = list.size() - 1; k >= 0; k--) {
				level.updateNeighborsAt((BlockPos)list.get(k), blockStates[i++].getBlock());
			}

			if (bl) {
				level.updateNeighborsAt(blockPos2, Blocks.PISTON_HEAD);
			}

			return true;
		}
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, EXTENDED);
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState blockState) {
		return (Boolean)blockState.getValue(EXTENDED);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}
}
