package net.minecraft.world.level.redstone;

import com.google.common.annotations.VisibleForTesting;
import io.netty.buffer.ByteBuf;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

public class Orientation {
	public static final StreamCodec<ByteBuf, Orientation> STREAM_CODEC = ByteBufCodecs.idMapper(Orientation::fromIndex, Orientation::getIndex);
	private static final Orientation[] ORIENTATIONS = Util.make(() -> {
		Orientation[] orientations = new Orientation[48];
		generateContext(new Orientation(Direction.UP, Direction.NORTH, Orientation.SideBias.LEFT), orientations);
		return orientations;
	});
	private final Direction up;
	private final Direction front;
	private final Direction side;
	private final Orientation.SideBias sideBias;
	private final int index;
	private final List<Direction> neighbors;
	private final List<Direction> horizontalNeighbors;
	private final List<Direction> verticalNeighbors;
	private final Map<Direction, Orientation> withFront = new EnumMap(Direction.class);
	private final Map<Direction, Orientation> withUp = new EnumMap(Direction.class);
	private final Map<Orientation.SideBias, Orientation> withSideBias = new EnumMap(Orientation.SideBias.class);

	private Orientation(Direction direction, Direction direction2, Orientation.SideBias sideBias) {
		this.up = direction;
		this.front = direction2;
		this.sideBias = sideBias;
		this.index = generateIndex(direction, direction2, sideBias);
		Vec3i vec3i = direction2.getUnitVec3i().cross(direction.getUnitVec3i());
		Direction direction3 = Direction.getNearest(vec3i, null);
		Objects.requireNonNull(direction3);
		if (this.sideBias == Orientation.SideBias.RIGHT) {
			this.side = direction3;
		} else {
			this.side = direction3.getOpposite();
		}

		this.neighbors = List.of(this.front.getOpposite(), this.front, this.side, this.side.getOpposite(), this.up.getOpposite(), this.up);
		this.horizontalNeighbors = this.neighbors.stream().filter(directionx -> directionx.getAxis() != this.up.getAxis()).toList();
		this.verticalNeighbors = this.neighbors.stream().filter(directionx -> directionx.getAxis() == this.up.getAxis()).toList();
	}

	public static Orientation of(Direction direction, Direction direction2, Orientation.SideBias sideBias) {
		return ORIENTATIONS[generateIndex(direction, direction2, sideBias)];
	}

	public Orientation withUp(Direction direction) {
		return (Orientation)this.withUp.get(direction);
	}

	public Orientation withFront(Direction direction) {
		return (Orientation)this.withFront.get(direction);
	}

	public Orientation withFrontPreserveUp(Direction direction) {
		return direction.getAxis() == this.up.getAxis() ? this : (Orientation)this.withFront.get(direction);
	}

	public Orientation withFrontAdjustSideBias(Direction direction) {
		Orientation orientation = this.withFront(direction);
		return this.front == orientation.side ? orientation.withMirror() : orientation;
	}

	public Orientation withSideBias(Orientation.SideBias sideBias) {
		return (Orientation)this.withSideBias.get(sideBias);
	}

	public Orientation withMirror() {
		return this.withSideBias(this.sideBias.getOpposite());
	}

	public Direction getFront() {
		return this.front;
	}

	public Direction getUp() {
		return this.up;
	}

	public Direction getSide() {
		return this.side;
	}

	public Orientation.SideBias getSideBias() {
		return this.sideBias;
	}

	public List<Direction> getDirections() {
		return this.neighbors;
	}

	public List<Direction> getHorizontalDirections() {
		return this.horizontalNeighbors;
	}

	public List<Direction> getVerticalDirections() {
		return this.verticalNeighbors;
	}

	public String toString() {
		return "[up=" + this.up + ",front=" + this.front + ",sideBias=" + this.sideBias + "]";
	}

	public int getIndex() {
		return this.index;
	}

	public static Orientation fromIndex(int i) {
		return ORIENTATIONS[i];
	}

	public static Orientation random(RandomSource randomSource) {
		return Util.getRandom(ORIENTATIONS, randomSource);
	}

	private static Orientation generateContext(Orientation orientation, Orientation[] orientations) {
		if (orientations[orientation.getIndex()] != null) {
			return orientations[orientation.getIndex()];
		} else {
			orientations[orientation.getIndex()] = orientation;

			for (Orientation.SideBias sideBias : Orientation.SideBias.values()) {
				orientation.withSideBias.put(sideBias, generateContext(new Orientation(orientation.up, orientation.front, sideBias), orientations));
			}

			for (Direction direction : Direction.values()) {
				Direction direction2 = orientation.up;
				if (direction == orientation.up) {
					direction2 = orientation.front.getOpposite();
				}

				if (direction == orientation.up.getOpposite()) {
					direction2 = orientation.front;
				}

				orientation.withFront.put(direction, generateContext(new Orientation(direction2, direction, orientation.sideBias), orientations));
			}

			for (Direction direction : Direction.values()) {
				Direction direction2x = orientation.front;
				if (direction == orientation.front) {
					direction2x = orientation.up.getOpposite();
				}

				if (direction == orientation.front.getOpposite()) {
					direction2x = orientation.up;
				}

				orientation.withUp.put(direction, generateContext(new Orientation(direction, direction2x, orientation.sideBias), orientations));
			}

			return orientation;
		}
	}

	@VisibleForTesting
	protected static int generateIndex(Direction direction, Direction direction2, Orientation.SideBias sideBias) {
		if (direction.getAxis() == direction2.getAxis()) {
			throw new IllegalStateException("Up-vector and front-vector can not be on the same axis");
		} else {
			int i;
			if (direction.getAxis() == Direction.Axis.Y) {
				i = direction2.getAxis() == Direction.Axis.X ? 1 : 0;
			} else {
				i = direction2.getAxis() == Direction.Axis.Y ? 1 : 0;
			}

			int j = i << 1 | direction2.getAxisDirection().ordinal();
			return ((direction.ordinal() << 2) + j << 1) + sideBias.ordinal();
		}
	}

	public static enum SideBias {
		LEFT("left"),
		RIGHT("right");

		private final String name;

		private SideBias(final String string2) {
			this.name = string2;
		}

		public Orientation.SideBias getOpposite() {
			return this == LEFT ? RIGHT : LEFT;
		}

		public String toString() {
			return this.name;
		}
	}
}
