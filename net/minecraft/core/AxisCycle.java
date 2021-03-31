/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import net.minecraft.core.Direction;

public enum AxisCycle {
    NONE{

        @Override
        public int cycle(int i, int j, int k, Direction.Axis axis) {
            return axis.choose(i, j, k);
        }

        @Override
        public double cycle(double d, double e, double f, Direction.Axis axis) {
            return axis.choose(d, e, f);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis axis) {
            return axis;
        }

        @Override
        public AxisCycle inverse() {
            return this;
        }
    }
    ,
    FORWARD{

        @Override
        public int cycle(int i, int j, int k, Direction.Axis axis) {
            return axis.choose(k, i, j);
        }

        @Override
        public double cycle(double d, double e, double f, Direction.Axis axis) {
            return axis.choose(f, d, e);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis axis) {
            return AXIS_VALUES[Math.floorMod(axis.ordinal() + 1, 3)];
        }

        @Override
        public AxisCycle inverse() {
            return BACKWARD;
        }
    }
    ,
    BACKWARD{

        @Override
        public int cycle(int i, int j, int k, Direction.Axis axis) {
            return axis.choose(j, k, i);
        }

        @Override
        public double cycle(double d, double e, double f, Direction.Axis axis) {
            return axis.choose(e, f, d);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis axis) {
            return AXIS_VALUES[Math.floorMod(axis.ordinal() - 1, 3)];
        }

        @Override
        public AxisCycle inverse() {
            return FORWARD;
        }
    };

    public static final Direction.Axis[] AXIS_VALUES;
    public static final AxisCycle[] VALUES;

    public abstract int cycle(int var1, int var2, int var3, Direction.Axis var4);

    public abstract double cycle(double var1, double var3, double var5, Direction.Axis var7);

    public abstract Direction.Axis cycle(Direction.Axis var1);

    public abstract AxisCycle inverse();

    public static AxisCycle between(Direction.Axis axis, Direction.Axis axis2) {
        return VALUES[Math.floorMod(axis2.ordinal() - axis.ordinal(), 3)];
    }

    static {
        AXIS_VALUES = Direction.Axis.values();
        VALUES = AxisCycle.values();
    }
}

