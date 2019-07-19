/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;
import net.minecraft.core.Direction;

public enum Direction8 {
    NORTH(Direction.NORTH),
    NORTH_EAST(Direction.NORTH, Direction.EAST),
    EAST(Direction.EAST),
    SOUTH_EAST(Direction.SOUTH, Direction.EAST),
    SOUTH(Direction.SOUTH),
    SOUTH_WEST(Direction.SOUTH, Direction.WEST),
    WEST(Direction.WEST),
    NORTH_WEST(Direction.NORTH, Direction.WEST);

    private static final int NORTH_WEST_MASK;
    private static final int WEST_MASK;
    private static final int SOUTH_WEST_MASK;
    private static final int SOUTH_MASK;
    private static final int SOUTH_EAST_MASK;
    private static final int EAST_MASK;
    private static final int NORTH_EAST_MASK;
    private static final int NORTH_MASK;
    private final Set<Direction> directions;

    private Direction8(Direction ... directions) {
        this.directions = Sets.immutableEnumSet(Arrays.asList(directions));
    }

    public Set<Direction> getDirections() {
        return this.directions;
    }

    static {
        NORTH_WEST_MASK = 1 << NORTH_WEST.ordinal();
        WEST_MASK = 1 << WEST.ordinal();
        SOUTH_WEST_MASK = 1 << SOUTH_WEST.ordinal();
        SOUTH_MASK = 1 << SOUTH.ordinal();
        SOUTH_EAST_MASK = 1 << SOUTH_EAST.ordinal();
        EAST_MASK = 1 << EAST.ordinal();
        NORTH_EAST_MASK = 1 << NORTH_EAST.ordinal();
        NORTH_MASK = 1 << NORTH.ordinal();
    }
}

