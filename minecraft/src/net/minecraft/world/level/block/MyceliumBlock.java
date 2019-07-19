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
				(double)((float)blockPos.getX() + random.nextFloat()),
				(double)((float)blockPos.getY() + 1.1F),
				(double)((float)blockPos.getZ() + random.nextFloat()),
				0.0,
				0.0,
				0.0
			);
		}
	}
}
