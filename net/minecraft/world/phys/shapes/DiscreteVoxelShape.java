/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys.shapes;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;

public abstract class DiscreteVoxelShape {
    private static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();
    protected final int xSize;
    protected final int ySize;
    protected final int zSize;

    protected DiscreteVoxelShape(int i, int j, int k) {
        if (i < 0 || j < 0 || k < 0) {
            throw new IllegalArgumentException("Need all positive sizes: x: " + i + ", y: " + j + ", z: " + k);
        }
        this.xSize = i;
        this.ySize = j;
        this.zSize = k;
    }

    public boolean isFullWide(AxisCycle axisCycle, int i, int j, int k) {
        return this.isFullWide(axisCycle.cycle(i, j, k, Direction.Axis.X), axisCycle.cycle(i, j, k, Direction.Axis.Y), axisCycle.cycle(i, j, k, Direction.Axis.Z));
    }

    public boolean isFullWide(int i, int j, int k) {
        if (i < 0 || j < 0 || k < 0) {
            return false;
        }
        if (i >= this.xSize || j >= this.ySize || k >= this.zSize) {
            return false;
        }
        return this.isFull(i, j, k);
    }

    public boolean isFull(AxisCycle axisCycle, int i, int j, int k) {
        return this.isFull(axisCycle.cycle(i, j, k, Direction.Axis.X), axisCycle.cycle(i, j, k, Direction.Axis.Y), axisCycle.cycle(i, j, k, Direction.Axis.Z));
    }

    public abstract boolean isFull(int var1, int var2, int var3);

    public abstract void fill(int var1, int var2, int var3);

    public boolean isEmpty() {
        for (Direction.Axis axis : AXIS_VALUES) {
            if (this.firstFull(axis) < this.lastFull(axis)) continue;
            return true;
        }
        return false;
    }

    public abstract int firstFull(Direction.Axis var1);

    public abstract int lastFull(Direction.Axis var1);

    public int firstFull(Direction.Axis axis, int i, int j) {
        int k = this.getSize(axis);
        if (i < 0 || j < 0) {
            return k;
        }
        Direction.Axis axis2 = AxisCycle.FORWARD.cycle(axis);
        Direction.Axis axis3 = AxisCycle.BACKWARD.cycle(axis);
        if (i >= this.getSize(axis2) || j >= this.getSize(axis3)) {
            return k;
        }
        AxisCycle axisCycle = AxisCycle.between(Direction.Axis.X, axis);
        for (int l = 0; l < k; ++l) {
            if (!this.isFull(axisCycle, l, i, j)) continue;
            return l;
        }
        return k;
    }

    public int lastFull(Direction.Axis axis, int i, int j) {
        if (i < 0 || j < 0) {
            return 0;
        }
        Direction.Axis axis2 = AxisCycle.FORWARD.cycle(axis);
        Direction.Axis axis3 = AxisCycle.BACKWARD.cycle(axis);
        if (i >= this.getSize(axis2) || j >= this.getSize(axis3)) {
            return 0;
        }
        int k = this.getSize(axis);
        AxisCycle axisCycle = AxisCycle.between(Direction.Axis.X, axis);
        for (int l = k - 1; l >= 0; --l) {
            if (!this.isFull(axisCycle, l, i, j)) continue;
            return l + 1;
        }
        return 0;
    }

    public int getSize(Direction.Axis axis) {
        return axis.choose(this.xSize, this.ySize, this.zSize);
    }

    public int getXSize() {
        return this.getSize(Direction.Axis.X);
    }

    public int getYSize() {
        return this.getSize(Direction.Axis.Y);
    }

    public int getZSize() {
        return this.getSize(Direction.Axis.Z);
    }

    public void forAllEdges(IntLineConsumer intLineConsumer, boolean bl) {
        this.forAllAxisEdges(intLineConsumer, AxisCycle.NONE, bl);
        this.forAllAxisEdges(intLineConsumer, AxisCycle.FORWARD, bl);
        this.forAllAxisEdges(intLineConsumer, AxisCycle.BACKWARD, bl);
    }

    private void forAllAxisEdges(IntLineConsumer intLineConsumer, AxisCycle axisCycle, boolean bl) {
        AxisCycle axisCycle2 = axisCycle.inverse();
        int i = this.getSize(axisCycle2.cycle(Direction.Axis.X));
        int j = this.getSize(axisCycle2.cycle(Direction.Axis.Y));
        int k = this.getSize(axisCycle2.cycle(Direction.Axis.Z));
        for (int l = 0; l <= i; ++l) {
            for (int m = 0; m <= j; ++m) {
                int n = -1;
                for (int o = 0; o <= k; ++o) {
                    int p = 0;
                    int q = 0;
                    for (int r = 0; r <= 1; ++r) {
                        for (int s = 0; s <= 1; ++s) {
                            if (!this.isFullWide(axisCycle2, l + r - 1, m + s - 1, o)) continue;
                            ++p;
                            q ^= r ^ s;
                        }
                    }
                    if (p == 1 || p == 3 || p == 2 && !(q & true)) {
                        if (bl) {
                            if (n != -1) continue;
                            n = o;
                            continue;
                        }
                        intLineConsumer.consume(axisCycle2.cycle(l, m, o, Direction.Axis.X), axisCycle2.cycle(l, m, o, Direction.Axis.Y), axisCycle2.cycle(l, m, o, Direction.Axis.Z), axisCycle2.cycle(l, m, o + 1, Direction.Axis.X), axisCycle2.cycle(l, m, o + 1, Direction.Axis.Y), axisCycle2.cycle(l, m, o + 1, Direction.Axis.Z));
                        continue;
                    }
                    if (n == -1) continue;
                    intLineConsumer.consume(axisCycle2.cycle(l, m, n, Direction.Axis.X), axisCycle2.cycle(l, m, n, Direction.Axis.Y), axisCycle2.cycle(l, m, n, Direction.Axis.Z), axisCycle2.cycle(l, m, o, Direction.Axis.X), axisCycle2.cycle(l, m, o, Direction.Axis.Y), axisCycle2.cycle(l, m, o, Direction.Axis.Z));
                    n = -1;
                }
            }
        }
    }

    public void forAllBoxes(IntLineConsumer intLineConsumer, boolean bl) {
        BitSetDiscreteVoxelShape.forAllBoxes(this, intLineConsumer, bl);
    }

    public void forAllFaces(IntFaceConsumer intFaceConsumer) {
        this.forAllAxisFaces(intFaceConsumer, AxisCycle.NONE);
        this.forAllAxisFaces(intFaceConsumer, AxisCycle.FORWARD);
        this.forAllAxisFaces(intFaceConsumer, AxisCycle.BACKWARD);
    }

    private void forAllAxisFaces(IntFaceConsumer intFaceConsumer, AxisCycle axisCycle) {
        AxisCycle axisCycle2 = axisCycle.inverse();
        Direction.Axis axis = axisCycle2.cycle(Direction.Axis.Z);
        int i = this.getSize(axisCycle2.cycle(Direction.Axis.X));
        int j = this.getSize(axisCycle2.cycle(Direction.Axis.Y));
        int k = this.getSize(axis);
        Direction direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
        Direction direction2 = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        for (int l = 0; l < i; ++l) {
            for (int m = 0; m < j; ++m) {
                boolean bl = false;
                for (int n = 0; n <= k; ++n) {
                    boolean bl2;
                    boolean bl3 = bl2 = n != k && this.isFull(axisCycle2, l, m, n);
                    if (!bl && bl2) {
                        intFaceConsumer.consume(direction, axisCycle2.cycle(l, m, n, Direction.Axis.X), axisCycle2.cycle(l, m, n, Direction.Axis.Y), axisCycle2.cycle(l, m, n, Direction.Axis.Z));
                    }
                    if (bl && !bl2) {
                        intFaceConsumer.consume(direction2, axisCycle2.cycle(l, m, n - 1, Direction.Axis.X), axisCycle2.cycle(l, m, n - 1, Direction.Axis.Y), axisCycle2.cycle(l, m, n - 1, Direction.Axis.Z));
                    }
                    bl = bl2;
                }
            }
        }
    }

    public static interface IntFaceConsumer {
        public void consume(Direction var1, int var2, int var3, int var4);
    }

    public static interface IntLineConsumer {
        public void consume(int var1, int var2, int var3, int var4, int var5, int var6);
    }
}

