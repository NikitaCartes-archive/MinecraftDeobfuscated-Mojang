package net.minecraft.world.phys;

import com.mojang.math.Vector3f;
import java.util.EnumSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

public class Vec3 implements Position {
	public static final Vec3 ZERO = new Vec3(0.0, 0.0, 0.0);
	public final double x;
	public final double y;
	public final double z;

	@Environment(EnvType.CLIENT)
	public static Vec3 fromRGB24(int i) {
		double d = (double)(i >> 16 & 0xFF) / 255.0;
		double e = (double)(i >> 8 & 0xFF) / 255.0;
		double f = (double)(i & 0xFF) / 255.0;
		return new Vec3(d, e, f);
	}

	public static Vec3 atCenterOf(Vec3i vec3i) {
		return new Vec3((double)vec3i.getX() + 0.5, (double)vec3i.getY() + 0.5, (double)vec3i.getZ() + 0.5);
	}

	public static Vec3 atLowerCornerOf(Vec3i vec3i) {
		return new Vec3((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ());
	}

	public static Vec3 atBottomCenterOf(Vec3i vec3i) {
		return new Vec3((double)vec3i.getX() + 0.5, (double)vec3i.getY(), (double)vec3i.getZ() + 0.5);
	}

	public static Vec3 upFromBottomCenterOf(Vec3i vec3i, double d) {
		return new Vec3((double)vec3i.getX() + 0.5, (double)vec3i.getY() + d, (double)vec3i.getZ() + 0.5);
	}

	public Vec3(double d, double e, double f) {
		this.x = d;
		this.y = e;
		this.z = f;
	}

	public Vec3(Vector3f vector3f) {
		this((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z());
	}

	public Vec3 vectorTo(Vec3 vec3) {
		return new Vec3(vec3.x - this.x, vec3.y - this.y, vec3.z - this.z);
	}

	public Vec3 normalize() {
		double d = (double)Mth.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
		return d < 1.0E-4 ? ZERO : new Vec3(this.x / d, this.y / d, this.z / d);
	}

	public double dot(Vec3 vec3) {
		return this.x * vec3.x + this.y * vec3.y + this.z * vec3.z;
	}

	public Vec3 cross(Vec3 vec3) {
		return new Vec3(this.y * vec3.z - this.z * vec3.y, this.z * vec3.x - this.x * vec3.z, this.x * vec3.y - this.y * vec3.x);
	}

	public Vec3 subtract(Vec3 vec3) {
		return this.subtract(vec3.x, vec3.y, vec3.z);
	}

	public Vec3 subtract(double d, double e, double f) {
		return this.add(-d, -e, -f);
	}

	public Vec3 add(Vec3 vec3) {
		return this.add(vec3.x, vec3.y, vec3.z);
	}

	public Vec3 add(double d, double e, double f) {
		return new Vec3(this.x + d, this.y + e, this.z + f);
	}

	public boolean closerThan(Position position, double d) {
		return this.distanceToSqr(position.x(), position.y(), position.z()) < d * d;
	}

	public double distanceTo(Vec3 vec3) {
		double d = vec3.x - this.x;
		double e = vec3.y - this.y;
		double f = vec3.z - this.z;
		return (double)Mth.sqrt(d * d + e * e + f * f);
	}

	public double distanceToSqr(Vec3 vec3) {
		double d = vec3.x - this.x;
		double e = vec3.y - this.y;
		double f = vec3.z - this.z;
		return d * d + e * e + f * f;
	}

	public double distanceToSqr(double d, double e, double f) {
		double g = d - this.x;
		double h = e - this.y;
		double i = f - this.z;
		return g * g + h * h + i * i;
	}

	public Vec3 scale(double d) {
		return this.multiply(d, d, d);
	}

	@Environment(EnvType.CLIENT)
	public Vec3 reverse() {
		return this.scale(-1.0);
	}

	public Vec3 multiply(Vec3 vec3) {
		return this.multiply(vec3.x, vec3.y, vec3.z);
	}

	public Vec3 multiply(double d, double e, double f) {
		return new Vec3(this.x * d, this.y * e, this.z * f);
	}

	public double length() {
		return (double)Mth.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public double lengthSqr() {
		return this.x * this.x + this.y * this.y + this.z * this.z;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof Vec3)) {
			return false;
		} else {
			Vec3 vec3 = (Vec3)object;
			if (Double.compare(vec3.x, this.x) != 0) {
				return false;
			} else {
				return Double.compare(vec3.y, this.y) != 0 ? false : Double.compare(vec3.z, this.z) == 0;
			}
		}
	}

	public int hashCode() {
		long l = Double.doubleToLongBits(this.x);
		int i = (int)(l ^ l >>> 32);
		l = Double.doubleToLongBits(this.y);
		i = 31 * i + (int)(l ^ l >>> 32);
		l = Double.doubleToLongBits(this.z);
		return 31 * i + (int)(l ^ l >>> 32);
	}

	public String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}

	public Vec3 xRot(float f) {
		float g = Mth.cos(f);
		float h = Mth.sin(f);
		double d = this.x;
		double e = this.y * (double)g + this.z * (double)h;
		double i = this.z * (double)g - this.y * (double)h;
		return new Vec3(d, e, i);
	}

	public Vec3 yRot(float f) {
		float g = Mth.cos(f);
		float h = Mth.sin(f);
		double d = this.x * (double)g + this.z * (double)h;
		double e = this.y;
		double i = this.z * (double)g - this.x * (double)h;
		return new Vec3(d, e, i);
	}

	@Environment(EnvType.CLIENT)
	public Vec3 zRot(float f) {
		float g = Mth.cos(f);
		float h = Mth.sin(f);
		double d = this.x * (double)g + this.y * (double)h;
		double e = this.y * (double)g - this.x * (double)h;
		double i = this.z;
		return new Vec3(d, e, i);
	}

	@Environment(EnvType.CLIENT)
	public static Vec3 directionFromRotation(Vec2 vec2) {
		return directionFromRotation(vec2.x, vec2.y);
	}

	@Environment(EnvType.CLIENT)
	public static Vec3 directionFromRotation(float f, float g) {
		float h = Mth.cos(-g * (float) (Math.PI / 180.0) - (float) Math.PI);
		float i = Mth.sin(-g * (float) (Math.PI / 180.0) - (float) Math.PI);
		float j = -Mth.cos(-f * (float) (Math.PI / 180.0));
		float k = Mth.sin(-f * (float) (Math.PI / 180.0));
		return new Vec3((double)(i * j), (double)k, (double)(h * j));
	}

	public Vec3 align(EnumSet<Direction.Axis> enumSet) {
		double d = enumSet.contains(Direction.Axis.X) ? (double)Mth.floor(this.x) : this.x;
		double e = enumSet.contains(Direction.Axis.Y) ? (double)Mth.floor(this.y) : this.y;
		double f = enumSet.contains(Direction.Axis.Z) ? (double)Mth.floor(this.z) : this.z;
		return new Vec3(d, e, f);
	}

	public double get(Direction.Axis axis) {
		return axis.choose(this.x, this.y, this.z);
	}

	@Override
	public final double x() {
		return this.x;
	}

	@Override
	public final double y() {
		return this.y;
	}

	@Override
	public final double z() {
		return this.z;
	}
}
