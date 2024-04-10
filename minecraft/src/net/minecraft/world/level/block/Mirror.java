package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum Mirror implements StringRepresentable {
	NONE("none", OctahedralGroup.IDENTITY),
	LEFT_RIGHT("left_right", OctahedralGroup.INVERT_Z),
	FRONT_BACK("front_back", OctahedralGroup.INVERT_X);

	public static final Codec<Mirror> CODEC = StringRepresentable.fromEnum(Mirror::values);
	private final String id;
	private final Component symbol;
	private final OctahedralGroup rotation;

	private Mirror(final String string2, final OctahedralGroup octahedralGroup) {
		this.id = string2;
		this.symbol = Component.translatable("mirror." + string2);
		this.rotation = octahedralGroup;
	}

	public int mirror(int i, int j) {
		int k = j / 2;
		int l = i > k ? i - j : i;
		switch (this) {
			case LEFT_RIGHT:
				return (k - l + j) % j;
			case FRONT_BACK:
				return (j - l) % j;
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

	public Component symbol() {
		return this.symbol;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}
}
