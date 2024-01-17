package net.minecraft.util;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {
	public static void spawnParticlesOnBlockFaces(Level level, BlockPos blockPos, ParticleOptions particleOptions, IntProvider intProvider) {
		for (Direction direction : Direction.values()) {
			spawnParticlesOnBlockFace(level, blockPos, particleOptions, intProvider, direction, () -> getRandomSpeedRanges(level.random), 0.55);
		}
	}

	public static void spawnParticlesOnBlockFace(
		Level level, BlockPos blockPos, ParticleOptions particleOptions, IntProvider intProvider, Direction direction, Supplier<Vec3> supplier, double d
	) {
		int i = intProvider.sample(level.random);

		for (int j = 0; j < i; j++) {
			spawnParticleOnFace(level, blockPos, direction, particleOptions, (Vec3)supplier.get(), d);
		}
	}

	private static Vec3 getRandomSpeedRanges(RandomSource randomSource) {
		return new Vec3(Mth.nextDouble(randomSource, -0.5, 0.5), Mth.nextDouble(randomSource, -0.5, 0.5), Mth.nextDouble(randomSource, -0.5, 0.5));
	}

	public static void spawnParticlesAlongAxis(
		Direction.Axis axis, Level level, BlockPos blockPos, double d, ParticleOptions particleOptions, UniformInt uniformInt
	) {
		Vec3 vec3 = Vec3.atCenterOf(blockPos);
		boolean bl = axis == Direction.Axis.X;
		boolean bl2 = axis == Direction.Axis.Y;
		boolean bl3 = axis == Direction.Axis.Z;
		int i = uniformInt.sample(level.random);

		for (int j = 0; j < i; j++) {
			double e = vec3.x + Mth.nextDouble(level.random, -1.0, 1.0) * (bl ? 0.5 : d);
			double f = vec3.y + Mth.nextDouble(level.random, -1.0, 1.0) * (bl2 ? 0.5 : d);
			double g = vec3.z + Mth.nextDouble(level.random, -1.0, 1.0) * (bl3 ? 0.5 : d);
			double h = bl ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
			double k = bl2 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
			double l = bl3 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
			level.addParticle(particleOptions, e, f, g, h, k, l);
		}
	}

	public static void spawnParticleOnFace(Level level, BlockPos blockPos, Direction direction, ParticleOptions particleOptions, Vec3 vec3, double d) {
		Vec3 vec32 = Vec3.atCenterOf(blockPos);
		int i = direction.getStepX();
		int j = direction.getStepY();
		int k = direction.getStepZ();
		double e = vec32.x + (i == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)i * d);
		double f = vec32.y + (j == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)j * d);
		double g = vec32.z + (k == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)k * d);
		double h = i == 0 ? vec3.x() : 0.0;
		double l = j == 0 ? vec3.y() : 0.0;
		double m = k == 0 ? vec3.z() : 0.0;
		level.addParticle(particleOptions, e, f, g, h, l, m);
	}

	public static void spawnParticleBelow(Level level, BlockPos blockPos, RandomSource randomSource, ParticleOptions particleOptions) {
		double d = (double)blockPos.getX() + randomSource.nextDouble();
		double e = (double)blockPos.getY() - 0.05;
		double f = (double)blockPos.getZ() + randomSource.nextDouble();
		level.addParticle(particleOptions, d, e, f, 0.0, 0.0, 0.0);
	}

	public static void spawnParticleInBlock(LevelAccessor levelAccessor, BlockPos blockPos, int i, ParticleOptions particleOptions) {
		double d = 0.5;
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		double e = blockState.isAir() ? 1.0 : blockState.getShape(levelAccessor, blockPos).max(Direction.Axis.Y);
		spawnParticles(levelAccessor, blockPos, i, 0.5, e, true, particleOptions);
	}

	public static void spawnParticles(LevelAccessor levelAccessor, BlockPos blockPos, int i, double d, double e, boolean bl, ParticleOptions particleOptions) {
		RandomSource randomSource = levelAccessor.getRandom();

		for (int j = 0; j < i; j++) {
			double f = randomSource.nextGaussian() * 0.02;
			double g = randomSource.nextGaussian() * 0.02;
			double h = randomSource.nextGaussian() * 0.02;
			double k = 0.5 - d;
			double l = (double)blockPos.getX() + k + randomSource.nextDouble() * d * 2.0;
			double m = (double)blockPos.getY() + randomSource.nextDouble() * e;
			double n = (double)blockPos.getZ() + k + randomSource.nextDouble() * d * 2.0;
			if (bl || !levelAccessor.getBlockState(BlockPos.containing(l, m, n).below()).isAir()) {
				levelAccessor.addParticle(particleOptions, l, m, n, f, g, h);
			}
		}
	}
}
