package net.minecraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {
	public static void spawnParticlesOnBlockFaces(Level level, BlockPos blockPos, ParticleOptions particleOptions, UniformInt uniformInt) {
		for (Direction direction : Direction.values()) {
			int i = uniformInt.sample(level.random);

			for (int j = 0; j < i; j++) {
				spawnParticleOnFace(level, blockPos, direction, particleOptions);
			}
		}
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

	public static void spawnParticleOnFace(Level level, BlockPos blockPos, Direction direction, ParticleOptions particleOptions) {
		Vec3 vec3 = Vec3.atCenterOf(blockPos);
		int i = direction.getStepX();
		int j = direction.getStepY();
		int k = direction.getStepZ();
		double d = vec3.x + (i == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)i * 0.55);
		double e = vec3.y + (j == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)j * 0.55);
		double f = vec3.z + (k == 0 ? Mth.nextDouble(level.random, -0.5, 0.5) : (double)k * 0.55);
		double g = i == 0 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
		double h = j == 0 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
		double l = k == 0 ? Mth.nextDouble(level.random, -1.0, 1.0) : 0.0;
		level.addParticle(particleOptions, d, e, f, g, h, l);
	}
}
