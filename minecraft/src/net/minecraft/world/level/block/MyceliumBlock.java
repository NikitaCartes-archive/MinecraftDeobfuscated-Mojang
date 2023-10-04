package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MyceliumBlock extends SpreadingSnowyDirtBlock {
	public static final MapCodec<MyceliumBlock> CODEC = simpleCodec(MyceliumBlock::new);

	@Override
	public MapCodec<MyceliumBlock> codec() {
		return CODEC;
	}

	public MyceliumBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		super.animateTick(blockState, level, blockPos, randomSource);
		if (randomSource.nextInt(10) == 0) {
			level.addParticle(
				ParticleTypes.MYCELIUM,
				(double)blockPos.getX() + randomSource.nextDouble(),
				(double)blockPos.getY() + 1.1,
				(double)blockPos.getZ() + randomSource.nextDouble(),
				0.0,
				0.0,
				0.0
			);
		}
	}
}
