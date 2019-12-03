package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MyceliumBlock extends SpreadingSnowyDirtBlock {
	public MyceliumBlock(Block.Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		super.animateTick(blockState, level, blockPos, random);
		if (random.nextInt(10) == 0) {
			level.addParticle(
				ParticleTypes.MYCELIUM,
				(double)blockPos.getX() + (double)random.nextFloat(),
				(double)blockPos.getY() + 1.1,
				(double)blockPos.getZ() + (double)random.nextFloat(),
				0.0,
				0.0,
				0.0
			);
		}
	}
}
