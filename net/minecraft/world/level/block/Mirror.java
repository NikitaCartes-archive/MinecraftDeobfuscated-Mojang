/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;

public enum Mirror implements StringRepresentable
{
    NONE("none", OctahedralGroup.IDENTITY),
    LEFT_RIGHT("left_right", OctahedralGroup.INVERT_Z),
    FRONT_BACK("front_back", OctahedralGroup.INVERT_X);

    public static final Codec<Mirror> CODEC;
    private final String id;
    private final Component symbol;
    private final OctahedralGroup rotation;

    private Mirror(String string2, OctahedralGroup octahedralGroup) {
        this.id = string2;
        this.symbol = Component.translatable("mirror." + string2);
        this.rotation = octahedralGroup;
    }

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

    public OctahedralGroup rotation() {
        return this.rotation;
    }

    public Component symbol() {
        return this.symbol;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Mirror::values);
    }
}

