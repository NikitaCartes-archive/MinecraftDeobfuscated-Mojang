/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockUtil {
    public static FoundRectangle getLargestRectangleAround(BlockPos blockPos, Direction.Axis axis, int i, Direction.Axis axis2, int j, Predicate<BlockPos> predicate) {
        IntBounds intBounds;
        int o;
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        Direction direction = Direction.get(Direction.AxisDirection.NEGATIVE, axis);
        Direction direction2 = direction.getOpposite();
        Direction direction3 = Direction.get(Direction.AxisDirection.NEGATIVE, axis2);
        Direction direction4 = direction3.getOpposite();
        int k = BlockUtil.getLimit(predicate, mutableBlockPos.set(blockPos), direction, i);
        int l = BlockUtil.getLimit(predicate, mutableBlockPos.set(blockPos), direction2, i);
        int m = k;
        IntBounds[] intBoundss = new IntBounds[m + 1 + l];
        intBoundss[m] = new IntBounds(BlockUtil.getLimit(predicate, mutableBlockPos.set(blockPos), direction3, j), BlockUtil.getLimit(predicate, mutableBlockPos.set(blockPos), direction4, j));
        int n = intBoundss[m].min;
        for (o = 1; o <= k; ++o) {
            intBounds = intBoundss[m - (o - 1)];
            intBoundss[m - o] = new IntBounds(BlockUtil.getLimit(predicate, mutableBlockPos.set(blockPos).move(direction, o), direction3, intBounds.min), BlockUtil.getLimit(predicate, mutableBlockPos.set(blockPos).move(direction, o), direction4, intBounds.max));
        }
        for (o = 1; o <= l; ++o) {
            intBounds = intBoundss[m + o - 1];
            intBoundss[m + o] = new IntBounds(BlockUtil.getLimit(predicate, mutableBlockPos.set(blockPos).move(direction2, o), direction3, intBounds.min), BlockUtil.getLimit(predicate, mutableBlockPos.set(blockPos).move(direction2, o), direction4, intBounds.max));
        }
        o = 0;
        int p = 0;
        int q = 0;
        int r = 0;
        int[] is = new int[intBoundss.length];
        for (int s = n; s >= 0; --s) {
            int v;
            int u;
            IntBounds intBounds2;
            for (int t = 0; t < intBoundss.length; ++t) {
                intBounds2 = intBoundss[t];
                u = n - intBounds2.min;
                v = n + intBounds2.max;
                is[t] = s >= u && s <= v ? v + 1 - s : 0;
            }
            Pair<IntBounds, Integer> pair = BlockUtil.getMaxRectangleLocation(is);
            intBounds2 = pair.getFirst();
            u = 1 + intBounds2.max - intBounds2.min;
            v = pair.getSecond();
            if (u * v <= q * r) continue;
            o = intBounds2.min;
            p = s;
            q = u;
            r = v;
        }
        return new FoundRectangle(blockPos.relative(axis, o - m).relative(axis2, p - n), q, r);
    }

    private static int getLimit(Predicate<BlockPos> predicate, BlockPos.MutableBlockPos mutableBlockPos, Direction direction, int i) {
        int j;
        for (j = 0; j < i && predicate.test(mutableBlockPos.move(direction)); ++j) {
        }
        return j;
    }

    @VisibleForTesting
    static Pair<IntBounds, Integer> getMaxRectangleLocation(int[] is) {
        int i = 0;
        int j = 0;
        int k = 0;
        IntArrayList intStack = new IntArrayList();
        intStack.push(0);
        for (int l = 1; l <= is.length; ++l) {
            int m;
            int n = m = l == is.length ? 0 : is[l];
            while (!intStack.isEmpty()) {
                int n2 = is[intStack.topInt()];
                if (m >= n2) {
                    intStack.push(l);
                    break;
                }
                intStack.popInt();
                int o = intStack.isEmpty() ? 0 : intStack.topInt() + 1;
                if (n2 * (l - o) <= k * (j - i)) continue;
                j = l;
                i = o;
                k = n2;
            }
            if (!intStack.isEmpty()) continue;
            intStack.push(l);
        }
        return new Pair<IntBounds, Integer>(new IntBounds(i, j - 1), k);
    }

    public static class FoundRectangle {
        public final BlockPos minCorner;
        public final int axis1Size;
        public final int axis2Size;

        public FoundRectangle(BlockPos blockPos, int i, int j) {
            this.minCorner = blockPos;
            this.axis1Size = i;
            this.axis2Size = j;
        }
    }

    public static class IntBounds {
        public final int min;
        public final int max;

        public IntBounds(int i, int j) {
            this.min = i;
            this.max = j;
        }

        public String toString() {
            return "IntBounds{min=" + this.min + ", max=" + this.max + '}';
        }
    }
}

