package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WetSpongeBlock extends Block {
	protected WetSpongeBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (level.getDimension().isUltraWarm()) {
			level.setBlock(blockPos, Blocks.SPONGE.defaultBlockState(), 2);
			level.levelEvent(2009, blockPos, 0);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		Direction direction = Direction.getRandomFace(random);
		if (direction != Direction.UP) {
			BlockPos blockPos2 = blockPos.relative(direction);
			BlockState blockState2 = level.getBlockState(blockPos2);
			if (!blockState.canOcclude() || !blockState2.isFaceSturdy(level, blockPos2, direction.getOpposite())) {
				double d = (double)blockPos.getX();
				double e = (double)blockPos.getY();
				double f = (double)blockPos.getZ();
				if (direction == Direction.DOWN) {
					e -= 0.05;
					d += random.nextDouble();
					f += random.nextDouble();
				} else {
					e += random.nextDouble() * 0.8;
					if (direction.getAxis() == Direction.Axis.X) {
						f += random.nextDouble();
						if (direction == Direction.EAST) {
							d++;
						} else {
							d += 0.05;
						}
					} else {
						d += random.nextDouble();
						if (direction == Direction.SOUTH) {
							f++;
						} else {
							f += 0.05;
						}
					}
				}

				level.addParticle(ParticleTypes.DRIPPING_WATER, d, e, f, 0.0, 0.0, 0.0);
			}
		}
	}
}
