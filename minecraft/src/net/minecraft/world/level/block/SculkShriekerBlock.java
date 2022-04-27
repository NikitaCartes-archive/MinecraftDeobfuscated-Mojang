package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkShriekerBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final BooleanProperty SHRIEKING = BlockStateProperties.SHRIEKING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty CAN_SUMMON = BlockStateProperties.CAN_SUMMON;
	protected static final VoxelShape COLLIDER = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
	public static final double TOP_Y = COLLIDER.max(Direction.Axis.Y);

	public SculkShriekerBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(SHRIEKING, Boolean.valueOf(false))
				.setValue(WATERLOGGED, Boolean.valueOf(false))
				.setValue(CAN_SUMMON, Boolean.valueOf(false))
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SHRIEKING);
		builder.add(WATERLOGGED);
		builder.add(CAN_SUMMON);
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
		Entity entity2;
		if (entity instanceof Player) {
			entity2 = entity;
		} else if (entity.getControllingPassenger() instanceof Player) {
			entity2 = entity.getControllingPassenger();
		} else {
			entity2 = null;
		}

		if (level instanceof ServerLevel serverLevel && entity2 != null) {
			serverLevel.getBlockEntity(blockPos, BlockEntityType.SCULK_SHRIEKER)
				.ifPresent(sculkShriekerBlockEntity -> sculkShriekerBlockEntity.shriek(serverLevel, entity2));
		}

		super.stepOn(level, blockPos, blockState, entity);
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (level instanceof ServerLevel serverLevel && (Boolean)blockState.getValue(SHRIEKING) && !blockState.is(blockState2.getBlock())) {
			serverLevel.getBlockEntity(blockPos, BlockEntityType.SCULK_SHRIEKER)
				.ifPresent(sculkShriekerBlockEntity -> sculkShriekerBlockEntity.replyOrSummon(serverLevel));
		}

		super.onRemove(blockState, level, blockPos, blockState2, bl);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if ((Boolean)blockState.getValue(SHRIEKING)) {
			serverLevel.setBlock(blockPos, blockState.setValue(SHRIEKING, Boolean.valueOf(false)), 3);
			serverLevel.getBlockEntity(blockPos, BlockEntityType.SCULK_SHRIEKER)
				.ifPresent(sculkShriekerBlockEntity -> sculkShriekerBlockEntity.replyOrSummon(serverLevel));
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return COLLIDER;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return COLLIDER;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new SculkShriekerBlockEntity(blockPos, blockState);
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

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState()
			.setValue(WATERLOGGED, Boolean.valueOf(blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos()).getType() == Fluids.WATER));
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
		super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
		if (bl) {
			this.tryDropExperience(serverLevel, blockPos, itemStack, ConstantInt.of(5));
		}
	}

	@Nullable
	@Override
	public <T extends BlockEntity> GameEventListener getListener(ServerLevel serverLevel, T blockEntity) {
		return blockEntity instanceof SculkShriekerBlockEntity sculkShriekerBlockEntity ? sculkShriekerBlockEntity.getListener() : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return !level.isClientSide
			? BaseEntityBlock.createTickerHelper(
				blockEntityType,
				BlockEntityType.SCULK_SHRIEKER,
				(levelx, blockPos, blockStatex, sculkShriekerBlockEntity) -> sculkShriekerBlockEntity.getListener().tick(levelx)
			)
			: null;
	}
}
