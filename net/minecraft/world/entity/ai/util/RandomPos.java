/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.util;

import java.util.Random;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j) {
        return RandomPos.generateRandomPos(pathfinderMob, i, j, null);
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
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
        Vec3 vec32 = vec3.subtract(pathfinderMob.x, pathfinderMob.y, pathfinderMob.z);
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec32);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, double d) {
        Vec3 vec32 = vec3.subtract(pathfinderMob.x, pathfinderMob.y, pathfinderMob.z);
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec32, true, d, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getLandPosAvoid(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
        Vec3 vec32 = new Vec3(pathfinderMob.x, pathfinderMob.y, pathfinderMob.z).subtract(vec3);
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec32, false, 1.5707963705062866, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getPosAvoid(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
        Vec3 vec32 = new Vec3(pathfinderMob.x, pathfinderMob.y, pathfinderMob.z).subtract(vec3);
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec32);
    }

    @Nullable
    private static Vec3 generateRandomPos(PathfinderMob pathfinderMob, int i, int j, @Nullable Vec3 vec3) {
        return RandomPos.generateRandomPos(pathfinderMob, i, j, vec3, true, 1.5707963705062866, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    private static Vec3 generateRandomPos(PathfinderMob pathfinderMob, int i, int j, @Nullable Vec3 vec3, boolean bl, double d, ToDoubleFunction<BlockPos> toDoubleFunction) {
        PathNavigation pathNavigation = pathfinderMob.getNavigation();
        Random random = pathfinderMob.getRandom();
        boolean bl2 = pathfinderMob.hasRestriction() ? pathfinderMob.getRestrictCenter().closerThan(pathfinderMob.position(), (double)(pathfinderMob.getRestrictRadius() + (float)i) + 1.0) : false;
        boolean bl3 = false;
        double e = Double.NEGATIVE_INFINITY;
        BlockPos blockPos = new BlockPos(pathfinderMob);
        for (int k = 0; k < 10; ++k) {
            double f;
            BlockPos blockPos3;
            BlockPos blockPos2 = RandomPos.getRandomDelta(random, i, j, vec3, d);
            if (blockPos2 == null) continue;
            int l = blockPos2.getX();
            int m = blockPos2.getY();
            int n = blockPos2.getZ();
            if (pathfinderMob.hasRestriction() && i > 1) {
                blockPos3 = pathfinderMob.getRestrictCenter();
                l = pathfinderMob.x > (double)blockPos3.getX() ? (l -= random.nextInt(i / 2)) : (l += random.nextInt(i / 2));
                n = pathfinderMob.z > (double)blockPos3.getZ() ? (n -= random.nextInt(i / 2)) : (n += random.nextInt(i / 2));
            }
            blockPos3 = new BlockPos((double)l + pathfinderMob.x, (double)m + pathfinderMob.y, (double)n + pathfinderMob.z);
            if (bl2 && !pathfinderMob.isWithinRestriction(blockPos3) || !pathNavigation.isStableDestination(blockPos3) || !bl && RandomPos.isWaterDestination(blockPos3 = RandomPos.moveAboveSolid(blockPos3, pathfinderMob), pathfinderMob) || !((f = toDoubleFunction.applyAsDouble(blockPos3)) > e)) continue;
            e = f;
            blockPos = blockPos3;
            bl3 = true;
        }
        if (bl3) {
            return new Vec3(blockPos);
        }
        return null;
    }

    @Nullable
    private static BlockPos getRandomDelta(Random random, int i, int j, @Nullable Vec3 vec3, double d) {
        if (vec3 == null || d >= Math.PI) {
            int k = random.nextInt(2 * i + 1) - i;
            int l = random.nextInt(2 * j + 1) - j;
            int m = random.nextInt(2 * i + 1) - i;
            return new BlockPos(k, l, m);
        }
        double e = Mth.atan2(vec3.z, vec3.x) - 1.5707963705062866;
        double f = e + (double)(2.0f * random.nextFloat() - 1.0f) * d;
        double g = Math.sqrt(random.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)i;
        double h = -g * Math.sin(f);
        double n = g * Math.cos(f);
        if (Math.abs(h) > (double)i || Math.abs(n) > (double)i) {
            return null;
        }
        int o = random.nextInt(2 * j + 1) - j;
        return new BlockPos(h, (double)o, n);
    }

    private static BlockPos moveAboveSolid(BlockPos blockPos, PathfinderMob pathfinderMob) {
        if (pathfinderMob.level.getBlockState(blockPos).getMaterial().isSolid()) {
            BlockPos blockPos2 = blockPos.above();
            while (blockPos2.getY() < pathfinderMob.level.getMaxBuildHeight() && pathfinderMob.level.getBlockState(blockPos2).getMaterial().isSolid()) {
                blockPos2 = blockPos2.above();
            }
            return blockPos2;
        }
        return blockPos;
    }

    private static boolean isWaterDestination(BlockPos blockPos, PathfinderMob pathfinderMob) {
        return pathfinderMob.level.getFluidState(blockPos).is(FluidTags.WATER);
    }
}

