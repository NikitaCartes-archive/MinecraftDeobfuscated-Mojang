package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrapDoorBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<TrapDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter(trapDoorBlock -> trapDoorBlock.type), propertiesCodec())
				.apply(instance, TrapDoorBlock::new)
	);
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final int AABB_THICKNESS = 3;
	protected static final VoxelShape EAST_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
	protected static final VoxelShape WEST_OPEN_AABB = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	protected static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
	protected static final VoxelShape TOP_AABB = Block.box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
	private final BlockSetType type;

	@Override
	public MapCodec<? extends TrapDoorBlock> codec() {
		return CODEC;
	}

	protected TrapDoorBlock(BlockSetType blockSetType, BlockBehaviour.Properties properties) {
		super(properties.sound(blockSetType.soundType()));
		this.type = blockSetType;
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(OPEN, Boolean.valueOf(false))
				.setValue(HALF, Half.BOTTOM)
				.setValue(POWERED, Boolean.valueOf(false))
				.setValue(WATERLOGGED, Boolean.valueOf(false))
		);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		if (!(Boolean)blockState.getValue(OPEN)) {
			return blockState.getValue(HALF) == Half.TOP ? TOP_AABB : BOTTOM_AABB;
		} else {
			switch ((Direction)blockState.getValue(FACING)) {
				case NORTH:
				default:
					return NORTH_OPEN_AABB;
				case SOUTH:
					return SOUTH_OPEN_AABB;
				case WEST:
					return WEST_OPEN_AABB;
				case EAST:
					return EAST_OPEN_AABB;
			}
		}
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		switch (pathComputationType) {
			case LAND:
				return (Boolean)blockState.getValue(OPEN);
			case WATER:
				return (Boolean)blockState.getValue(WATERLOGGED);
			case AIR:
				return (Boolean)blockState.getValue(OPEN);
			default:
				return false;
		}
	}

	@Override
	public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (!this.type.canOpenByHand()) {
			return InteractionResult.PASS;
		} else {
			this.toggle(blockState, level, blockPos, player);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	@Override
	public void onExplosionHit(BlockState blockState, Level level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
		if (explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK
			&& !level.isClientSide()
			&& this.type.canOpenByWindCharge()
			&& !(Boolean)blockState.getValue(POWERED)) {
			this.toggle(blockState, level, blockPos, null);
		}

		super.onExplosionHit(blockState, level, blockPos, explosion, biConsumer);
	}

	private void toggle(BlockState blockState, Level level, BlockPos blockPos, @Nullable Player player) {
		BlockState blockState2 = blockState.cycle(OPEN);
		level.setBlock(blockPos, blockState2, 2);
		if ((Boolean)blockState2.getValue(WATERLOGGED)) {
			level.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}

		this.playSound(player, level, blockPos, (Boolean)blockState2.getValue(OPEN));
	}

	protected void playSound(@Nullable Player player, Level level, BlockPos blockPos, boolean bl) {
		level.playSound(
			player, blockPos, bl ? this.type.trapdoorOpen() : this.type.trapdoorClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F
		);
		level.gameEvent(player, bl ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (!level.isClientSide) {
			boolean bl2 = level.hasNeighborSignal(blockPos);
			if (bl2 != (Boolean)blockState.getValue(POWERED)) {
				if ((Boolean)blockState.getValue(OPEN) != bl2) {
					blockState = blockState.setValue(OPEN, Boolean.valueOf(bl2));
					this.playSound(null, level, blockPos, bl2);
				}

				level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(bl2)), 2);
				if ((Boolean)blockState.getValue(WATERLOGGED)) {
					level.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
				}
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = this.defaultBlockState();
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		Direction direction = blockPlaceContext.getClickedFace();
		if (!blockPlaceContext.replacingClickedOnBlock() && direction.getAxis().isHorizontal()) {
			blockState = blockState.setValue(FACING, direction)
				.setValue(HALF, blockPlaceContext.getClickLocation().y - (double)blockPlaceContext.getClickedPos().getY() > 0.5 ? Half.TOP : Half.BOTTOM);
		} else {
			blockState = blockState.setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())
				.setValue(HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP);
		}

		if (blockPlaceContext.getLevel().hasNeighborSignal(blockPlaceContext.getClickedPos())) {
			blockState = blockState.setValue(OPEN, Boolean.valueOf(true)).setValue(POWERED, Boolean.valueOf(true));
		}

		return blockState.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, OPEN, HALF, POWERED, WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
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

	protected BlockSetType getType() {
		return this.type;
	}
}
