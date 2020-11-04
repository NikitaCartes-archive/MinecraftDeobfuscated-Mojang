/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys.shapes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.CubePointRange;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteCubeMerger;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.IdenticalMerger;
import net.minecraft.world.phys.shapes.IndexMerger;
import net.minecraft.world.phys.shapes.IndirectMerger;
import net.minecraft.world.phys.shapes.NonOverlappingMerger;
import net.minecraft.world.phys.shapes.SliceShape;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class Shapes {
    private static final VoxelShape BLOCK = Util.make(() -> {
        BitSetDiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(1, 1, 1);
        ((DiscreteVoxelShape)discreteVoxelShape).fill(0, 0, 0);
        return new CubeVoxelShape(discreteVoxelShape);
    });
    public static final VoxelShape INFINITY = Shapes.box(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    private static final VoxelShape EMPTY = new ArrayVoxelShape(new BitSetDiscreteVoxelShape(0, 0, 0), new DoubleArrayList(new double[]{0.0}), new DoubleArrayList(new double[]{0.0}), new DoubleArrayList(new double[]{0.0}));

    public static VoxelShape empty() {
        return EMPTY;
    }

    public static VoxelShape block() {
        return BLOCK;
    }

    public static VoxelShape box(double d, double e, double f, double g, double h, double i) {
        if (d > g || e > h || f > i) {
            throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
        }
        return Shapes.create(d, e, f, g, h, i);
    }

    public static VoxelShape create(double d, double e, double f, double g, double h, double i) {
        if (g - d < 1.0E-7 || h - e < 1.0E-7 || i - f < 1.0E-7) {
            return Shapes.empty();
        }
        int j = Shapes.findBits(d, g);
        int k = Shapes.findBits(e, h);
        int l = Shapes.findBits(f, i);
        if (j < 0 || k < 0 || l < 0) {
            return new ArrayVoxelShape(Shapes.BLOCK.shape, DoubleArrayList.wrap(new double[]{d, g}), DoubleArrayList.wrap(new double[]{e, h}), DoubleArrayList.wrap(new double[]{f, i}));
        }
        if (j == 0 && k == 0 && l == 0) {
            return Shapes.block();
        }
        int m = 1 << j;
        int n = 1 << k;
        int o = 1 << l;
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = BitSetDiscreteVoxelShape.withFilledBounds(m, n, o, (int)Math.round(d * (double)m), (int)Math.round(e * (double)n), (int)Math.round(f * (double)o), (int)Math.round(g * (double)m), (int)Math.round(h * (double)n), (int)Math.round(i * (double)o));
        return new CubeVoxelShape(bitSetDiscreteVoxelShape);
    }

    public static VoxelShape create(AABB aABB) {
        return Shapes.create(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
    }

    @VisibleForTesting
    protected static int findBits(double d, double e) {
        if (d < -1.0E-7 || e > 1.0000001) {
            return -1;
        }
        for (int i = 0; i <= 3; ++i) {
            boolean bl2;
            int j = 1 << i;
            double f = d * (double)j;
            double g = e * (double)j;
            boolean bl = Math.abs(f - (double)Math.round(f)) < 1.0E-7 * (double)j;
            boolean bl3 = bl2 = Math.abs(g - (double)Math.round(g)) < 1.0E-7 * (double)j;
            if (!bl || !bl2) continue;
            return i;
        }
        return -1;
    }

    protected static long lcm(int i, int j) {
        return (long)i * (long)(j / IntMath.gcd(i, j));
    }

    public static VoxelShape or(VoxelShape voxelShape, VoxelShape voxelShape2) {
        return Shapes.join(voxelShape, voxelShape2, BooleanOp.OR);
    }

    public static VoxelShape or(VoxelShape voxelShape, VoxelShape ... voxelShapes) {
        return Arrays.stream(voxelShapes).reduce(voxelShape, Shapes::or);
    }

    public static VoxelShape join(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
        return Shapes.joinUnoptimized(voxelShape, voxelShape2, booleanOp).optimize();
    }

    public static VoxelShape joinUnoptimized(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
        if (booleanOp.apply(false, false)) {
            throw Util.pauseInIde(new IllegalArgumentException());
        }
        if (voxelShape == voxelShape2) {
            return booleanOp.apply(true, true) ? voxelShape : Shapes.empty();
        }
        boolean bl = booleanOp.apply(true, false);
        boolean bl2 = booleanOp.apply(false, true);
        if (voxelShape.isEmpty()) {
            return bl2 ? voxelShape2 : Shapes.empty();
        }
        if (voxelShape2.isEmpty()) {
            return bl ? voxelShape : Shapes.empty();
        }
        IndexMerger indexMerger = Shapes.createIndexMerger(1, voxelShape.getCoords(Direction.Axis.X), voxelShape2.getCoords(Direction.Axis.X), bl, bl2);
        IndexMerger indexMerger2 = Shapes.createIndexMerger(indexMerger.size() - 1, voxelShape.getCoords(Direction.Axis.Y), voxelShape2.getCoords(Direction.Axis.Y), bl, bl2);
        IndexMerger indexMerger3 = Shapes.createIndexMerger((indexMerger.size() - 1) * (indexMerger2.size() - 1), voxelShape.getCoords(Direction.Axis.Z), voxelShape2.getCoords(Direction.Axis.Z), bl, bl2);
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = BitSetDiscreteVoxelShape.join(voxelShape.shape, voxelShape2.shape, indexMerger, indexMerger2, indexMerger3, booleanOp);
        if (indexMerger instanceof DiscreteCubeMerger && indexMerger2 instanceof DiscreteCubeMerger && indexMerger3 instanceof DiscreteCubeMerger) {
            return new CubeVoxelShape(bitSetDiscreteVoxelShape);
        }
        return new ArrayVoxelShape(bitSetDiscreteVoxelShape, indexMerger.getList(), indexMerger2.getList(), indexMerger3.getList());
    }

    public static boolean joinIsNotEmpty(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
        if (booleanOp.apply(false, false)) {
            throw Util.pauseInIde(new IllegalArgumentException());
        }
        boolean bl = voxelShape.isEmpty();
        boolean bl2 = voxelShape2.isEmpty();
        if (bl || bl2) {
            return booleanOp.apply(!bl, !bl2);
        }
        if (voxelShape == voxelShape2) {
            return booleanOp.apply(true, true);
        }
        boolean bl3 = booleanOp.apply(true, false);
        boolean bl4 = booleanOp.apply(false, true);
        for (Direction.Axis axis : AxisCycle.AXIS_VALUES) {
            if (voxelShape.max(axis) < voxelShape2.min(axis) - 1.0E-7) {
                return bl3 || bl4;
            }
            if (!(voxelShape2.max(axis) < voxelShape.min(axis) - 1.0E-7)) continue;
            return bl3 || bl4;
        }
        IndexMerger indexMerger = Shapes.createIndexMerger(1, voxelShape.getCoords(Direction.Axis.X), voxelShape2.getCoords(Direction.Axis.X), bl3, bl4);
        IndexMerger indexMerger2 = Shapes.createIndexMerger(indexMerger.size() - 1, voxelShape.getCoords(Direction.Axis.Y), voxelShape2.getCoords(Direction.Axis.Y), bl3, bl4);
        IndexMerger indexMerger3 = Shapes.createIndexMerger((indexMerger.size() - 1) * (indexMerger2.size() - 1), voxelShape.getCoords(Direction.Axis.Z), voxelShape2.getCoords(Direction.Axis.Z), bl3, bl4);
        return Shapes.joinIsNotEmpty(indexMerger, indexMerger2, indexMerger3, voxelShape.shape, voxelShape2.shape, booleanOp);
    }

    private static boolean joinIsNotEmpty(IndexMerger indexMerger, IndexMerger indexMerger2, IndexMerger indexMerger3, DiscreteVoxelShape discreteVoxelShape, DiscreteVoxelShape discreteVoxelShape2, BooleanOp booleanOp) {
        return !indexMerger.forMergedIndexes((i, j, k2) -> indexMerger2.forMergedIndexes((k, l, m2) -> indexMerger3.forMergedIndexes((m, n, o) -> !booleanOp.apply(discreteVoxelShape.isFullWide(i, k, m), discreteVoxelShape2.isFullWide(j, l, n)))));
    }

    public static double collide(Direction.Axis axis, AABB aABB, Stream<VoxelShape> stream, double d) {
        Iterator iterator = stream.iterator();
        while (iterator.hasNext()) {
            if (Math.abs(d) < 1.0E-7) {
                return 0.0;
            }
            d = ((VoxelShape)iterator.next()).collide(axis, aABB, d);
        }
        return d;
    }

    public static double collide(Direction.Axis axis, AABB aABB, LevelReader levelReader, double d, CollisionContext collisionContext, Stream<VoxelShape> stream) {
        return Shapes.collide(aABB, levelReader, d, collisionContext, AxisCycle.between(axis, Direction.Axis.Z), stream);
    }

    private static double collide(AABB aABB, LevelReader levelReader, double d, CollisionContext collisionContext, AxisCycle axisCycle, Stream<VoxelShape> stream) {
        if (aABB.getXsize() < 1.0E-6 || aABB.getYsize() < 1.0E-6 || aABB.getZsize() < 1.0E-6) {
            return d;
        }
        if (Math.abs(d) < 1.0E-7) {
            return 0.0;
        }
        AxisCycle axisCycle2 = axisCycle.inverse();
        Direction.Axis axis = axisCycle2.cycle(Direction.Axis.X);
        Direction.Axis axis2 = axisCycle2.cycle(Direction.Axis.Y);
        Direction.Axis axis3 = axisCycle2.cycle(Direction.Axis.Z);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int i = Mth.floor(aABB.min(axis) - 1.0E-7) - 1;
        int j = Mth.floor(aABB.max(axis) + 1.0E-7) + 1;
        int k = Mth.floor(aABB.min(axis2) - 1.0E-7) - 1;
        int l = Mth.floor(aABB.max(axis2) + 1.0E-7) + 1;
        double e = aABB.min(axis3) - 1.0E-7;
        double f = aABB.max(axis3) + 1.0E-7;
        boolean bl = d > 0.0;
        int m = bl ? Mth.floor(aABB.max(axis3) - 1.0E-7) - 1 : Mth.floor(aABB.min(axis3) + 1.0E-7) + 1;
        int n = Shapes.lastC(d, e, f);
        int o = bl ? 1 : -1;
        int p = m;
        while (bl ? p <= n : p >= n) {
            for (int q = i; q <= j; ++q) {
                for (int r = k; r <= l; ++r) {
                    int s = 0;
                    if (q == i || q == j) {
                        ++s;
                    }
                    if (r == k || r == l) {
                        ++s;
                    }
                    if (p == m || p == n) {
                        ++s;
                    }
                    if (s >= 3) continue;
                    mutableBlockPos.set(axisCycle2, q, r, p);
                    BlockState blockState = levelReader.getBlockState(mutableBlockPos);
                    if (s == 1 && !blockState.hasLargeCollisionShape() || s == 2 && !blockState.is(Blocks.MOVING_PISTON)) continue;
                    d = blockState.getCollisionShape(levelReader, mutableBlockPos, collisionContext).collide(axis3, aABB.move(-mutableBlockPos.getX(), -mutableBlockPos.getY(), -mutableBlockPos.getZ()), d);
                    if (Math.abs(d) < 1.0E-7) {
                        return 0.0;
                    }
                    n = Shapes.lastC(d, e, f);
                }
            }
            p += o;
        }
        double[] ds = new double[]{d};
        stream.forEach(voxelShape -> {
            ds[0] = voxelShape.collide(axis3, aABB, ds[0]);
        });
        return ds[0];
    }

    private static int lastC(double d, double e, double f) {
        return d > 0.0 ? Mth.floor(f + d) + 1 : Mth.floor(e + d) - 1;
    }

    @Environment(value=EnvType.CLIENT)
    public static boolean blockOccudes(VoxelShape voxelShape, VoxelShape voxelShape2, Direction direction) {
        if (voxelShape == Shapes.block() && voxelShape2 == Shapes.block()) {
            return true;
        }
        if (voxelShape2.isEmpty()) {
            return false;
        }
        Direction.Axis axis = direction.getAxis();
        Direction.AxisDirection axisDirection = direction.getAxisDirection();
        VoxelShape voxelShape3 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape : voxelShape2;
        VoxelShape voxelShape4 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape2 : voxelShape;
        BooleanOp booleanOp = axisDirection == Direction.AxisDirection.POSITIVE ? BooleanOp.ONLY_FIRST : BooleanOp.ONLY_SECOND;
        return DoubleMath.fuzzyEquals(voxelShape3.max(axis), 1.0, 1.0E-7) && DoubleMath.fuzzyEquals(voxelShape4.min(axis), 0.0, 1.0E-7) && !Shapes.joinIsNotEmpty(new SliceShape(voxelShape3, axis, voxelShape3.shape.getSize(axis) - 1), new SliceShape(voxelShape4, axis, 0), booleanOp);
    }

    public static VoxelShape getFaceShape(VoxelShape voxelShape, Direction direction) {
        int i;
        boolean bl;
        if (voxelShape == Shapes.block()) {
            return Shapes.block();
        }
        Direction.Axis axis = direction.getAxis();
        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            bl = DoubleMath.fuzzyEquals(voxelShape.max(axis), 1.0, 1.0E-7);
            i = voxelShape.shape.getSize(axis) - 1;
        } else {
            bl = DoubleMath.fuzzyEquals(voxelShape.min(axis), 0.0, 1.0E-7);
            i = 0;
        }
        if (!bl) {
            return Shapes.empty();
        }
        return new SliceShape(voxelShape, axis, i);
    }

    public static boolean mergedFaceOccludes(VoxelShape voxelShape, VoxelShape voxelShape2, Direction direction) {
        VoxelShape voxelShape4;
        if (voxelShape == Shapes.block() || voxelShape2 == Shapes.block()) {
            return true;
        }
        Direction.Axis axis = direction.getAxis();
        Direction.AxisDirection axisDirection = direction.getAxisDirection();
        VoxelShape voxelShape3 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape : voxelShape2;
        VoxelShape voxelShape5 = voxelShape4 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape2 : voxelShape;
        if (!DoubleMath.fuzzyEquals(voxelShape3.max(axis), 1.0, 1.0E-7)) {
            voxelShape3 = Shapes.empty();
        }
        if (!DoubleMath.fuzzyEquals(voxelShape4.min(axis), 0.0, 1.0E-7)) {
            voxelShape4 = Shapes.empty();
        }
        return !Shapes.joinIsNotEmpty(Shapes.block(), Shapes.joinUnoptimized(new SliceShape(voxelShape3, axis, voxelShape3.shape.getSize(axis) - 1), new SliceShape(voxelShape4, axis, 0), BooleanOp.OR), BooleanOp.ONLY_FIRST);
    }

    public static boolean faceShapeOccludes(VoxelShape voxelShape, VoxelShape voxelShape2) {
        if (voxelShape == Shapes.block() || voxelShape2 == Shapes.block()) {
            return true;
        }
        if (voxelShape.isEmpty() && voxelShape2.isEmpty()) {
            return false;
        }
        return !Shapes.joinIsNotEmpty(Shapes.block(), Shapes.joinUnoptimized(voxelShape, voxelShape2, BooleanOp.OR), BooleanOp.ONLY_FIRST);
    }

    @VisibleForTesting
    protected static IndexMerger createIndexMerger(int i, DoubleList doubleList, DoubleList doubleList2, boolean bl, boolean bl2) {
        long l;
        int j = doubleList.size() - 1;
        int k = doubleList2.size() - 1;
        if (doubleList instanceof CubePointRange && doubleList2 instanceof CubePointRange && (long)i * (l = Shapes.lcm(j, k)) <= 256L) {
            return new DiscreteCubeMerger(j, k);
        }
        if (doubleList.getDouble(j) < doubleList2.getDouble(0) - 1.0E-7) {
            return new NonOverlappingMerger(doubleList, doubleList2, false);
        }
        if (doubleList2.getDouble(k) < doubleList.getDouble(0) - 1.0E-7) {
            return new NonOverlappingMerger(doubleList2, doubleList, true);
        }
        if (j == k && Objects.equals(doubleList, doubleList2)) {
            return new IdenticalMerger(doubleList);
        }
        return new IndirectMerger(doubleList, doubleList2, bl, bl2);
    }

    public static interface DoubleLineConsumer {
        public void consume(double var1, double var3, double var5, double var7, double var9, double var11);
    }
}

