package net.minecraft.world.entity.ai.util;

import java.util.Random;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class RandomPos {
	@Nullable
	public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j) {
		return generateRandomPos(pathfinderMob, i, j, 0, null, true, (float) (Math.PI / 2), pathfinderMob::getWalkTargetValue, false, 0, 0, true);
	}

	@Nullable
	public static Vec3 getAirPos(PathfinderMob pathfinderMob, int i, int j, int k, @Nullable Vec3 vec3, double d) {
		return generateRandomPos(pathfinderMob, i, j, k, vec3, true, d, pathfinderMob::getWalkTargetValue, true, 0, 0, false);
	}

	@Nullable
	public static Vec3 getLandPos(PathfinderMob pathfinderMob, int i, int j) {
		return getLandPos(pathfinderMob, i, j, pathfinderMob::getWalkTargetValue);
	}

	@Nullable
	public static Vec3 getLandPos(PathfinderMob pathfinderMob, int i, int j, ToDoubleFunction<BlockPos> toDoubleFunction) {
		return generateRandomPos(pathfinderMob, i, j, 0, null, false, 0.0, toDoubleFunction, true, 0, 0, true);
	}

	@Nullable
	public static Vec3 getAboveLandPos(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, float f, int k, int l) {
		return generateRandomPos(pathfinderMob, i, j, 0, vec3, false, (double)f, pathfinderMob::getWalkTargetValue, true, k, l, true);
	}

	@Nullable
	public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
		Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
		return generateRandomPos(pathfinderMob, i, j, 0, vec32, true, (float) (Math.PI / 2), pathfinderMob::getWalkTargetValue, false, 0, 0, true);
	}

	@Nullable
	public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, double d) {
		Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
		return generateRandomPos(pathfinderMob, i, j, 0, vec32, true, d, pathfinderMob::getWalkTargetValue, false, 0, 0, true);
	}

	@Nullable
	public static Vec3 getAirPosTowards(PathfinderMob pathfinderMob, int i, int j, int k, Vec3 vec3, double d) {
		Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
		return generateRandomPos(pathfinderMob, i, j, k, vec32, false, d, pathfinderMob::getWalkTargetValue, true, 0, 0, false);
	}

	@Nullable
	public static Vec3 getPosAvoid(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
		Vec3 vec32 = pathfinderMob.position().subtract(vec3);
		return generateRandomPos(pathfinderMob, i, j, 0, vec32, true, (float) (Math.PI / 2), pathfinderMob::getWalkTargetValue, false, 0, 0, true);
	}

	@Nullable
	public static Vec3 getLandPosAvoid(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
		Vec3 vec32 = pathfinderMob.position().subtract(vec3);
		return generateRandomPos(pathfinderMob, i, j, 0, vec32, false, (float) (Math.PI / 2), pathfinderMob::getWalkTargetValue, true, 0, 0, true);
	}

	@Nullable
	private static Vec3 generateRandomPos(
		PathfinderMob pathfinderMob,
		int i,
		int j,
		int k,
		@Nullable Vec3 vec3,
		boolean bl,
		double d,
		ToDoubleFunction<BlockPos> toDoubleFunction,
		boolean bl2,
		int l,
		int m,
		boolean bl3
	) {
		PathNavigation pathNavigation = pathfinderMob.getNavigation();
		Random random = pathfinderMob.getRandom();
		boolean bl4;
		if (pathfinderMob.hasRestriction()) {
			bl4 = pathfinderMob.getRestrictCenter().closerThan(pathfinderMob.position(), (double)(pathfinderMob.getRestrictRadius() + (float)i) + 1.0);
		} else {
			bl4 = false;
		}

		boolean bl5 = false;
		double e = Double.NEGATIVE_INFINITY;
		BlockPos blockPos = pathfinderMob.blockPosition();

		for (int n = 0; n < 10; n++) {
			BlockPos blockPos2 = getRandomDelta(random, i, j, k, vec3, d);
			if (blockPos2 != null) {
				int o = blockPos2.getX();
				int p = blockPos2.getY();
				int q = blockPos2.getZ();
				if (pathfinderMob.hasRestriction() && i > 1) {
					BlockPos blockPos3 = pathfinderMob.getRestrictCenter();
					if (pathfinderMob.getX() > (double)blockPos3.getX()) {
						o -= random.nextInt(i / 2);
					} else {
						o += random.nextInt(i / 2);
					}

					if (pathfinderMob.getZ() > (double)blockPos3.getZ()) {
						q -= random.nextInt(i / 2);
					} else {
						q += random.nextInt(i / 2);
					}
				}

				BlockPos blockPos3x = new BlockPos((double)o + pathfinderMob.getX(), (double)p + pathfinderMob.getY(), (double)q + pathfinderMob.getZ());
				if (blockPos3x.getY() >= 0
					&& blockPos3x.getY() <= pathfinderMob.level.getMaxBuildHeight()
					&& (!bl4 || pathfinderMob.isWithinRestriction(blockPos3x))
					&& (!bl3 || pathNavigation.isStableDestination(blockPos3x))) {
					if (bl2) {
						blockPos3x = moveUpToAboveSolid(
							blockPos3x,
							random.nextInt(l + 1) + m,
							pathfinderMob.level.getMaxBuildHeight(),
							blockPosx -> pathfinderMob.level.getBlockState(blockPosx).getMaterial().isSolid()
						);
					}

					if (bl || !pathfinderMob.level.getFluidState(blockPos3x).is(FluidTags.WATER)) {
						BlockPathTypes blockPathTypes = WalkNodeEvaluator.getBlockPathTypeStatic(pathfinderMob.level, blockPos3x.mutable());
						if (pathfinderMob.getPathfindingMalus(blockPathTypes) == 0.0F) {
							double f = toDoubleFunction.applyAsDouble(blockPos3x);
							if (f > e) {
								e = f;
								blockPos = blockPos3x;
								bl5 = true;
							}
						}
					}
				}
			}
		}

		return bl5 ? Vec3.atBottomCenterOf(blockPos) : null;
	}

	@Nullable
	private static BlockPos getRandomDelta(Random random, int i, int j, int k, @Nullable Vec3 vec3, double d) {
		if (vec3 != null && !(d >= Math.PI)) {
			double e = Mth.atan2(vec3.z, vec3.x) - (float) (Math.PI / 2);
			double f = e + (double)(2.0F * random.nextFloat() - 1.0F) * d;
			double g = Math.sqrt(random.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)i;
			double h = -g * Math.sin(f);
			double o = g * Math.cos(f);
			if (!(Math.abs(h) > (double)i) && !(Math.abs(o) > (double)i)) {
				int p = random.nextInt(2 * j + 1) - j + k;
				return new BlockPos(h, (double)p, o);
			} else {
				return null;
			}
		} else {
			int l = random.nextInt(2 * i + 1) - i;
			int m = random.nextInt(2 * j + 1) - j + k;
			int n = random.nextInt(2 * i + 1) - i;
			return new BlockPos(l, m, n);
		}
	}

	static BlockPos moveUpToAboveSolid(BlockPos blockPos, int i, int j, Predicate<BlockPos> predicate) {
		if (i < 0) {
			throw new IllegalArgumentException("aboveSolidAmount was " + i + ", expected >= 0");
		} else if (!predicate.test(blockPos)) {
			return blockPos;
		} else {
			BlockPos blockPos2 = blockPos.above();

			while (blockPos2.getY() < j && predicate.test(blockPos2)) {
				blockPos2 = blockPos2.above();
			}

			BlockPos blockPos3 = blockPos2;

			while (blockPos3.getY() < j && blockPos3.getY() - blockPos2.getY() < i) {
				BlockPos blockPos4 = blockPos3.above();
				if (predicate.test(blockPos4)) {
					break;
				}

				blockPos3 = blockPos4;
			}

			return blockPos3;
		}
	}
}
