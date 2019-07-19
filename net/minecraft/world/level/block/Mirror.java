/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

public enum Mirror {
    NONE,
    LEFT_RIGHT,
    FRONT_BACK;


    public int mirror(int i, int j) {
        int k = j / 2;
        int l = i > k ? i - j : i;
        switch (this) {
            case FRONT_BACK: {
                return (j - l) % j;
            }
            case LEFT_RIGHT: {
                return (k - l + j) % j;
            }
        }
        return i;
    }

    public Rotation getRotation(Direction direction) {
        Direction.Axis axis = direction.getAxis();
        return this == LEFT_RIGHT && axis == Direction.Axis.Z || this == FRONT_BACK && axis == Direction.Axis.X ? Rotation.CLOCKWISE_180 : Rotation.NONE;
    }

    public Direction mirror(Direction direction) {
        if (this == FRONT_BACK && direction.getAxis() == Direction.Axis.X) {
            return direction.getOpposite();
        }
        if (this == LEFT_RIGHT && direction.getAxis() == Direction.Axis.Z) {
            return direction.getOpposite();
        }
        return direction;
    }
}

