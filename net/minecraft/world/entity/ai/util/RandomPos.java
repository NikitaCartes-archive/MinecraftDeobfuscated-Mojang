/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.util;

import java.util.Random;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j) {
        return RandomPos.generateRandomPos(pathfinderMob, i, j, null);
    }

    @Nullable
    public static Vec3 getPosAboveSolid(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, float f, int k, int l) {
        return RandomPos.generateRandomPos(pathfinderMob, i, j, 0, vec3, true, f, pathfinderMob::getWalkTargetValue, true, blockPos -> pathfinderMob.getNavigation().isStableDestination((BlockPos)blockPos), k, l, true);
    }

    @Nullable
    public static Vec3 getLandPos(PathfinderMob pathfinderMob, int i, int j) {
        return RandomPos.getLandPos(pathfinderMob, i, j, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getLandPos(PathfinderMob pathfinderMob, int i, int j, ToDoubleFunction<BlockPos> toDoubleFunction) {
        return RandomPos.generateRandomPos(pathfinderMob, i, j, null, false, 0.0, toDoubleFunction);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, boolean bl) {
        Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec32, bl);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
        return RandomPos.getPosTowards(pathfinderMob, i, j, vec3, true);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, double d, boolean bl) {
        Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec32, bl, d, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, double d) {
        return RandomPos.getPosTowards(pathfinderMob, i, j, vec3, d, true);
    }

    @Nullable
    public static Vec3 getLandPosAvoid(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
        Vec3 vec32 = pathfinderMob.position().subtract(vec3);
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec32, false, 1.5707963705062866, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getPosAvoid(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
        Vec3 vec32 = pathfinderMob.position().subtract(vec3);
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec32);
    }

    @Nullable
    public static Vec3 getAirPos(PathfinderMob pathfinderMob, int i, int j, int k, @Nullable Vec3 vec3, double d) {
        return RandomPos.generateRandomPos(pathfinderMob, i, j, k, vec3, true, d, pathfinderMob::getWalkTargetValue, false, blockPos -> false, 0, 0, false);
    }

    @Nullable
    private static Vec3 generateRandomPos(PathfinderMob pathfinderMob, int i, int j, @Nullable Vec3 vec3) {
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec3, true, 1.5707963705062866, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    private static Vec3 generateRandomPos(PathfinderMob pathfinderMob, int i, int j, @Nullable Vec3 vec3, boolean bl) {
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec3, bl, 1.5707963705062866, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    private static Vec3 generateRandomPos(PathfinderMob pathfinderMob, int i, int j, @Nullable Vec3 vec3, boolean bl, double d, ToDoubleFunction<BlockPos> toDoubleFunction) {
        return RandomPos.generateRandomPos(pathfinderMob, i, j, 0, vec3, bl, d, toDoubleFunction, !bl, blockPos -> pathfinderMob.level.getBlockState((BlockPos)blockPos).getMaterial().isSolid(), 0, 0, true);
    }

    @Nullable
    private static Vec3 generateRandomPos(PathfinderMob pathfinderMob, int i, int j, int k, @Nullable Vec3 vec3, boolean bl, double d, ToDoubleFunction<BlockPos> toDoubleFunction, boolean bl2, Predicate<BlockPos> predicate, int l, int m, boolean bl3) {
        PathNavigation pathNavigation = pathfinderMob.getNavigation();
        Random random = pathfinderMob.getRandom();
        boolean bl4 = pathfinderMob.hasRestriction() ? pathfinderMob.getRestrictCenter().closerThan(pathfinderMob.position(), (double)(pathfinderMob.getRestrictRadius() + (float)i) + 1.0) : false;
        boolean bl5 = false;
        double e = Double.NEGATIVE_INFINITY;
        BlockPos blockPos = new BlockPos(pathfinderMob);
        for (int n = 0; n < 10; ++n) {
            double f;
            BlockPathTypes blockPathTypes;
            BlockPos blockPos3;
            BlockPos blockPos2 = RandomPos.getRandomDelta(random, i, j, k, vec3, d);
            if (blockPos2 == null) continue;
            int o = blockPos2.getX();
            int p = blockPos2.getY();
            int q = blockPos2.getZ();
            if (pathfinderMob.hasRestriction() && i > 1) {
                blockPos3 = pathfinderMob.getRestrictCenter();
                o = pathfinderMob.getX() > (double)blockPos3.getX() ? (o -= random.nextInt(i / 2)) : (o += random.nextInt(i / 2));
                q = pathfinderMob.getZ() > (double)blockPos3.getZ() ? (q -= random.nextInt(i / 2)) : (q += random.nextInt(i / 2));
            }
            blockPos3 = new BlockPos((double)o + pathfinderMob.getX(), (double)p + pathfinderMob.getY(), (double)q + pathfinderMob.getZ());
            if (bl4 && !pathfinderMob.isWithinRestriction(blockPos3) || bl3 && !pathNavigation.isStableDestination(blockPos3)) continue;
            if (bl2) {
                blockPos3 = RandomPos.moveAboveSolid(blockPos3, random.nextInt(l + 1) + m, pathfinderMob.level.getMaxBuildHeight(), predicate);
            }
            if (!bl && RandomPos.isWaterDestination(blockPos3, pathfinderMob) || pathfinderMob.getPathfindingMalus(blockPathTypes = WalkNodeEvaluator.getBlockPathTypeStatic(pathfinderMob.level, blockPos3.getX(), blockPos3.getY(), blockPos3.getZ())) != 0.0f || !((f = toDoubleFunction.applyAsDouble(blockPos3)) > e)) continue;
            e = f;
            blockPos = blockPos3;
            bl5 = true;
        }
        if (bl5) {
            return new Vec3(blockPos);
        }
        return null;
    }

    @Nullable
    private static BlockPos getRandomDelta(Random random, int i, int j, int k, @Nullable Vec3 vec3, double d) {
        if (vec3 == null || d >= Math.PI) {
            int l = random.nextInt(2 * i + 1) - i;
            int m = random.nextInt(2 * j + 1) - j + k;
            int n = random.nextInt(2 * i + 1) - i;
            return new BlockPos(l, m, n);
        }
        double e = Mth.atan2(vec3.z, vec3.x) - 1.5707963705062866;
        double f = e + (double)(2.0f * random.nextFloat() - 1.0f) * d;
        double g = Math.sqrt(random.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)i;
        double h = -g * Math.sin(f);
        double o = g * Math.cos(f);
        if (Math.abs(h) > (double)i || Math.abs(o) > (double)i) {
            return null;
        }
        int p = random.nextInt(2 * j + 1) - j + k;
        return new BlockPos(h, (double)p, o);
    }

    static BlockPos moveAboveSolid(BlockPos blockPos, int i, int j, Predicate<BlockPos> predicate) {
        if (i < 0) {
            throw new IllegalArgumentException("aboveSolidAmount was " + i + ", expected >= 0");
        }
        if (predicate.test(blockPos)) {
            BlockPos blockPos4;
            BlockPos blockPos2 = blockPos.above();
            while (blockPos2.getY() < j && predicate.test(blockPos2)) {
                blockPos2 = blockPos2.above();
            }
            BlockPos blockPos3 = blockPos2;
            while (blockPos3.getY() < j && blockPos3.getY() - blockPos2.getY() < i && !predicate.test(blockPos4 = blockPos3.above())) {
                blockPos3 = blockPos4;
            }
            return blockPos3;
        }
        return blockPos;
    }

    private static boolean isWaterDestination(BlockPos blockPos, PathfinderMob pathfinderMob) {
        return pathfinderMob.level.getFluidState(blockPos).is(FluidTags.WATER);
    }
}

