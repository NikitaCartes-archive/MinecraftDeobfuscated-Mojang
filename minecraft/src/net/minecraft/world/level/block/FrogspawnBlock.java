package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FrogspawnBlock extends Block {
	public static final MapCodec<FrogspawnBlock> CODEC = simpleCodec(FrogspawnBlock::new);
	private static final int MIN_TADPOLES_SPAWN = 2;
	private static final int MAX_TADPOLES_SPAWN = 5;
	private static final int DEFAULT_MIN_HATCH_TICK_DELAY = 3600;
	private static final int DEFAULT_MAX_HATCH_TICK_DELAY = 12000;
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.5, 16.0);
	private static int minHatchTickDelay = 3600;
	private static int maxHatchTickDelay = 12000;

	@Override
	public MapCodec<FrogspawnBlock> codec() {
		return CODEC;
	}

	public FrogspawnBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return mayPlaceOn(levelReader, blockPos.below());
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		level.scheduleTick(blockPos, this, getFrogspawnHatchDelay(level.getRandom()));
	}

	private static int getFrogspawnHatchDelay(RandomSource randomSource) {
		return randomSource.nextInt(minHatchTickDelay, maxHatchTickDelay);
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		return !this.canSurvive(blockState, levelReader, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!this.canSurvive(blockState, serverLevel, blockPos)) {
			this.destroyBlock(serverLevel, blockPos);
		} else {
			this.hatchFrogspawn(serverLevel, blockPos, randomSource);
		}
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity.getType().equals(EntityType.FALLING_BLOCK)) {
			this.destroyBlock(level, blockPos);
		}
	}

	private static boolean mayPlaceOn(BlockGetter blockGetter, BlockPos blockPos) {
		FluidState fluidState = blockGetter.getFluidState(blockPos);
		FluidState fluidState2 = blockGetter.getFluidState(blockPos.above());
		return fluidState.getType() == Fluids.WATER && fluidState2.getType() == Fluids.EMPTY;
	}

	private void hatchFrogspawn(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		this.destroyBlock(serverLevel, blockPos);
		serverLevel.playSound(null, blockPos, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
		this.spawnTadpoles(serverLevel, blockPos, randomSource);
	}

	private void destroyBlock(Level level, BlockPos blockPos) {
		level.destroyBlock(blockPos, false);
	}

	private void spawnTadpoles(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		int i = randomSource.nextInt(2, 6);

		for (int j = 1; j <= i; j++) {
			Tadpole tadpole = EntityType.TADPOLE.create(serverLevel, EntitySpawnReason.BREEDING);
			if (tadpole != null) {
				double d = (double)blockPos.getX() + this.getRandomTadpolePositionOffset(randomSource);
				double e = (double)blockPos.getZ() + this.getRandomTadpolePositionOffset(randomSource);
				int k = randomSource.nextInt(1, 361);
				tadpole.moveTo(d, (double)blockPos.getY() - 0.5, e, (float)k, 0.0F);
				tadpole.setPersistenceRequired();
				serverLevel.addFreshEntity(tadpole);
			}
		}
	}

	private double getRandomTadpolePositionOffset(RandomSource randomSource) {
		double d = 0.2F;
		return Mth.clamp(randomSource.nextDouble(), 0.2F, 0.7999999970197678);
	}

	@VisibleForTesting
	public static void setHatchDelay(int i, int j) {
		minHatchTickDelay = i;
		maxHatchTickDelay = j;
	}

	@VisibleForTesting
	public static void setDefaultHatchDelay() {
		minHatchTickDelay = 3600;
		maxHatchTickDelay = 12000;
	}
}
