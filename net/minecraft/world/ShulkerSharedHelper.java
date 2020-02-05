/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

public class ShulkerSharedHelper {
    public static AABB openBoundingBox(BlockPos blockPos, Direction direction) {
        return Shapes.block().bounds().expandTowards(0.5f * (float)direction.getStepX(), 0.5f * (float)direction.getStepY(), 0.5f * (float)direction.getStepZ()).contract(direction.getStepX(), direction.getStepY(), direction.getStepZ()).move(blockPos.relative(direction));
    }
}

