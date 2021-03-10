package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class BoundingBox {
	public static final Codec<BoundingBox> CODEC = Codec.INT_STREAM
		.<BoundingBox>comapFlatMap(
			intStream -> Util.fixedSize(intStream, 6).map(is -> new BoundingBox(is[0], is[1], is[2], is[3], is[4], is[5])),
			boundingBox -> IntStream.of(new int[]{boundingBox.x0, boundingBox.y0, boundingBox.z0, boundingBox.x1, boundingBox.y1, boundingBox.z1})
		)
		.stable();
	public int x0;
	public int y0;
	public int z0;
	public int x1;
	public int y1;
	public int z1;

	public BoundingBox(BlockPos blockPos) {
		this(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	public BoundingBox(int i, int j, int k, int l, int m, int n) {
		this.x0 = i;
		this.y0 = j;
		this.z0 = k;
		this.x1 = l;
		this.y1 = m;
		this.z1 = n;
	}

	public static BoundingBox createProper(Vec3i vec3i, Vec3i vec3i2) {
		return createProper(vec3i.getX(), vec3i.getY(), vec3i.getZ(), vec3i2.getX(), vec3i2.getY(), vec3i2.getZ());
	}

	public static BoundingBox createProper(int i, int j, int k, int l, int m, int n) {
		return new BoundingBox(Math.min(i, l), Math.min(j, m), Math.min(k, n), Math.max(i, l), Math.max(j, m), Math.max(k, n));
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

	public BoundingBox encapsulate(BlockPos blockPos) {
		this.x0 = Math.min(this.x0, blockPos.getX());
		this.y0 = Math.min(this.y0, blockPos.getY());
		this.z0 = Math.min(this.z0, blockPos.getZ());
		this.x1 = Math.max(this.x1, blockPos.getX());
		this.y1 = Math.max(this.y1, blockPos.getY());
		this.z1 = Math.max(this.z1, blockPos.getZ());
		return this;
	}

	public BoundingBox move(int i, int j, int k) {
		this.x0 += i;
		this.y0 += j;
		this.z0 += k;
		this.x1 += i;
		this.y1 += j;
		this.z1 += k;
		return this;
	}

	public BoundingBox move(Vec3i vec3i) {
		return this.move(vec3i.getX(), vec3i.getY(), vec3i.getZ());
	}

	public BoundingBox moved(int i, int j, int k) {
		return new BoundingBox(this.x0 + i, this.y0 + j, this.z0 + k, this.x1 + i, this.y1 + j, this.z1 + k);
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

	public BlockPos getCenter() {
		return new BlockPos(this.x0 + (this.x1 - this.x0 + 1) / 2, this.y0 + (this.y1 - this.y0 + 1) / 2, this.z0 + (this.z1 - this.z0 + 1) / 2);
	}

	public void forAllCorners(Consumer<BlockPos> consumer) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		consumer.accept(mutableBlockPos.set(this.x1, this.y1, this.z1));
		consumer.accept(mutableBlockPos.set(this.x0, this.y1, this.z1));
		consumer.accept(mutableBlockPos.set(this.x1, this.y0, this.z1));
		consumer.accept(mutableBlockPos.set(this.x0, this.y0, this.z1));
		consumer.accept(mutableBlockPos.set(this.x1, this.y1, this.z0));
		consumer.accept(mutableBlockPos.set(this.x0, this.y1, this.z0));
		consumer.accept(mutableBlockPos.set(this.x1, this.y0, this.z0));
		consumer.accept(mutableBlockPos.set(this.x0, this.y0, this.z0));
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

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof BoundingBox)) {
			return false;
		} else {
			BoundingBox boundingBox = (BoundingBox)object;
			return this.x0 == boundingBox.x0
				&& this.y0 == boundingBox.y0
				&& this.z0 == boundingBox.z0
				&& this.x1 == boundingBox.x1
				&& this.y1 == boundingBox.y1
				&& this.z1 == boundingBox.z1;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.x0, this.y0, this.z0, this.x1, this.y1, this.z1});
	}
}
