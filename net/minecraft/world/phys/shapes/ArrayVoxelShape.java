/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ArrayVoxelShape
extends VoxelShape {
    private final DoubleList xs;
    private final DoubleList ys;
    private final DoubleList zs;

    protected ArrayVoxelShape(DiscreteVoxelShape discreteVoxelShape, double[] ds, double[] es, double[] fs) {
        this(discreteVoxelShape, DoubleArrayList.wrap(Arrays.copyOf(ds, discreteVoxelShape.getXSize() + 1)), DoubleArrayList.wrap(Arrays.copyOf(es, discreteVoxelShape.getYSize() + 1)), DoubleArrayList.wrap(Arrays.copyOf(fs, discreteVoxelShape.getZSize() + 1)));
    }

    ArrayVoxelShape(DiscreteVoxelShape discreteVoxelShape, DoubleList doubleList, DoubleList doubleList2, DoubleList doubleList3) {
        super(discreteVoxelShape);
        int i = discreteVoxelShape.getXSize() + 1;
        int j = discreteVoxelShape.getYSize() + 1;
        int k = discreteVoxelShape.getZSize() + 1;
        if (i != doubleList.size() || j != doubleList2.size() || k != doubleList3.size()) {
            throw Util.pauseInIde(new IllegalArgumentException("Lengths of point arrays must be consistent with the size of the VoxelShape."));
        }
        this.xs = doubleList;
        this.ys = doubleList2;
        this.zs = doubleList3;
    }

    @Override
    protected DoubleList getCoords(Direction.Axis axis) {
        switch (axis) {
            case X: {
                return this.xs;
            }
            case Y: {
                return this.ys;
            }
            case Z: {
                return this.zs;
            }
        }
        throw new IllegalArgumentException();
    }
}

