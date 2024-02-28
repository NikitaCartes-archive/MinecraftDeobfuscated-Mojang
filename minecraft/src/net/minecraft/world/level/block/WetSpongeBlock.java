package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WetSpongeBlock extends Block {
	public static final MapCodec<WetSpongeBlock> CODEC = simpleCodec(WetSpongeBlock::new);

	@Override
	public MapCodec<WetSpongeBlock> codec() {
		return CODEC;
	}

	protected WetSpongeBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (level.dimensionType().ultraWarm()) {
			level.setBlock(blockPos, Blocks.SPONGE.defaultBlockState(), 3);
			level.levelEvent(2009, blockPos, 0);
			level.playSound(null, blockPos, SoundEvents.WET_SPONGE_DRIES, SoundSource.BLOCKS, 1.0F, (1.0F + level.getRandom().nextFloat() * 0.2F) * 0.7F);
		}
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		Direction direction = Direction.getRandom(randomSource);
		if (direction != Direction.UP) {
			BlockPos blockPos2 = blockPos.relative(direction);
			BlockState blockState2 = level.getBlockState(blockPos2);
			if (!blockState.canOcclude() || !blockState2.isFaceSturdy(level, blockPos2, direction.getOpposite())) {
				double d = (double)blockPos.getX();
				double e = (double)blockPos.getY();
				double f = (double)blockPos.getZ();
				if (direction == Direction.DOWN) {
					e -= 0.05;
					d += randomSource.nextDouble();
					f += randomSource.nextDouble();
				} else {
					e += randomSource.nextDouble() * 0.8;
					if (direction.getAxis() == Direction.Axis.X) {
						f += randomSource.nextDouble();
						if (direction == Direction.EAST) {
							d++;
						} else {
							d += 0.05;
						}
					} else {
						d += randomSource.nextDouble();
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
