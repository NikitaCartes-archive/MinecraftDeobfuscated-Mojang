package net.minecraft.world.entity.ai.util;

import java.util.Random;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

public class RandomPos {
	@Nullable
	public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j) {
		return generateRandomPos(pathfinderMob, i, j, null);
	}

	@Nullable
	public static Vec3 getLandPos(PathfinderMob pathfinderMob, int i, int j) {
		return getLandPos(pathfinderMob, i, j, pathfinderMob::getWalkTargetValue);
	}

	@Nullable
	public static Vec3 getLandPos(PathfinderMob pathfinderMob, int i, int j, ToDoubleFunction<BlockPos> toDoubleFunction) {
		return generateRandomPos(pathfinderMob, i, j, null, false, 0.0, toDoubleFunction);
	}

	@Nullable
	public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
		Vec3 vec32 = vec3.subtract(pathfinderMob.x, pathfinderMob.y, pathfinderMob.z);
		return generateRandomPos(pathfinderMob, i, j, vec32);
	}

	@Nullable
	public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, double d) {
		Vec3 vec32 = vec3.subtract(pathfinderMob.x, pathfinderMob.y, pathfinderMob.z);
		return generateRandomPos(pathfinderMob, i, j, vec32, true, d, pathfinderMob::getWalkTargetValue);
	}

	@Nullable
	public static Vec3 getLandPosAvoid(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
		Vec3 vec32 = new Vec3(pathfinderMob.x, pathfinderMob.y, pathfinderMob.z).subtract(vec3);
		return generateRandomPos(pathfinderMob, i, j, vec32, false, (float) (Math.PI / 2), pathfinderMob::getWalkTargetValue);
	}

	@Nullable
	public static Vec3 getPosAvoid(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
		Vec3 vec32 = new Vec3(pathfinderMob.x, pathfinderMob.y, pathfinderMob.z).subtract(vec3);
		return generateRandomPos(pathfinderMob, i, j, vec32);
	}

	@Nullable
	private static Vec3 generateRandomPos(PathfinderMob pathfinderMob, int i, int j, @Nullable Vec3 vec3) {
		return generateRandomPos(pathfinderMob, i, j, vec3, true, (float) (Math.PI / 2), pathfinderMob::getWalkTargetValue);
	}

	@Nullable
	private static Vec3 generateRandomPos(
		PathfinderMob pathfinderMob, int i, int j, @Nullable Vec3 vec3, boolean bl, double d, ToDoubleFunction<BlockPos> toDoubleFunction
	) {
		PathNavigation pathNavigation = pathfinderMob.getNavigation();
		Random random = pathfinderMob.getRandom();
		boolean bl2;
		if (pathfinderMob.hasRestriction()) {
			bl2 = pathfinderMob.getRestrictCenter().closerThan(pathfinderMob.position(), (double)(pathfinderMob.getRestrictRadius() + (float)i) + 1.0);
		} else {
			bl2 = false;
		}

		boolean bl3 = false;
		double e = Double.NEGATIVE_INFINITY;
		BlockPos blockPos = new BlockPos(pathfinderMob);

		for (int k = 0; k < 10; k++) {
			BlockPos blockPos2 = getRandomDelta(random, i, j, vec3, d);
			if (blockPos2 != null) {
				int l = blockPos2.getX();
				int m = blockPos2.getY();
				int n = blockPos2.getZ();
				if (pathfinderMob.hasRestriction() && i > 1) {
					BlockPos blockPos3 = pathfinderMob.getRestrictCenter();
					if (pathfinderMob.x > (double)blockPos3.getX()) {
						l -= random.nextInt(i / 2);
					} else {
						l += random.nextInt(i / 2);
					}

					if (pathfinderMob.z > (double)blockPos3.getZ()) {
						n -= random.nextInt(i / 2);
					} else {
						n += random.nextInt(i / 2);
					}
				}

				BlockPos blockPos3x = new BlockPos((double)l + pathfinderMob.x, (double)m + pathfinderMob.y, (double)n + pathfinderMob.z);
				if ((!bl2 || pathfinderMob.isWithinRestriction(blockPos3x)) && pathNavigation.isStableDestination(blockPos3x)) {
					if (!bl) {
						blockPos3x = moveAboveSolid(blockPos3x, pathfinderMob);
						if (isWaterDestination(blockPos3x, pathfinderMob)) {
							continue;
						}
					}

					double f = toDoubleFunction.applyAsDouble(blockPos3x);
					if (f > e) {
						e = f;
						blockPos = blockPos3x;
						bl3 = true;
					}
				}
			}
		}

		return bl3 ? new Vec3(blockPos) : null;
	}

	@Nullable
	private static BlockPos getRandomDelta(Random random, int i, int j, @Nullable Vec3 vec3, double d) {
		if (vec3 != null && !(d >= Math.PI)) {
			double e = Mth.atan2(vec3.z, vec3.x) - (float) (Math.PI / 2);
			double f = e + (double)(2.0F * random.nextFloat() - 1.0F) * d;
			double g = Math.sqrt(random.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)i;
			double h = -g * Math.sin(f);
			double n = g * Math.cos(f);
			if (!(Math.abs(h) > (double)i) && !(Math.abs(n) > (double)i)) {
				int o = random.nextInt(2 * j + 1) - j;
				return new BlockPos(h, (double)o, n);
			} else {
				return null;
			}
		} else {
			int k = random.nextInt(2 * i + 1) - i;
			int l = random.nextInt(2 * j + 1) - j;
			int m = random.nextInt(2 * i + 1) - i;
			return new BlockPos(k, l, m);
		}
	}

	private static BlockPos moveAboveSolid(BlockPos blockPos, PathfinderMob pathfinderMob) {
		if (!pathfinderMob.level.getBlockState(blockPos).getMaterial().isSolid()) {
			return blockPos;
		} else {
			BlockPos blockPos2 = blockPos.above();

			while (blockPos2.getY() < pathfinderMob.level.getMaxBuildHeight() && pathfinderMob.level.getBlockState(blockPos2).getMaterial().isSolid()) {
				blockPos2 = blockPos2.above();
			}

			return blockPos2;
		}
	}

	private static boolean isWaterDestination(BlockPos blockPos, PathfinderMob pathfinderMob) {
		return pathfinderMob.level.getFluidState(blockPos).is(FluidTags.WATER);
	}
}
