package net.minecraft.core;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

	public Vec3i(int i, int j, int k) {
		this.x = i;
		this.y = j;
		this.z = k;
	}

	public Vec3i(double d, double e, double f) {
		this(Mth.floor(d), Mth.floor(e), Mth.floor(f));
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof Vec3i)) {
			return false;
		} else {
			Vec3i vec3i = (Vec3i)object;
			if (this.getX() != vec3i.getX()) {
				return false;
			} else {
				return this.getY() != vec3i.getY() ? false : this.getZ() == vec3i.getZ();
			}
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

	protected void setX(int i) {
		this.x = i;
	}

	protected void setY(int i) {
		this.y = i;
	}

	protected void setZ(int i) {
		this.z = i;
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

	public Vec3i relative(Direction direction, int i) {
		return i == 0 ? this : new Vec3i(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
	}

	public Vec3i cross(Vec3i vec3i) {
		return new Vec3i(
			this.getY() * vec3i.getZ() - this.getZ() * vec3i.getY(),
			this.getZ() * vec3i.getX() - this.getX() * vec3i.getZ(),
			this.getX() * vec3i.getY() - this.getY() * vec3i.getX()
		);
	}

	public boolean closerThan(Vec3i vec3i, double d) {
		return this.distSqr((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ(), false) < d * d;
	}

	public boolean closerThan(Position position, double d) {
		return this.distSqr(position.x(), position.y(), position.z(), true) < d * d;
	}

	public double distSqr(Vec3i vec3i) {
		return this.distSqr((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ(), true);
	}

	public double distSqr(Position position, boolean bl) {
		return this.distSqr(position.x(), position.y(), position.z(), bl);
	}

	public double distSqr(double d, double e, double f, boolean bl) {
		double g = bl ? 0.5 : 0.0;
		double h = (double)this.getX() + g - d;
		double i = (double)this.getY() + g - e;
		double j = (double)this.getZ() + g - f;
		return h * h + i * i + j * j;
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

	@Environment(EnvType.CLIENT)
	public String toShortString() {
		return "" + this.getX() + ", " + this.getY() + ", " + this.getZ();
	}
}
