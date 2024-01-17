package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BubbleColumnBlock extends Block implements BucketPickup {
	public static final MapCodec<BubbleColumnBlock> CODEC = simpleCodec(BubbleColumnBlock::new);
	public static final BooleanProperty DRAG_DOWN = BlockStateProperties.DRAG;
	private static final int CHECK_PERIOD = 5;

	@Override
	public MapCodec<BubbleColumnBlock> codec() {
		return CODEC;
	}

	public BubbleColumnBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(DRAG_DOWN, Boolean.valueOf(true)));
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		BlockState blockState2 = level.getBlockState(blockPos.above());
		if (blockState2.isAir()) {
			entity.onAboveBubbleCol((Boolean)blockState.getValue(DRAG_DOWN));
			if (!level.isClientSide) {
				ServerLevel serverLevel = (ServerLevel)level;

				for (int i = 0; i < 2; i++) {
					serverLevel.sendParticles(
						ParticleTypes.SPLASH,
						(double)blockPos.getX() + level.random.nextDouble(),
						(double)(blockPos.getY() + 1),
						(double)blockPos.getZ() + level.random.nextDouble(),
						1,
						0.0,
						0.0,
						0.0,
						1.0
					);
					serverLevel.sendParticles(
						ParticleTypes.BUBBLE,
						(double)blockPos.getX() + level.random.nextDouble(),
						(double)(blockPos.getY() + 1),
						(double)blockPos.getZ() + level.random.nextDouble(),
						1,
						0.0,
						0.01,
						0.0,
						0.2
					);
				}
			}
		} else {
			entity.onInsideBubbleColumn((Boolean)blockState.getValue(DRAG_DOWN));
		}
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		updateColumn(serverLevel, blockPos, blockState, serverLevel.getBlockState(blockPos.below()));
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return Fluids.WATER.getSource(false);
	}

	public static void updateColumn(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		updateColumn(levelAccessor, blockPos, levelAccessor.getBlockState(blockPos), blockState);
	}

	public static void updateColumn(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, BlockState blockState2) {
		if (canExistIn(blockState)) {
			BlockState blockState3 = getColumnState(blockState2);
			levelAccessor.setBlock(blockPos, blockState3, 2);
			BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.UP);

			while (canExistIn(levelAccessor.getBlockState(mutableBlockPos))) {
				if (!levelAccessor.setBlock(mutableBlockPos, blockState3, 2)) {
					return;
				}

				mutableBlockPos.move(Direction.UP);
			}
		}
	}

	private static boolean canExistIn(BlockState blockState) {
		return blockState.is(Blocks.BUBBLE_COLUMN)
			|| blockState.is(Blocks.WATER) && blockState.getFluidState().getAmount() >= 8 && blockState.getFluidState().isSource();
	}

	private static BlockState getColumnState(BlockState blockState) {
		if (blockState.is(Blocks.BUBBLE_COLUMN)) {
			return blockState;
		} else if (blockState.is(Blocks.SOUL_SAND)) {
			return Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(false));
		} else {
			return blockState.is(Blocks.MAGMA_BLOCK)
				? Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(true))
				: Blocks.WATER.defaultBlockState();
		}
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		double d = (double)blockPos.getX();
		double e = (double)blockPos.getY();
		double f = (double)blockPos.getZ();
		if ((Boolean)blockState.getValue(DRAG_DOWN)) {
			level.addAlwaysVisibleParticle(ParticleTypes.CURRENT_DOWN, d + 0.5, e + 0.8, f, 0.0, 0.0, 0.0);
			if (randomSource.nextInt(200) == 0) {
				level.playLocalSound(
					d,
					e,
					f,
					SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
					SoundSource.BLOCKS,
					0.2F + randomSource.nextFloat() * 0.2F,
					0.9F + randomSource.nextFloat() * 0.15F,
					false
				);
			}
		} else {
			level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d + 0.5, e, f + 0.5, 0.0, 0.04, 0.0);
			level.addAlwaysVisibleParticle(
				ParticleTypes.BUBBLE_COLUMN_UP,
				d + (double)randomSource.nextFloat(),
				e + (double)randomSource.nextFloat(),
				f + (double)randomSource.nextFloat(),
				0.0,
				0.04,
				0.0
			);
			if (randomSource.nextInt(200) == 0) {
				level.playLocalSound(
					d,
					e,
					f,
					SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
					SoundSource.BLOCKS,
					0.2F + randomSource.nextFloat() * 0.2F,
					0.9F + randomSource.nextFloat() * 0.15F,
					false
				);
			}
		}
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		if (!blockState.canSurvive(levelAccessor, blockPos)
			|| direction == Direction.DOWN
			|| direction == Direction.UP && !blockState2.is(Blocks.BUBBLE_COLUMN) && canExistIn(blockState2)) {
			levelAccessor.scheduleTick(blockPos, this, 5);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState2 = levelReader.getBlockState(blockPos.below());
		return blockState2.is(Blocks.BUBBLE_COLUMN) || blockState2.is(Blocks.MAGMA_BLOCK) || blockState2.is(Blocks.SOUL_SAND);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Shapes.empty();
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DRAG_DOWN);
	}

	@Override
	public ItemStack pickupBlock(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
		return new ItemStack(Items.WATER_BUCKET);
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Fluids.WATER.getPickupSound();
	}
}
