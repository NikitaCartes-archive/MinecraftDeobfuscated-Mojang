package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.IntArrayTag;

public class BoundingBox {
	public int x0;
	public int y0;
	public int z0;
	public int x1;
	public int y1;
	public int z1;

	public BoundingBox() {
	}

	public BoundingBox(int[] is) {
		if (is.length == 6) {
			this.x0 = is[0];
			this.y0 = is[1];
			this.z0 = is[2];
			this.x1 = is[3];
			this.y1 = is[4];
			this.z1 = is[5];
		}
	}

	public static BoundingBox getUnknownBox() {
		return new BoundingBox(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public static BoundingBox infinite() {
		return new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public static BoundingBox orientBox(int i, int j, int k, int l, int m, int n, int o, int p, int q, Direction direction) {
		switch (direction) {
			case NORTH:
				return new BoundingBox(i + l, j + m, k - q + 1 + n, i + o - 1 + l, j + p - 1 + m, k + n);
			case SOUTH:
				return new BoundingBox(i + l, j + m, k + n, i + o - 1 + l, j + p - 1 + m, k + q - 1 + n);
			case WEST:
				return new BoundingBox(i - q + 1 + n, j + m, k + l, i + n, j + p - 1 + m, k + o - 1 + l);
			case EAST:
				return new BoundingBox(i + n, j + m, k + l, i + q - 1 + n, j + p - 1 + m, k + o - 1 + l);
			default:
				return new BoundingBox(i + l, j + m, k + n, i + o - 1 + l, j + p - 1 + m, k + q - 1 + n);
		}
	}

	public static BoundingBox createProper(int i, int j, int k, int l, int m, int n) {
		return new BoundingBox(Math.min(i, l), Math.min(j, m), Math.min(k, n), Math.max(i, l), Math.max(j, m), Math.max(k, n));
	}

	public BoundingBox(BoundingBox boundingBox) {
		this.x0 = boundingBox.x0;
		this.y0 = boundingBox.y0;
		this.z0 = boundingBox.z0;
		this.x1 = boundingBox.x1;
		this.y1 = boundingBox.y1;
		this.z1 = boundingBox.z1;
	}

	public BoundingBox(int i, int j, int k, int l, int m, int n) {
		this.x0 = i;
		this.y0 = j;
		this.z0 = k;
		this.x1 = l;
		this.y1 = m;
		this.z1 = n;
	}

	public BoundingBox(Vec3i vec3i, Vec3i vec3i2) {
		this.x0 = Math.min(vec3i.getX(), vec3i2.getX());
		this.y0 = Math.min(vec3i.getY(), vec3i2.getY());
		this.z0 = Math.min(vec3i.getZ(), vec3i2.getZ());
		this.x1 = Math.max(vec3i.getX(), vec3i2.getX());
		this.y1 = Math.max(vec3i.getY(), vec3i2.getY());
		this.z1 = Math.max(vec3i.getZ(), vec3i2.getZ());
	}

	public BoundingBox(int i, int j, int k, int l) {
		this.x0 = i;
		this.z0 = j;
		this.x1 = k;
		this.z1 = l;
		this.y0 = 1;
		this.y1 = 512;
	}

	public boolean intersects(BoundingBox boundingBox) {
		return this.x1 >= boundingBox.x0
			&& this.x0 <= boundingBox.x1
			&& this.z1 >= boundingBox.z0
			&& this.z0 <= boundingBox.z1
			&& this.y1 >= boundingBox.y0
			&& this.y0 <= boundingBox.y1;
	}

	public boolean intersects(int i, int j, int k, int l) {
		return this.x1 >= i && this.x0 <= k && this.z1 >= j && this.z0 <= l;
	}

	public void expand(BoundingBox boundingBox) {
		this.x0 = Math.min(this.x0, boundingBox.x0);
		this.y0 = Math.min(this.y0, boundingBox.y0);
		this.z0 = Math.min(this.z0, boundingBox.z0);
		this.x1 = Math.max(this.x1, boundingBox.x1);
		this.y1 = Math.max(this.y1, boundingBox.y1);
		this.z1 = Math.max(this.z1, boundingBox.z1);
	}

	public void move(int i, int j, int k) {
		this.x0 += i;
		this.y0 += j;
		this.z0 += k;
		this.x1 += i;
		this.y1 += j;
		this.z1 += k;
	}

	public BoundingBox moved(int i, int j, int k) {
		return new BoundingBox(this.x0 + i, this.y0 + j, this.z0 + k, this.x1 + i, this.y1 + j, this.z1 + k);
	}

	public void move(Vec3i vec3i) {
		this.move(vec3i.getX(), vec3i.getY(), vec3i.getZ());
	}

	public boolean isInside(Vec3i vec3i) {
		return vec3i.getX() >= this.x0
			&& vec3i.getX() <= this.x1
			&& vec3i.getZ() >= this.z0
			&& vec3i.getZ() <= this.z1
			&& vec3i.getY() >= this.y0
			&& vec3i.getY() <= this.y1;
	}

	public Vec3i getLength() {
		return new Vec3i(this.x1 - this.x0, this.y1 - this.y0, this.z1 - this.z0);
	}

	public int getXSpan() {
		return this.x1 - this.x0 + 1;
	}

	public int getYSpan() {
		return this.y1 - this.y0 + 1;
	}

	public int getZSpan() {
		return this.z1 - this.z0 + 1;
	}

	public Vec3i getCenter() {
		return new BlockPos(this.x0 + (this.x1 - this.x0 + 1) / 2, this.y0 + (this.y1 - this.y0 + 1) / 2, this.z0 + (this.z1 - this.z0 + 1) / 2);
	}

	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("x0", this.x0)
			.add("y0", this.y0)
			.add("z0", this.z0)
			.add("x1", this.x1)
			.add("y1", this.y1)
			.add("z1", this.z1)
			.toString();
	}

	public IntArrayTag createTag() {
		return new IntArrayTag(new int[]{this.x0, this.y0, this.z0, this.x1, this.y1, this.z1});
	}
}
