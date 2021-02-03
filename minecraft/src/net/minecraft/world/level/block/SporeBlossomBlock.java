package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SporeBlossomBlock extends Block {
	private static final VoxelShape SHAPE = Block.box(2.0, 13.0, 2.0, 14.0, 16.0, 14.0);

	public SporeBlossomBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return Block.canSupportCenter(levelReader, blockPos.above(), Direction.DOWN);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		double d = (double)i + random.nextDouble();
		double e = (double)j + 0.7;
		double f = (double)k + random.nextDouble();
		level.addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, d, e, f, 0.0, 0.0, 0.0);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}
}
