/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import com.mojang.math.OctahedralGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Direction;

public enum Rotation {
    NONE(OctahedralGroup.IDENTITY),
    CLOCKWISE_90(OctahedralGroup.ROT_90_Y_NEG),
    CLOCKWISE_180(OctahedralGroup.ROT_180_FACE_XZ),
    COUNTERCLOCKWISE_90(OctahedralGroup.ROT_90_Y_POS);

    private final OctahedralGroup rotation;

    private Rotation(OctahedralGroup octahedralGroup) {
        this.rotation = octahedralGroup;
    }

    public Rotation getRotated(Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                switch (this) {
                    case NONE: {
                        return CLOCKWISE_180;
                    }
                    case CLOCKWISE_90: {
                        return COUNTERCLOCKWISE_90;
                    }
                    case CLOCKWISE_180: {
                        return NONE;
                    }
                    case COUNTERCLOCKWISE_90: {
                        return CLOCKWISE_90;
                    }
                }
            }
            case COUNTERCLOCKWISE_90: {
                switch (this) {
                    case NONE: {
                        return COUNTERCLOCKWISE_90;
                    }
                    case CLOCKWISE_90: {
                        return NONE;
                    }
                    case CLOCKWISE_180: {
                        return CLOCKWISE_90;
                    }
                    case COUNTERCLOCKWISE_90: {
                        return CLOCKWISE_180;
                    }
                }
            }
            case CLOCKWISE_90: {
                switch (this) {
                    case NONE: {
                        return CLOCKWISE_90;
                    }
                    case CLOCKWISE_90: {
                        return CLOCKWISE_180;
                    }
                    case CLOCKWISE_180: {
                        return COUNTERCLOCKWISE_90;
                    }
                    case COUNTERCLOCKWISE_90: {
                        return NONE;
                    }
                }
            }
        }
        return this;
    }

    public OctahedralGroup rotation() {
        return this.rotation;
    }

    public Direction rotate(Direction direction) {
        if (direction.getAxis() == Direction.Axis.Y) {
            return direction;
        }
        switch (this) {
            case CLOCKWISE_180: {
                return direction.getOpposite();
            }
            case COUNTERCLOCKWISE_90: {
                return direction.getCounterClockWise();
            }
            case CLOCKWISE_90: {
                return direction.getClockWise();
            }
        }
        return direction;
    }

    public int rotate(int i, int j) {
        switch (this) {
            case CLOCKWISE_180: {
                return (i + j / 2) % j;
            }
            case COUNTERCLOCKWISE_90: {
                return (i + j * 3 / 4) % j;
            }
            case CLOCKWISE_90: {
                return (i + j / 4) % j;
            }
        }
        return i;
    }

    public static Rotation getRandom(Random random) {
        Rotation[] rotations = Rotation.values();
        return rotations[random.nextInt(rotations.length)];
    }

    public static List<Rotation> getShuffled(Random random) {
        ArrayList<Rotation> list = Lists.newArrayList(Rotation.values());
        Collections.shuffle(list, random);
        return list;
    }
}

