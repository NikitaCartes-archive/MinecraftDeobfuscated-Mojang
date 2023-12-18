package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BrewingStandBlock extends BaseEntityBlock {
	public static final MapCodec<BrewingStandBlock> CODEC = simpleCodec(BrewingStandBlock::new);
	public static final BooleanProperty[] HAS_BOTTLE = new BooleanProperty[]{
		BlockStateProperties.HAS_BOTTLE_0, BlockStateProperties.HAS_BOTTLE_1, BlockStateProperties.HAS_BOTTLE_2
	};
	protected static final VoxelShape SHAPE = Shapes.or(Block.box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0), Block.box(7.0, 0.0, 7.0, 9.0, 14.0, 9.0));

	@Override
	public MapCodec<BrewingStandBlock> codec() {
		return CODEC;
	}

	public BrewingStandBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(HAS_BOTTLE[0], Boolean.valueOf(false))
				.setValue(HAS_BOTTLE[1], Boolean.valueOf(false))
				.setValue(HAS_BOTTLE[2], Boolean.valueOf(false))
		);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new BrewingStandBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockEntityType.BREWING_STAND, BrewingStandBlockEntity::serverTick);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof BrewingStandBlockEntity) {
				player.openMenu((BrewingStandBlockEntity)blockEntity);
				player.awardStat(Stats.INTERACT_WITH_BREWINGSTAND);
			}

			return InteractionResult.CONSUME;
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		if (itemStack.hasCustomHoverName()) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof BrewingStandBlockEntity) {
				((BrewingStandBlockEntity)blockEntity).setCustomName(itemStack.getHoverName());
			}
		}
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		double d = (double)blockPos.getX() + 0.4 + (double)randomSource.nextFloat() * 0.2;
		double e = (double)blockPos.getY() + 0.7 + (double)randomSource.nextFloat() * 0.3;
		double f = (double)blockPos.getZ() + 0.4 + (double)randomSource.nextFloat() * 0.2;
		level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		Containers.dropContentsOnDestroy(blockState, blockState2, level, blockPos);
		super.onRemove(blockState, level, blockPos, blockState2, bl);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HAS_BOTTLE[0], HAS_BOTTLE[1], HAS_BOTTLE[2]);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
