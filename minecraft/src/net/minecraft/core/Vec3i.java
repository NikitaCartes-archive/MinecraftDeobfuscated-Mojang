package net.minecraft.core;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.util.Mth;

@Immutable
public class Vec3i implements Comparable<Vec3i> {
	public static final Codec<Vec3i> CODEC = Codec.INT_STREAM
		.comapFlatMap(
			intStream -> Util.fixedSize(intStream, 3).map(is -> new Vec3i(is[0], is[1], is[2])),
			vec3i -> IntStream.of(new int[]{vec3i.getX(), vec3i.getY(), vec3i.getZ()})
		);
	public static final Vec3i ZERO = new Vec3i(0, 0, 0);
	private int x;
	private int y;
	private int z;

	public static Codec<Vec3i> offsetCodec(int i) {
		return CODEC.validate(
			vec3i -> Math.abs(vec3i.getX()) < i && Math.abs(vec3i.getY()) < i && Math.abs(vec3i.getZ()) < i
					? DataResult.success(vec3i)
					: DataResult.error(() -> "Position out of range, expected at most " + i + ": " + vec3i)
		);
	}

	public Vec3i(int i, int j, int k) {
		this.x = i;
		this.y = j;
		this.z = k;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof Vec3i vec3i)) {
			return false;
		} else if (this.getX() != vec3i.getX()) {
			return false;
		} else {
			return this.getY() != vec3i.getY() ? false : this.getZ() == vec3i.getZ();
		}
	}

	public int hashCode() {
		return (this.getY() + this.getZ() * 31) * 31 + this.getX();
	}

	public int compareTo(Vec3i vec3i) {
		if (this.getY() == vec3i.getY()) {
			return this.getZ() == vec3i.getZ() ? this.getX() - vec3i.getX() : this.getZ() - vec3i.getZ();
		} else {
			return this.getY() - vec3i.getY();
		}
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getZ() {
		return this.z;
	}

	protected Vec3i setX(int i) {
		this.x = i;
		return this;
	}

	protected Vec3i setY(int i) {
		this.y = i;
		return this;
	}

	protected Vec3i setZ(int i) {
		this.z = i;
		return this;
	}

	public Vec3i offset(int i, int j, int k) {
		return i == 0 && j == 0 && k == 0 ? this : new Vec3i(this.getX() + i, this.getY() + j, this.getZ() + k);
	}

	public Vec3i offset(Vec3i vec3i) {
		return this.offset(vec3i.getX(), vec3i.getY(), vec3i.getZ());
	}

	public Vec3i subtract(Vec3i vec3i) {
		return this.offset(-vec3i.getX(), -vec3i.getY(), -vec3i.getZ());
	}

	public Vec3i multiply(int i) {
		if (i == 1) {
			return this;
		} else {
			return i == 0 ? ZERO : new Vec3i(this.getX() * i, this.getY() * i, this.getZ() * i);
		}
	}

	public Vec3i above() {
		return this.above(1);
	}

	public Vec3i above(int i) {
		return this.relative(Direction.UP, i);
	}

	public Vec3i below() {
		return this.below(1);
	}

	public Vec3i below(int i) {
		return this.relative(Direction.DOWN, i);
	}

	public Vec3i north() {
		return this.north(1);
	}

	public Vec3i north(int i) {
		return this.relative(Direction.NORTH, i);
	}

	public Vec3i south() {
		return this.south(1);
	}

	public Vec3i south(int i) {
		return this.relative(Direction.SOUTH, i);
	}

	public Vec3i west() {
		return this.west(1);
	}

	public Vec3i west(int i) {
		return this.relative(Direction.WEST, i);
	}

	public Vec3i east() {
		return this.east(1);
	}

	public Vec3i east(int i) {
		return this.relative(Direction.EAST, i);
	}

	public Vec3i relative(Direction direction) {
		return this.relative(direction, 1);
	}

	public Vec3i relative(Direction direction, int i) {
		return i == 0 ? this : new Vec3i(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
	}

	public Vec3i relative(Direction.Axis axis, int i) {
		if (i == 0) {
			return this;
		} else {
			int j = axis == Direction.Axis.X ? i : 0;
			int k = axis == Direction.Axis.Y ? i : 0;
			int l = axis == Direction.Axis.Z ? i : 0;
			return new Vec3i(this.getX() + j, this.getY() + k, this.getZ() + l);
		}
	}

	public Vec3i cross(Vec3i vec3i) {
		return new Vec3i(
			this.getY() * vec3i.getZ() - this.getZ() * vec3i.getY(),
			this.getZ() * vec3i.getX() - this.getX() * vec3i.getZ(),
			this.getX() * vec3i.getY() - this.getY() * vec3i.getX()
		);
	}

	public boolean closerThan(Vec3i vec3i, double d) {
		return this.distSqr(vec3i) < Mth.square(d);
	}

	public boolean closerToCenterThan(Position position, double d) {
		return this.distToCenterSqr(position) < Mth.square(d);
	}

	public double distSqr(Vec3i vec3i) {
		return this.distToLowCornerSqr((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ());
	}

	public double distToCenterSqr(Position position) {
		return this.distToCenterSqr(position.x(), position.y(), position.z());
	}

	public double distToCenterSqr(double d, double e, double f) {
		double g = (double)this.getX() + 0.5 - d;
		double h = (double)this.getY() + 0.5 - e;
		double i = (double)this.getZ() + 0.5 - f;
		return g * g + h * h + i * i;
	}

	public double distToLowCornerSqr(double d, double e, double f) {
		double g = (double)this.getX() - d;
		double h = (double)this.getY() - e;
		double i = (double)this.getZ() - f;
		return g * g + h * h + i * i;
	}

	public int distManhattan(Vec3i vec3i) {
		float f = (float)Math.abs(vec3i.getX() - this.getX());
		float g = (float)Math.abs(vec3i.getY() - this.getY());
		float h = (float)Math.abs(vec3i.getZ() - this.getZ());
		return (int)(f + g + h);
	}

	public int get(Direction.Axis axis) {
		return axis.choose(this.x, this.y, this.z);
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
	}

	public String toShortString() {
		return this.getX() + ", " + this.getY() + ", " + this.getZ();
	}
}
