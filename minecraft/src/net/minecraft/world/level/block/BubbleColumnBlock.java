package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BubbleColumnBlock extends Block implements BucketPickup {
	public static final BooleanProperty DRAG_DOWN = BlockStateProperties.DRAG;

	public BubbleColumnBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(DRAG_DOWN, Boolean.valueOf(true)));
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		BlockState blockState2 = level.getBlockState(blockPos.above());
		if (blockState2.isAir()) {
			entity.onAboveBubbleCol((Boolean)blockState.getValue(DRAG_DOWN));
			if (!level.isClientSide) {
				ServerLevel serverLevel = (ServerLevel)level;

				for (int i = 0; i < 2; i++) {
					serverLevel.sendParticles(
						ParticleTypes.SPLASH,
						(double)((float)blockPos.getX() + level.random.nextFloat()),
						(double)(blockPos.getY() + 1),
						(double)((float)blockPos.getZ() + level.random.nextFloat()),
						1,
						0.0,
						0.0,
						0.0,
						1.0
					);
					serverLevel.sendParticles(
						ParticleTypes.BUBBLE,
						(double)((float)blockPos.getX() + level.random.nextFloat()),
						(double)(blockPos.getY() + 1),
						(double)((float)blockPos.getZ() + level.random.nextFloat()),
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
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		growColumn(level, blockPos.above(), getDrag(level, blockPos.below()));
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		growColumn(serverLevel, blockPos.above(), getDrag(serverLevel, blockPos));
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return Fluids.WATER.getSource(false);
	}

	public static void growColumn(LevelAccessor levelAccessor, BlockPos blockPos, boolean bl) {
		if (canExistIn(levelAccessor, blockPos)) {
			levelAccessor.setBlock(blockPos, Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(bl)), 2);
		}
	}

	public static boolean canExistIn(LevelAccessor levelAccessor, BlockPos blockPos) {
		FluidState fluidState = levelAccessor.getFluidState(blockPos);
		return levelAccessor.getBlockState(blockPos).getBlock() == Blocks.WATER && fluidState.getAmount() >= 8 && fluidState.isSource();
	}

	private static boolean getDrag(BlockGetter blockGetter, BlockPos blockPos) {
		BlockState blockState = blockGetter.getBlockState(blockPos);
		Block block = blockState.getBlock();
		return block == Blocks.BUBBLE_COLUMN ? (Boolean)blockState.getValue(DRAG_DOWN) : block != Blocks.SOUL_SAND;
	}

	@Override
	public int getTickDelay(LevelReader levelReader) {
		return 5;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		double d = (double)blockPos.getX();
		double e = (double)blockPos.getY();
		double f = (double)blockPos.getZ();
		if ((Boolean)blockState.getValue(DRAG_DOWN)) {
			level.addAlwaysVisibleParticle(ParticleTypes.CURRENT_DOWN, d + 0.5, e + 0.8, f, 0.0, 0.0, 0.0);
			if (random.nextInt(200) == 0) {
				level.playLocalSound(
					d, e, f, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false
				);
			}
		} else {
			level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d + 0.5, e, f + 0.5, 0.0, 0.04, 0.0);
			level.addAlwaysVisibleParticle(
				ParticleTypes.BUBBLE_COLUMN_UP, d + (double)random.nextFloat(), e + (double)random.nextFloat(), f + (double)random.nextFloat(), 0.0, 0.04, 0.0
			);
			if (random.nextInt(200) == 0) {
				level.playLocalSound(
					d, e, f, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false
				);
			}
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!blockState.canSurvive(levelAccessor, blockPos)) {
			return Blocks.WATER.defaultBlockState();
		} else {
			if (direction == Direction.DOWN) {
				levelAccessor.setBlock(blockPos, Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(getDrag(levelAccessor, blockPos2))), 2);
			} else if (direction == Direction.UP && blockState2.getBlock() != Blocks.BUBBLE_COLUMN && canExistIn(levelAccessor, blockPos2)) {
				levelAccessor.getBlockTicks().scheduleTick(blockPos, this, this.getTickDelay(levelAccessor));
			}

			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		}
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		Block block = levelReader.getBlockState(blockPos.below()).getBlock();
		return block == Blocks.BUBBLE_COLUMN || block == Blocks.MAGMA_BLOCK || block == Blocks.SOUL_SAND;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Shapes.empty();
	}

	@Override
	public BlockLayer getRenderLayer() {
		return BlockLayer.TRANSLUCENT;
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DRAG_DOWN);
	}

	@Override
	public Fluid takeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
		return Fluids.WATER;
	}
}
