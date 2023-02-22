/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments.coordinates;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface Coordinates {
    public Vec3 getPosition(CommandSourceStack var1);

    public Vec2 getRotation(CommandSourceStack var1);

    default public BlockPos getBlockPos(CommandSourceStack commandSourceStack) {
        return BlockPos.containing(this.getPosition(commandSourceStack));
    }

    public boolean isXRelative();

    public boolean isYRelative();

    public boolean isZRelative();
}

