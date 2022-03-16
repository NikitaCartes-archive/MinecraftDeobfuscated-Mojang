package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FrogspawnBlock extends Block {
	private static final int MIN_TADPOLES_SPAWN = 2;
	private static final int MAX_TADPOLES_SPAWN = 5;
	private static final int DEFAULT_MIN_HATCH_TICK_DELAY = 3600;
	private static final int DEFAULT_MAX_HATCH_TICK_DELAY = 12000;
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.5, 16.0);
	private static int minHatchTickDelay = 3600;
	private static int maxHatchTickDelay = 12000;

	public FrogspawnBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return mayPlaceOn(levelReader, blockPos.below());
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		level.scheduleTick(blockPos, this, getFrogspawnHatchDelay(level.getRandom()));
	}

	private static int getFrogspawnHatchDelay(Random random) {
		return random.nextInt(minHatchTickDelay, maxHatchTickDelay);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return !this.canSurvive(blockState, levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!this.canSurvive(blockState, serverLevel, blockPos)) {
			this.destroyBlock(serverLevel, blockPos);
		} else {
			this.hatchFrogspawn(serverLevel, blockPos, random);
		}
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity.getType().equals(EntityType.FALLING_BLOCK)) {
			this.destroyBlock(level, blockPos);
		}
	}

	private static boolean mayPlaceOn(BlockGetter blockGetter, BlockPos blockPos) {
		FluidState fluidState = blockGetter.getFluidState(blockPos);
		FluidState fluidState2 = blockGetter.getFluidState(blockPos.above());
		return fluidState.getType() == Fluids.WATER && fluidState2.getType() == Fluids.EMPTY;
	}

	private void hatchFrogspawn(ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.destroyBlock(serverLevel, blockPos);
		serverLevel.playSound(null, blockPos, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
		this.spawnTadpoles(serverLevel, blockPos, random);
	}

	private void destroyBlock(Level level, BlockPos blockPos) {
		level.destroyBlock(blockPos, false);
	}

	private void spawnTadpoles(ServerLevel serverLevel, BlockPos blockPos, Random random) {
		int i = random.nextInt(2, 6);

		for (int j = 1; j <= i; j++) {
			Tadpole tadpole = EntityType.TADPOLE.create(serverLevel);
			double d = random.nextDouble();
			double e = random.nextDouble();
			double f = (double)blockPos.getX() + d;
			double g = (double)blockPos.getZ() + e;
			int k = random.nextInt(1, 361);
			tadpole.moveTo(f, (double)blockPos.getY() - 0.5, g, (float)k, 0.0F);
			tadpole.setPersistenceRequired();
			serverLevel.addFreshEntity(tadpole);
		}
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
