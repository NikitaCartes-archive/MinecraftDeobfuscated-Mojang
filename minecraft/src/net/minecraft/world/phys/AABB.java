package net.minecraft.world.phys;

import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class AABB {
	public final double minX;
	public final double minY;
	public final double minZ;
	public final double maxX;
	public final double maxY;
	public final double maxZ;

	public AABB(double d, double e, double f, double g, double h, double i) {
		this.minX = Math.min(d, g);
		this.minY = Math.min(e, h);
		this.minZ = Math.min(f, i);
		this.maxX = Math.max(d, g);
		this.maxY = Math.max(e, h);
		this.maxZ = Math.max(f, i);
	}

	public AABB(BlockPos blockPos) {
		this(
			(double)blockPos.getX(),
			(double)blockPos.getY(),
			(double)blockPos.getZ(),
			(double)(blockPos.getX() + 1),
			(double)(blockPos.getY() + 1),
			(double)(blockPos.getZ() + 1)
		);
	}

	public AABB(BlockPos blockPos, BlockPos blockPos2) {
		this((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ());
	}

	public AABB(Vec3 vec3, Vec3 vec32) {
		this(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z);
	}

	public static AABB of(BoundingBox boundingBox) {
		return new AABB(
			(double)boundingBox.x0,
			(double)boundingBox.y0,
			(double)boundingBox.z0,
			(double)(boundingBox.x1 + 1),
			(double)(boundingBox.y1 + 1),
			(double)(boundingBox.z1 + 1)
		);
	}

	public static AABB unitCubeFromLowerCorner(Vec3 vec3) {
		return new AABB(vec3.x, vec3.y, vec3.z, vec3.x + 1.0, vec3.y + 1.0, vec3.z + 1.0);
	}

	public double min(Direction.Axis axis) {
		return axis.choose(this.minX, this.minY, this.minZ);
	}

	public double max(Direction.Axis axis) {
		return axis.choose(this.maxX, this.maxY, this.maxZ);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof AABB)) {
			return false;
		} else {
			AABB aABB = (AABB)object;
			if (Double.compare(aABB.minX, this.minX) != 0) {
				return false;
			} else if (Double.compare(aABB.minY, this.minY) != 0) {
				return false;
			} else if (Double.compare(aABB.minZ, this.minZ) != 0) {
				return false;
			} else if (Double.compare(aABB.maxX, this.maxX) != 0) {
				return false;
			} else {
				return Double.compare(aABB.maxY, this.maxY) != 0 ? false : Double.compare(aABB.maxZ, this.maxZ) == 0;
			}
		}
	}

	public int hashCode() {
		long l = Double.doubleToLongBits(this.minX);
		int i = (int)(l ^ l >>> 32);
		l = Double.doubleToLongBits(this.minY);
		i = 31 * i + (int)(l ^ l >>> 32);
		l = Double.doubleToLongBits(this.minZ);
		i = 31 * i + (int)(l ^ l >>> 32);
		l = Double.doubleToLongBits(this.maxX);
		i = 31 * i + (int)(l ^ l >>> 32);
		l = Double.doubleToLongBits(this.maxY);
		i = 31 * i + (int)(l ^ l >>> 32);
		l = Double.doubleToLongBits(this.maxZ);
		return 31 * i + (int)(l ^ l >>> 32);
	}

	public AABB contract(double d, double e, double f) {
		double g = this.minX;
		double h = this.minY;
		double i = this.minZ;
		double j = this.maxX;
		double k = this.maxY;
		double l = this.maxZ;
		if (d < 0.0) {
			g -= d;
		} else if (d > 0.0) {
			j -= d;
		}

		if (e < 0.0) {
			h -= e;
		} else if (e > 0.0) {
			k -= e;
		}

		if (f < 0.0) {
			i -= f;
		} else if (f > 0.0) {
			l -= f;
		}

		return new AABB(g, h, i, j, k, l);
	}

	public AABB expandTowards(Vec3 vec3) {
		return this.expandTowards(vec3.x, vec3.y, vec3.z);
	}

	public AABB expandTowards(double d, double e, double f) {
		double g = this.minX;
		double h = this.minY;
		double i = this.minZ;
		double j = this.maxX;
		double k = this.maxY;
		double l = this.maxZ;
		if (d < 0.0) {
			g += d;
		} else if (d > 0.0) {
			j += d;
		}

		if (e < 0.0) {
			h += e;
		} else if (e > 0.0) {
			k += e;
		}

		if (f < 0.0) {
			i += f;
		} else if (f > 0.0) {
			l += f;
		}

		return new AABB(g, h, i, j, k, l);
	}

	public AABB inflate(double d, double e, double f) {
		double g = this.minX - d;
		double h = this.minY - e;
		double i = this.minZ - f;
		double j = this.maxX + d;
		double k = this.maxY + e;
		double l = this.maxZ + f;
		return new AABB(g, h, i, j, k, l);
	}

	public AABB inflate(double d) {
		return this.inflate(d, d, d);
	}

	public AABB intersect(AABB aABB) {
		double d = Math.max(this.minX, aABB.minX);
		double e = Math.max(this.minY, aABB.minY);
		double f = Math.max(this.minZ, aABB.minZ);
		double g = Math.min(this.maxX, aABB.maxX);
		double h = Math.min(this.maxY, aABB.maxY);
		double i = Math.min(this.maxZ, aABB.maxZ);
		return new AABB(d, e, f, g, h, i);
	}

	public AABB minmax(AABB aABB) {
		double d = Math.min(this.minX, aABB.minX);
		double e = Math.min(this.minY, aABB.minY);
		double f = Math.min(this.minZ, aABB.minZ);
		double g = Math.max(this.maxX, aABB.maxX);
		double h = Math.max(this.maxY, aABB.maxY);
		double i = Math.max(this.maxZ, aABB.maxZ);
		return new AABB(d, e, f, g, h, i);
	}

	public AABB move(double d, double e, double f) {
		return new AABB(this.minX + d, this.minY + e, this.minZ + f, this.maxX + d, this.maxY + e, this.maxZ + f);
	}

	public AABB move(BlockPos blockPos) {
		return new AABB(
			this.minX + (double)blockPos.getX(),
			this.minY + (double)blockPos.getY(),
			this.minZ + (double)blockPos.getZ(),
			this.maxX + (double)blockPos.getX(),
			this.maxY + (double)blockPos.getY(),
			this.maxZ + (double)blockPos.getZ()
		);
	}

	public AABB move(Vec3 vec3) {
		return this.move(vec3.x, vec3.y, vec3.z);
	}

	public boolean intersects(AABB aABB) {
		return this.intersects(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
	}

	public boolean intersects(double d, double e, double f, double g, double h, double i) {
		return this.minX < g && this.maxX > d && this.minY < h && this.maxY > e && this.minZ < i && this.maxZ > f;
	}

	@Environment(EnvType.CLIENT)
	public boolean intersects(Vec3 vec3, Vec3 vec32) {
		return this.intersects(
			Math.min(vec3.x, vec32.x),
			Math.min(vec3.y, vec32.y),
			Math.min(vec3.z, vec32.z),
			Math.max(vec3.x, vec32.x),
			Math.max(vec3.y, vec32.y),
			Math.max(vec3.z, vec32.z)
		);
	}

	public boolean contains(Vec3 vec3) {
		return this.contains(vec3.x, vec3.y, vec3.z);
	}

	public boolean contains(double d, double e, double f) {
		return d >= this.minX && d < this.maxX && e >= this.minY && e < this.maxY && f >= this.minZ && f < this.maxZ;
	}

	public double getSize() {
		double d = this.getXsize();
		double e = this.getYsize();
		double f = this.getZsize();
		return (d + e + f) / 3.0;
	}

	public double getXsize() {
		return this.maxX - this.minX;
	}

	public double getYsize() {
		return this.maxY - this.minY;
	}

	public double getZsize() {
		return this.maxZ - this.minZ;
	}

	public AABB deflate(double d) {
		return this.inflate(-d);
	}

	public Optional<Vec3> clip(Vec3 vec3, Vec3 vec32) {
		double[] ds = new double[]{1.0};
		double d = vec32.x - vec3.x;
		double e = vec32.y - vec3.y;
		double f = vec32.z - vec3.z;
		Direction direction = getDirection(this, vec3, ds, null, d, e, f);
		if (direction == null) {
			return Optional.empty();
		} else {
			double g = ds[0];
			return Optional.of(vec3.add(g * d, g * e, g * f));
		}
	}

	@Nullable
	public static BlockHitResult clip(Iterable<AABB> iterable, Vec3 vec3, Vec3 vec32, BlockPos blockPos) {
		double[] ds = new double[]{1.0};
		Direction direction = null;
		double d = vec32.x - vec3.x;
		double e = vec32.y - vec3.y;
		double f = vec32.z - vec3.z;

		for (AABB aABB : iterable) {
			direction = getDirection(aABB.move(blockPos), vec3, ds, direction, d, e, f);
		}

		if (direction == null) {
			return null;
		} else {
			double g = ds[0];
			return new BlockHitResult(vec3.add(g * d, g * e, g * f), direction, blockPos, false);
		}
	}

	@Nullable
	private static Direction getDirection(AABB aABB, Vec3 vec3, double[] ds, @Nullable Direction direction, double d, double e, double f) {
		if (d > 1.0E-7) {
			direction = clipPoint(ds, direction, d, e, f, aABB.minX, aABB.minY, aABB.maxY, aABB.minZ, aABB.maxZ, Direction.WEST, vec3.x, vec3.y, vec3.z);
		} else if (d < -1.0E-7) {
			direction = clipPoint(ds, direction, d, e, f, aABB.maxX, aABB.minY, aABB.maxY, aABB.minZ, aABB.maxZ, Direction.EAST, vec3.x, vec3.y, vec3.z);
		}

		if (e > 1.0E-7) {
			direction = clipPoint(ds, direction, e, f, d, aABB.minY, aABB.minZ, aABB.maxZ, aABB.minX, aABB.maxX, Direction.DOWN, vec3.y, vec3.z, vec3.x);
		} else if (e < -1.0E-7) {
			direction = clipPoint(ds, direction, e, f, d, aABB.maxY, aABB.minZ, aABB.maxZ, aABB.minX, aABB.maxX, Direction.UP, vec3.y, vec3.z, vec3.x);
		}

		if (f > 1.0E-7) {
			direction = clipPoint(ds, direction, f, d, e, aABB.minZ, aABB.minX, aABB.maxX, aABB.minY, aABB.maxY, Direction.NORTH, vec3.z, vec3.x, vec3.y);
		} else if (f < -1.0E-7) {
			direction = clipPoint(ds, direction, f, d, e, aABB.maxZ, aABB.minX, aABB.maxX, aABB.minY, aABB.maxY, Direction.SOUTH, vec3.z, vec3.x, vec3.y);
		}

		return direction;
	}

	@Nullable
	private static Direction clipPoint(
		double[] ds,
		@Nullable Direction direction,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i,
		double j,
		double k,
		Direction direction2,
		double l,
		double m,
		double n
	) {
		double o = (g - l) / d;
		double p = m + o * e;
		double q = n + o * f;
		if (0.0 < o && o < ds[0] && h - 1.0E-7 < p && p < i + 1.0E-7 && j - 1.0E-7 < q && q < k + 1.0E-7) {
			ds[0] = o;
			return direction2;
		} else {
			return direction;
		}
	}

	public String toString() {
		return "AABB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
	}

	@Environment(EnvType.CLIENT)
	public boolean hasNaN() {
		return Double.isNaN(this.minX)
			|| Double.isNaN(this.minY)
			|| Double.isNaN(this.minZ)
			|| Double.isNaN(this.maxX)
			|| Double.isNaN(this.maxY)
			|| Double.isNaN(this.maxZ);
	}

	public Vec3 getCenter() {
		return new Vec3(Mth.lerp(0.5, this.minX, this.maxX), Mth.lerp(0.5, this.minY, this.maxY), Mth.lerp(0.5, this.minZ, this.maxZ));
	}

	public static AABB ofSize(Vec3 vec3, double d, double e, double f) {
		return new AABB(vec3.x - d / 2.0, vec3.y - e / 2.0, vec3.z - f / 2.0, vec3.x + d / 2.0, vec3.y + e / 2.0, vec3.z + f / 2.0);
	}
}
