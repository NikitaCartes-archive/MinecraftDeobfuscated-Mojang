package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public enum Direction implements StringRepresentable {
	DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new Vec3i(0, -1, 0)),
	UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new Vec3i(0, 1, 0)),
	NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new Vec3i(0, 0, -1)),
	SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new Vec3i(0, 0, 1)),
	WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new Vec3i(-1, 0, 0)),
	EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new Vec3i(1, 0, 0));

	public static final StringRepresentable.EnumCodec<Direction> CODEC = StringRepresentable.fromEnum(Direction::values);
	public static final Codec<Direction> VERTICAL_CODEC = CODEC.flatXmap(Direction::verifyVertical, Direction::verifyVertical);
	private final int data3d;
	private final int oppositeIndex;
	private final int data2d;
	private final String name;
	private final Direction.Axis axis;
	private final Direction.AxisDirection axisDirection;
	private final Vec3i normal;
	private static final Direction[] VALUES = values();
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

	public static Direction rotate(Matrix4f matrix4f, Direction direction) {
		Vec3i vec3i = direction.getNormal();
		Vector4f vector4f = matrix4f.transform(new Vector4f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), 0.0F));
		return getNearest(vector4f.x(), vector4f.y(), vector4f.z());
	}

	public static Collection<Direction> allShuffled(RandomSource randomSource) {
		return Util.<Direction>shuffledCopy(values(), randomSource);
	}

	public static Stream<Direction> stream() {
		return Stream.of(VALUES);
	}

	public Quaternionf getRotation() {
		return switch (this) {
			case DOWN -> new Quaternionf().rotationX((float) Math.PI);
			case UP -> new Quaternionf();
			case NORTH -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) Math.PI);
			case SOUTH -> new Quaternionf().rotationX((float) (Math.PI / 2));
			case WEST -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) (Math.PI / 2));
			case EAST -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) (-Math.PI / 2));
		};
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

	public static Direction getFacingAxis(Entity entity, Direction.Axis axis) {
		return switch (axis) {
			case X -> EAST.isFacingAngle(entity.getViewYRot(1.0F)) ? EAST : WEST;
			case Z -> SOUTH.isFacingAngle(entity.getViewYRot(1.0F)) ? SOUTH : NORTH;
			case Y -> entity.getViewXRot(1.0F) < 0.0F ? UP : DOWN;
		};
	}

	public Direction getOpposite() {
		return from3DDataValue(this.oppositeIndex);
	}

	public Direction getClockWise(Direction.Axis axis) {
		return switch (axis) {
			case X -> this != WEST && this != EAST ? this.getClockWiseX() : this;
			case Z -> this != NORTH && this != SOUTH ? this.getClockWiseZ() : this;
			case Y -> this != UP && this != DOWN ? this.getClockWise() : this;
		};
	}

	public Direction getCounterClockWise(Direction.Axis axis) {
		return switch (axis) {
			case X -> this != WEST && this != EAST ? this.getCounterClockWiseX() : this;
			case Z -> this != NORTH && this != SOUTH ? this.getCounterClockWiseZ() : this;
			case Y -> this != UP && this != DOWN ? this.getCounterClockWise() : this;
		};
	}

	public Direction getClockWise() {
		return switch (this) {
			case NORTH -> EAST;
			case SOUTH -> WEST;
			case WEST -> NORTH;
			case EAST -> SOUTH;
			default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
		};
	}

	private Direction getClockWiseX() {
		return switch (this) {
			case DOWN -> SOUTH;
			case UP -> NORTH;
			case NORTH -> DOWN;
			case SOUTH -> UP;
			default -> throw new IllegalStateException("Unable to get X-rotated facing of " + this);
		};
	}

	private Direction getCounterClockWiseX() {
		return switch (this) {
			case DOWN -> NORTH;
			case UP -> SOUTH;
			case NORTH -> UP;
			case SOUTH -> DOWN;
			default -> throw new IllegalStateException("Unable to get X-rotated facing of " + this);
		};
	}

	private Direction getClockWiseZ() {
		return switch (this) {
			case DOWN -> WEST;
			case UP -> EAST;
			default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
			case WEST -> UP;
			case EAST -> DOWN;
		};
	}

	private Direction getCounterClockWiseZ() {
		return switch (this) {
			case DOWN -> EAST;
			case UP -> WEST;
			default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
			case WEST -> DOWN;
			case EAST -> UP;
		};
	}

	public Direction getCounterClockWise() {
		return switch (this) {
			case NORTH -> WEST;
			case SOUTH -> EAST;
			case WEST -> SOUTH;
			case EAST -> NORTH;
			default -> throw new IllegalStateException("Unable to get CCW facing of " + this);
		};
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
	public static Direction byName(@Nullable String string) {
		return (Direction)CODEC.byName(string);
	}

	public static Direction from3DDataValue(int i) {
		return BY_3D_DATA[Mth.abs(i % BY_3D_DATA.length)];
	}

	public static Direction from2DDataValue(int i) {
		return BY_2D_DATA[Mth.abs(i % BY_2D_DATA.length)];
	}

	@Nullable
	public static Direction fromNormal(BlockPos blockPos) {
		return BY_NORMAL.get(blockPos.asLong());
	}

	@Nullable
	public static Direction fromNormal(int i, int j, int k) {
		return BY_NORMAL.get(BlockPos.asLong(i, j, k));
	}

	public static Direction fromYRot(double d) {
		return from2DDataValue(Mth.floor(d / 90.0 + 0.5) & 3);
	}

	public static Direction fromAxisAndDirection(Direction.Axis axis, Direction.AxisDirection axisDirection) {
		return switch (axis) {
			case X -> axisDirection == Direction.AxisDirection.POSITIVE ? EAST : WEST;
			case Z -> axisDirection == Direction.AxisDirection.POSITIVE ? SOUTH : NORTH;
			case Y -> axisDirection == Direction.AxisDirection.POSITIVE ? UP : DOWN;
		};
	}

	public float toYRot() {
		return (float)((this.data2d & 3) * 90);
	}

	public static Direction getRandom(RandomSource randomSource) {
		return Util.getRandom(VALUES, randomSource);
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

	private static DataResult<Direction> verifyVertical(Direction direction) {
		return direction.getAxis().isVertical() ? DataResult.success(direction) : DataResult.error("Expected a vertical direction");
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

	public boolean isFacingAngle(float f) {
		float g = f * (float) (Math.PI / 180.0);
		float h = -Mth.sin(g);
		float i = Mth.cos(g);
		return (float)this.normal.getX() * h + (float)this.normal.getZ() * i > 0.0F;
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

		public static final Direction.Axis[] VALUES = values();
		public static final StringRepresentable.EnumCodec<Direction.Axis> CODEC = StringRepresentable.fromEnum(Direction.Axis::values);
		private final String name;

		Axis(String string2) {
			this.name = string2;
		}

		@Nullable
		public static Direction.Axis byName(String string) {
			return (Direction.Axis)CODEC.byName(string);
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

		public static Direction.Axis getRandom(RandomSource randomSource) {
			return Util.getRandom(VALUES, randomSource);
		}

		public boolean test(@Nullable Direction direction) {
			return direction != null && direction.getAxis() == this;
		}

		public Direction.Plane getPlane() {
			return switch (this) {
				case X, Z -> Direction.Plane.HORIZONTAL;
				case Y -> Direction.Plane.VERTICAL;
			};
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

		public String getName() {
			return this.name;
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

		public Direction getRandomDirection(RandomSource randomSource) {
			return Util.getRandom(this.faces, randomSource);
		}

		public Direction.Axis getRandomAxis(RandomSource randomSource) {
			return Util.getRandom(this.axis, randomSource);
		}

		public boolean test(@Nullable Direction direction) {
			return direction != null && direction.getAxis().getPlane() == this;
		}

		public Iterator<Direction> iterator() {
			return Iterators.forArray(this.faces);
		}

		public Stream<Direction> stream() {
			return Arrays.stream(this.faces);
		}

		public List<Direction> shuffledCopy(RandomSource randomSource) {
			return Util.shuffledCopy(this.faces, randomSource);
		}
	}
}
