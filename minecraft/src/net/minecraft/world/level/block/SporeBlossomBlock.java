package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SporeBlossomBlock extends Block {
	public static final MapCodec<SporeBlossomBlock> CODEC = simpleCodec(SporeBlossomBlock::new);
	private static final VoxelShape SHAPE = Block.box(2.0, 13.0, 2.0, 14.0, 16.0, 14.0);
	private static final int ADD_PARTICLE_ATTEMPTS = 14;
	private static final int PARTICLE_XZ_RADIUS = 10;
	private static final int PARTICLE_Y_MAX = 10;

	@Override
	public MapCodec<SporeBlossomBlock> codec() {
		return CODEC;
	}

	public SporeBlossomBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return Block.canSupportCenter(levelReader, blockPos.above(), Direction.DOWN) && !levelReader.isWaterAt(blockPos);
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
		return direction == Direction.UP && !this.canSurvive(blockState, levelReader, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		double d = (double)i + randomSource.nextDouble();
		double e = (double)j + 0.7;
		double f = (double)k + randomSource.nextDouble();
		level.addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, d, e, f, 0.0, 0.0, 0.0);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int l = 0; l < 14; l++) {
			mutableBlockPos.set(i + Mth.nextInt(randomSource, -10, 10), j - randomSource.nextInt(10), k + Mth.nextInt(randomSource, -10, 10));
			BlockState blockState2 = level.getBlockState(mutableBlockPos);
			if (!blockState2.isCollisionShapeFullBlock(level, mutableBlockPos)) {
				level.addParticle(
					ParticleTypes.SPORE_BLOSSOM_AIR,
					(double)mutableBlockPos.getX() + randomSource.nextDouble(),
					(double)mutableBlockPos.getY() + randomSource.nextDouble(),
					(double)mutableBlockPos.getZ() + randomSource.nextDouble(),
					0.0,
					0.0,
					0.0
				);
			}
		}
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}
}
