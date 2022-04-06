/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RandomPos {
    private static final int RANDOM_POS_ATTEMPTS = 10;

    public static BlockPos generateRandomDirection(RandomSource randomSource, int i, int j) {
        int k = randomSource.nextInt(2 * i + 1) - i;
        int l = randomSource.nextInt(2 * j + 1) - j;
        int m = randomSource.nextInt(2 * i + 1) - i;
        return new BlockPos(k, l, m);
    }

    @Nullable
    public static BlockPos generateRandomDirectionWithinRadians(RandomSource randomSource, int i, int j, int k, double d, double e, double f) {
        double g = Mth.atan2(e, d) - 1.5707963705062866;
        double h = g + (double)(2.0f * randomSource.nextFloat() - 1.0f) * f;
        double l = Math.sqrt(randomSource.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)i;
        double m = -l * Math.sin(h);
        double n = l * Math.cos(h);
        if (Math.abs(m) > (double)i || Math.abs(n) > (double)i) {
            return null;
        }
        int o = randomSource.nextInt(2 * j + 1) - j + k;
        return new BlockPos(m, (double)o, n);
    }

    @VisibleForTesting
    public static BlockPos moveUpOutOfSolid(BlockPos blockPos, int i, Predicate<BlockPos> predicate) {
        if (predicate.test(blockPos)) {
            BlockPos blockPos2 = blockPos.above();
            while (blockPos2.getY() < i && predicate.test(blockPos2)) {
                blockPos2 = blockPos2.above();
            }
            return blockPos2;
        }
        return blockPos;
    }

    @VisibleForTesting
    public static BlockPos moveUpToAboveSolid(BlockPos blockPos, int i, int j, Predicate<BlockPos> predicate) {
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

    @Nullable
    public static Vec3 generateRandomPos(PathfinderMob pathfinderMob, Supplier<BlockPos> supplier) {
        return RandomPos.generateRandomPos(supplier, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 generateRandomPos(Supplier<BlockPos> supplier, ToDoubleFunction<BlockPos> toDoubleFunction) {
        double d = Double.NEGATIVE_INFINITY;
        BlockPos blockPos = null;
        for (int i = 0; i < 10; ++i) {
            double e;
            BlockPos blockPos2 = supplier.get();
            if (blockPos2 == null || !((e = toDoubleFunction.applyAsDouble(blockPos2)) > d)) continue;
            d = e;
            blockPos = blockPos2;
        }
        return blockPos != null ? Vec3.atBottomCenterOf(blockPos) : null;
    }

    public static BlockPos generateRandomPosTowardDirection(PathfinderMob pathfinderMob, int i, RandomSource randomSource, BlockPos blockPos) {
        int j = blockPos.getX();
        int k = blockPos.getZ();
        if (pathfinderMob.hasRestriction() && i > 1) {
            BlockPos blockPos2 = pathfinderMob.getRestrictCenter();
            j = pathfinderMob.getX() > (double)blockPos2.getX() ? (j -= randomSource.nextInt(i / 2)) : (j += randomSource.nextInt(i / 2));
            k = pathfinderMob.getZ() > (double)blockPos2.getZ() ? (k -= randomSource.nextInt(i / 2)) : (k += randomSource.nextInt(i / 2));
        }
        return new BlockPos((double)j + pathfinderMob.getX(), (double)blockPos.getY() + pathfinderMob.getY(), (double)k + pathfinderMob.getZ());
    }
}

