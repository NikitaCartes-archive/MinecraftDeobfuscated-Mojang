package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import net.minecraft.core.Direction;

public enum Mirror {
	NONE(OctahedralGroup.IDENTITY),
	LEFT_RIGHT(OctahedralGroup.INVERT_Z),
	FRONT_BACK(OctahedralGroup.INVERT_X);

	private final OctahedralGroup rotation;

	private Mirror(OctahedralGroup octahedralGroup) {
		this.rotation = octahedralGroup;
	}

	public int mirror(int i, int j) {
		int k = j / 2;
		int l = i > k ? i - j : i;
		switch (this) {
			case FRONT_BACK:
				return (j - l) % j;
			case LEFT_RIGHT:
				return (k - l + j) % j;
			default:
				return i;
		}
	}

	public Rotation getRotation(Direction direction) {
		Direction.Axis axis = direction.getAxis();
		return (this != LEFT_RIGHT || axis != Direction.Axis.Z) && (this != FRONT_BACK || axis != Direction.Axis.X) ? Rotation.NONE : Rotation.CLOCKWISE_180;
	}

	public Direction mirror(Direction direction) {
		if (this == FRONT_BACK && direction.getAxis() == Direction.Axis.X) {
			return direction.getOpposite();
		} else {
			return this == LEFT_RIGHT && direction.getAxis() == Direction.Axis.Z ? direction.getOpposite() : direction;
		}
	}

	public OctahedralGroup rotation() {
		return this.rotation;
	}
}
