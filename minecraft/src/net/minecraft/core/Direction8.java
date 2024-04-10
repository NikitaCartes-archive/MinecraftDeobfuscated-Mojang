package net.minecraft.core;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;

public enum Direction8 {
	NORTH(Direction.NORTH),
	NORTH_EAST(Direction.NORTH, Direction.EAST),
	EAST(Direction.EAST),
	SOUTH_EAST(Direction.SOUTH, Direction.EAST),
	SOUTH(Direction.SOUTH),
	SOUTH_WEST(Direction.SOUTH, Direction.WEST),
	WEST(Direction.WEST),
	NORTH_WEST(Direction.NORTH, Direction.WEST);

	private final Set<Direction> directions;
	private final Vec3i step;

	private Direction8(final Direction... directions) {
		this.directions = Sets.immutableEnumSet(Arrays.asList(directions));
		this.step = new Vec3i(0, 0, 0);

		for (Direction direction : directions) {
			this.step.setX(this.step.getX() + direction.getStepX()).setY(this.step.getY() + direction.getStepY()).setZ(this.step.getZ() + direction.getStepZ());
		}
	}

	public Set<Direction> getDirections() {
		return this.directions;
	}

	public int getStepX() {
		return this.step.getX();
	}

	public int getStepZ() {
		return this.step.getZ();
	}
}
