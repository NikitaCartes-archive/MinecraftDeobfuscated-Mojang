package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CryingObsidianBlock extends Block {
	public CryingObsidianBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if (random.nextInt(5) == 0) {
			Direction direction = Direction.getRandom(random);
			if (direction != Direction.UP) {
				BlockPos blockPos2 = blockPos.relative(direction);
				BlockState blockState2 = level.getBlockState(blockPos2);
				if (!blockState.canOcclude() || !blockState2.isFaceSturdy(level, blockPos2, direction.getOpposite())) {
					double d = direction.getStepX() == 0 ? random.nextDouble() : 0.5 + (double)direction.getStepX() * 0.6;
					double e = direction.getStepY() == 0 ? random.nextDouble() : 0.5 + (double)direction.getStepY() * 0.6;
					double f = direction.getStepZ() == 0 ? random.nextDouble() : 0.5 + (double)direction.getStepZ() * 0.6;
					level.addParticle(
						ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double)blockPos.getX() + d, (double)blockPos.getY() + e, (double)blockPos.getZ() + f, 0.0, 0.0, 0.0
					);
				}
			}
		}
	}
}
