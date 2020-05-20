package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;

public enum Direction implements StringRepresentable {
	DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new Vec3i(0, -1, 0)),
	UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new Vec3i(0, 1, 0)),
	NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new Vec3i(0, 0, -1)),
	SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new Vec3i(0, 0, 1)),
	WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new Vec3i(-1, 0, 0)),
	EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new Vec3i(1, 0, 0));

	private final int data3d;
	private final int oppositeIndex;
	private final int data2d;
	private final String name;
	private final Direction.Axis axis;
	private final Direction.AxisDirection axisDirection;
	private final Vec3i normal;
	private static final Direction[] VALUES = values();
	private static final Map<String, Direction> BY_NAME = (Map<String, Direction>)Arrays.stream(VALUES)
		.collect(Collectors.toMap(Direction::getName, direction -> direction));
	private static final Direction[] BY_3D_DATA = (Direction[])Arrays.stream(VALUES)
		.sorted(Comparator.comparingInt(direction -> direction.data3d))
		.toArray(Direction[]::new);
	private static final Direction[] BY_2D_DATA = (Direction[])Arrays.stream(VALUES)
		.filter(direction -> direction.getAxis().isHorizontal())
		.sorted(Comparator.comparingInt(direction -> direction.data2d))
		.toArray(Direction[]::new);
	private static final Long2ObjectMap<Direction> BY_NORMAL = (Long2ObjectMap<Direction>)Arrays.stream(VALUES)
		.collect(Collectors.toMap(direction -> new BlockPos(direction.getNormal()).asLong(), direction -> direction, (direction, direction2) -> {
			throw new IllegalArgumentException("Duplicate keys");
		}, Long2ObjectOpenHashMap::new));

	private Direction(int j, int k, int l, String string2, Direction.AxisDirection axisDirection, Direction.Axis axis, Vec3i vec3i) {
		this.data3d = j;
		this.data2d = l;
		this.oppositeIndex = k;
		this.name = string2;
		this.axis = axis;
		this.axisDirection = axisDirection;
		this.normal = vec3i;
	}

	public static Direction[] orderedByNearest(Entity entity) {
		float f = entity.getViewXRot(1.0F) * (float) (Math.PI / 180.0);
		float g = -entity.getViewYRot(1.0F) * (float) (Math.PI / 180.0);
		float h = Mth.sin(f);
		float i = Mth.cos(f);
		float j = Mth.sin(g);
		float k = Mth.cos(g);
		boolean bl = j > 0.0F;
		boolean bl2 = h < 0.0F;
		boolean bl3 = k > 0.0F;
		float l = bl ? j : -j;
		float m = bl2 ? -h : h;
		float n = bl3 ? k : -k;
		float o = l * i;
		float p = n * i;
		Direction direction = bl ? EAST : WEST;
		Direction direction2 = bl2 ? UP : DOWN;
		Direction direction3 = bl3 ? SOUTH : NORTH;
		if (l > n) {
			if (m > o) {
				return makeDirectionArray(direction2, direction, direction3);
			} else {
				return p > m ? makeDirectionArray(direction, direction3, direction2) : makeDirectionArray(direction, direction2, direction3);
			}
		} else if (m > p) {
			return makeDirectionArray(direction2, direction3, direction);
		} else {
			return o > m ? makeDirectionArray(direction3, direction, direction2) : makeDirectionArray(direction3, direction2, direction);
		}
	}

	private static Direction[] makeDirectionArray(Direction direction, Direction direction2, Direction direction3) {
		return new Direction[]{direction, direction2, direction3, direction3.getOpposite(), direction2.getOpposite(), direction.getOpposite()};
	}

	@Environment(EnvType.CLIENT)
	public static Direction rotate(Matrix4f matrix4f, Direction direction) {
		Vec3i vec3i = direction.getNormal();
		Vector4f vector4f = new Vector4f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), 0.0F);
		vector4f.transform(matrix4f);
		return getNearest(vector4f.x(), vector4f.y(), vector4f.z());
	}

	@Environment(EnvType.CLIENT)
	public Quaternion getRotation() {
		Quaternion quaternion = Vector3f.XP.rotationDegrees(90.0F);
		switch (this) {
			case DOWN:
				return Vector3f.XP.rotationDegrees(180.0F);
			case UP:
				return Quaternion.ONE.copy();
			case NORTH:
				quaternion.mul(Vector3f.ZP.rotationDegrees(180.0F));
				return quaternion;
			case SOUTH:
				return quaternion;
			case WEST:
				quaternion.mul(Vector3f.ZP.rotationDegrees(90.0F));
				return quaternion;
			case EAST:
			default:
				quaternion.mul(Vector3f.ZP.rotationDegrees(-90.0F));
				return quaternion;
		}
	}

	public int get3DDataValue() {
		return this.data3d;
	}

	public int get2DDataValue() {
		return this.data2d;
	}

	public Direction.AxisDirection getAxisDirection() {
		return this.axisDirection;
	}

	public Direction getOpposite() {
		return from3DDataValue(this.oppositeIndex);
	}

	public Direction getClockWise() {
		switch (this) {
			case NORTH:
				return EAST;
			case SOUTH:
				return WEST;
			case WEST:
				return NORTH;
			case EAST:
				return SOUTH;
			default:
				throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
		}
	}

	public Direction getCounterClockWise() {
		switch (this) {
			case NORTH:
				return WEST;
			case SOUTH:
				return EAST;
			case WEST:
				return SOUTH;
			case EAST:
				return NORTH;
			default:
				throw new IllegalStateException("Unable to get CCW facing of " + this);
		}
	}

	public int getStepX() {
		return this.normal.getX();
	}

	public int getStepY() {
		return this.normal.getY();
	}

	public int getStepZ() {
		return this.normal.getZ();
	}

	@Environment(EnvType.CLIENT)
	public Vector3f step() {
		return new Vector3f((float)this.getStepX(), (float)this.getStepY(), (float)this.getStepZ());
	}

	public String getName() {
		return this.name;
	}

	public Direction.Axis getAxis() {
		return this.axis;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public static Direction byName(@Nullable String string) {
		return string == null ? null : (Direction)BY_NAME.get(string.toLowerCase(Locale.ROOT));
	}

	public static Direction from3DDataValue(int i) {
		return BY_3D_DATA[Mth.abs(i % BY_3D_DATA.length)];
	}

	public static Direction from2DDataValue(int i) {
		return BY_2D_DATA[Mth.abs(i % BY_2D_DATA.length)];
	}

	@Nullable
	public static Direction fromNormal(int i, int j, int k) {
		return BY_NORMAL.get(BlockPos.asLong(i, j, k));
	}

	public static Direction fromYRot(double d) {
		return from2DDataValue(Mth.floor(d / 90.0 + 0.5) & 3);
	}

	public static Direction fromAxisAndDirection(Direction.Axis axis, Direction.AxisDirection axisDirection) {
		switch (axis) {
			case X:
				return axisDirection == Direction.AxisDirection.POSITIVE ? EAST : WEST;
			case Y:
				return axisDirection == Direction.AxisDirection.POSITIVE ? UP : DOWN;
			case Z:
			default:
				return axisDirection == Direction.AxisDirection.POSITIVE ? SOUTH : NORTH;
		}
	}

	public float toYRot() {
		return (float)((this.data2d & 3) * 90);
	}

	public static Direction getRandom(Random random) {
		return Util.getRandom(VALUES, random);
	}

	public static Direction getNearest(double d, double e, double f) {
		return getNearest((float)d, (float)e, (float)f);
	}

	public static Direction getNearest(float f, float g, float h) {
		Direction direction = NORTH;
		float i = Float.MIN_VALUE;

		for (Direction direction2 : VALUES) {
			float j = f * (float)direction2.normal.getX() + g * (float)direction2.normal.getY() + h * (float)direction2.normal.getZ();
			if (j > i) {
				i = j;
				direction = direction2;
			}
		}

		return direction;
	}

	public String toString() {
		return this.name;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public static Direction get(Direction.AxisDirection axisDirection, Direction.Axis axis) {
		for (Direction direction : VALUES) {
			if (direction.getAxisDirection() == axisDirection && direction.getAxis() == axis) {
				return direction;
			}
		}

		throw new IllegalArgumentException("No such direction: " + axisDirection + " " + axis);
	}

	public Vec3i getNormal() {
		return this.normal;
	}

	public static enum Axis implements StringRepresentable, Predicate<Direction> {
		X("x") {
			@Override
			public int choose(int i, int j, int k) {
				return i;
			}

			@Override
			public double choose(double d, double e, double f) {
				return d;
			}
		},
		Y("y") {
			@Override
			public int choose(int i, int j, int k) {
				return j;
			}

			@Override
			public double choose(double d, double e, double f) {
				return e;
			}
		},
		Z("z") {
			@Override
			public int choose(int i, int j, int k) {
				return k;
			}

			@Override
			public double choose(double d, double e, double f) {
				return f;
			}
		};

		private static final Direction.Axis[] VALUES = values();
		public static final Codec<Direction.Axis> CODEC = StringRepresentable.fromEnum(Direction.Axis::values, Direction.Axis::byName);
		private static final Map<String, Direction.Axis> BY_NAME = (Map<String, Direction.Axis>)Arrays.stream(VALUES)
			.collect(Collectors.toMap(Direction.Axis::getName, axis -> axis));
		private final String name;

		private Axis(String string2) {
			this.name = string2;
		}

		@Nullable
		public static Direction.Axis byName(String string) {
			return (Direction.Axis)BY_NAME.get(string.toLowerCase(Locale.ROOT));
		}

		public String getName() {
			return this.name;
		}

		public boolean isVertical() {
			return this == Y;
		}

		public boolean isHorizontal() {
			return this == X || this == Z;
		}

		public String toString() {
			return this.name;
		}

		public static Direction.Axis getRandom(Random random) {
			return Util.getRandom(VALUES, random);
		}

		public boolean test(@Nullable Direction direction) {
			return direction != null && direction.getAxis() == this;
		}

		public Direction.Plane getPlane() {
			switch (this) {
				case X:
				case Z:
					return Direction.Plane.HORIZONTAL;
				case Y:
					return Direction.Plane.VERTICAL;
				default:
					throw new Error("Someone's been tampering with the universe!");
			}
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public abstract int choose(int i, int j, int k);

		public abstract double choose(double d, double e, double f);
	}

	public static enum AxisDirection {
		POSITIVE(1, "Towards positive"),
		NEGATIVE(-1, "Towards negative");

		private final int step;
		private final String name;

		private AxisDirection(int j, String string2) {
			this.step = j;
			this.name = string2;
		}

		public int getStep() {
			return this.step;
		}

		public String toString() {
			return this.name;
		}

		public Direction.AxisDirection opposite() {
			return this == POSITIVE ? NEGATIVE : POSITIVE;
		}
	}

	public static enum Plane implements Iterable<Direction>, Predicate<Direction> {
		HORIZONTAL(new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}, new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}),
		VERTICAL(new Direction[]{Direction.UP, Direction.DOWN}, new Direction.Axis[]{Direction.Axis.Y});

		private final Direction[] faces;
		private final Direction.Axis[] axis;

		private Plane(Direction[] directions, Direction.Axis[] axiss) {
			this.faces = directions;
			this.axis = axiss;
		}

		public Direction getRandomDirection(Random random) {
			return Util.getRandom(this.faces, random);
		}

		public boolean test(@Nullable Direction direction) {
			return direction != null && direction.getAxis().getPlane() == this;
		}

		public Iterator<Direction> iterator() {
			return Iterators.forArray(this.faces);
		}
	}
}
