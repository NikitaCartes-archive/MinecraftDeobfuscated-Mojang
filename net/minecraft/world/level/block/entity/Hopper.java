/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface Hopper
extends Container {
    public static final VoxelShape INSIDE = Block.box(2.0, 11.0, 2.0, 14.0, 16.0, 14.0);
    public static final VoxelShape ABOVE = Block.box(0.0, 16.0, 0.0, 16.0, 32.0, 16.0);
    public static final VoxelShape SUCK = Shapes.or(INSIDE, ABOVE);

    default public VoxelShape getSuckShape() {
        return SUCK;
    }

    public double getLevelX();

    public double getLevelY();

    public double getLevelZ();
}

