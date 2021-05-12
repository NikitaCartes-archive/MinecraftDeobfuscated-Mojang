package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BoundingBox {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Codec<BoundingBox> CODEC = Codec.INT_STREAM
		.<BoundingBox>comapFlatMap(
			intStream -> Util.fixedSize(intStream, 6).map(is -> new BoundingBox(is[0], is[1], is[2], is[3], is[4], is[5])),
			boundingBox -> IntStream.of(new int[]{boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ})
		)
		.stable();
	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;

	public BoundingBox(BlockPos blockPos) {
		this(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	public BoundingBox(int i, int j, int k, int l, int m, int n) {
		this.minX = i;
		this.minY = j;
		this.minZ = k;
		this.maxX = l;
		this.maxY = m;
		this.maxZ = n;
		if (l < i || m < j || n < k) {
			String string = "Invalid bounding box data, inverted bounds for: " + this;
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				throw new IllegalStateException(string);
			}

			LOGGER.error(string);
			this.minX = Math.min(i, l);
			this.minY = Math.min(j, m);
			this.minZ = Math.min(k, n);
			this.maxX = Math.max(i, l);
			this.maxY = Math.max(j, m);
			this.maxZ = Math.max(k, n);
		}
	}

	public static BoundingBox fromCorners(Vec3i vec3i, Vec3i vec3i2) {
		return new BoundingBox(
			Math.min(vec3i.getX(), vec3i2.getX()),
			Math.min(vec3i.getY(), vec3i2.getY()),
			Math.min(vec3i.getZ(), vec3i2.getZ()),
			Math.max(vec3i.getX(), vec3i2.getX()),
			Math.max(vec3i.getY(), vec3i2.getY()),
			Math.max(vec3i.getZ(), vec3i2.getZ())
		);
	}

	public static BoundingBox infinite() {
		return new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public static BoundingBox orientBox(int i, int j, int k, int l, int m, int n, int o, int p, int q, Direction direction) {
		switch (direction) {
			case SOUTH:
			default:
				return new BoundingBox(i + l, j + m, k + n, i + o - 1 + l, j + p - 1 + m, k + q - 1 + n);
			case NORTH:
				return new BoundingBox(i + l, j + m, k - q + 1 + n, i + o - 1 + l, j + p - 1 + m, k + n);
			case WEST:
				return new BoundingBox(i - q + 1 + n, j + m, k + l, i + n, j + p - 1 + m, k + o - 1 + l);
			case EAST:
				return new BoundingBox(i + n, j + m, k + l, i + q - 1 + n, j + p - 1 + m, k + o - 1 + l);
		}
	}

	public boolean intersects(BoundingBox boundingBox) {
		return this.maxX >= boundingBox.minX
			&& this.minX <= boundingBox.maxX
			&& this.maxZ >= boundingBox.minZ
			&& this.minZ <= boundingBox.maxZ
			&& this.maxY >= boundingBox.minY
			&& this.minY <= boundingBox.maxY;
	}

	public boolean intersects(int i, int j, int k, int l) {
		return this.maxX >= i && this.minX <= k && this.maxZ >= j && this.minZ <= l;
	}

	public static Optional<BoundingBox> encapsulatingPositions(Iterable<BlockPos> iterable) {
		Iterator<BlockPos> iterator = iterable.iterator();
		if (!iterator.hasNext()) {
			return Optional.empty();
		} else {
			BoundingBox boundingBox = new BoundingBox((BlockPos)iterator.next());
			iterator.forEachRemaining(boundingBox::encapsulate);
			return Optional.of(boundingBox);
		}
	}

	public static Optional<BoundingBox> encapsulatingBoxes(Iterable<BoundingBox> iterable) {
		Iterator<BoundingBox> iterator = iterable.iterator();
		if (!iterator.hasNext()) {
			return Optional.empty();
		} else {
			BoundingBox boundingBox = (BoundingBox)iterator.next();
			BoundingBox boundingBox2 = new BoundingBox(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
			iterator.forEachRemaining(boundingBox2::encapsulate);
			return Optional.of(boundingBox2);
		}
	}

	public BoundingBox encapsulate(BoundingBox boundingBox) {
		this.minX = Math.min(this.minX, boundingBox.minX);
		this.minY = Math.min(this.minY, boundingBox.minY);
		this.minZ = Math.min(this.minZ, boundingBox.minZ);
		this.maxX = Math.max(this.maxX, boundingBox.maxX);
		this.maxY = Math.max(this.maxY, boundingBox.maxY);
		this.maxZ = Math.max(this.maxZ, boundingBox.maxZ);
		return this;
	}

	public BoundingBox encapsulate(BlockPos blockPos) {
		this.minX = Math.min(this.minX, blockPos.getX());
		this.minY = Math.min(this.minY, blockPos.getY());
		this.minZ = Math.min(this.minZ, blockPos.getZ());
		this.maxX = Math.max(this.maxX, blockPos.getX());
		this.maxY = Math.max(this.maxY, blockPos.getY());
		this.maxZ = Math.max(this.maxZ, blockPos.getZ());
		return this;
	}

	public BoundingBox inflate(int i) {
		this.minX -= i;
		this.minY -= i;
		this.minZ -= i;
		this.maxX += i;
		this.maxY += i;
		this.maxZ += i;
		return this;
	}

	public BoundingBox move(int i, int j, int k) {
		this.minX += i;
		this.minY += j;
		this.minZ += k;
		this.maxX += i;
		this.maxY += j;
		this.maxZ += k;
		return this;
	}

	public BoundingBox move(Vec3i vec3i) {
		return this.move(vec3i.getX(), vec3i.getY(), vec3i.getZ());
	}

	public BoundingBox moved(int i, int j, int k) {
		return new BoundingBox(this.minX + i, this.minY + j, this.minZ + k, this.maxX + i, this.maxY + j, this.maxZ + k);
	}

	public boolean isInside(Vec3i vec3i) {
		return vec3i.getX() >= this.minX
			&& vec3i.getX() <= this.maxX
			&& vec3i.getZ() >= this.minZ
			&& vec3i.getZ() <= this.maxZ
			&& vec3i.getY() >= this.minY
			&& vec3i.getY() <= this.maxY;
	}

	public Vec3i getLength() {
		return new Vec3i(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ);
	}

	public int getXSpan() {
		return this.maxX - this.minX + 1;
	}

	public int getYSpan() {
		return this.maxY - this.minY + 1;
	}

	public int getZSpan() {
		return this.maxZ - this.minZ + 1;
	}

	public BlockPos getCenter() {
		return new BlockPos(this.minX + (this.maxX - this.minX + 1) / 2, this.minY + (this.maxY - this.minY + 1) / 2, this.minZ + (this.maxZ - this.minZ + 1) / 2);
	}

	public void forAllCorners(Consumer<BlockPos> consumer) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		consumer.accept(mutableBlockPos.set(this.maxX, this.maxY, this.maxZ));
		consumer.accept(mutableBlockPos.set(this.minX, this.maxY, this.maxZ));
		consumer.accept(mutableBlockPos.set(this.maxX, this.minY, this.maxZ));
		consumer.accept(mutableBlockPos.set(this.minX, this.minY, this.maxZ));
		consumer.accept(mutableBlockPos.set(this.maxX, this.maxY, this.minZ));
		consumer.accept(mutableBlockPos.set(this.minX, this.maxY, this.minZ));
		consumer.accept(mutableBlockPos.set(this.maxX, this.minY, this.minZ));
		consumer.accept(mutableBlockPos.set(this.minX, this.minY, this.minZ));
	}

	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("minX", this.minX)
			.add("minY", this.minY)
			.add("minZ", this.minZ)
			.add("maxX", this.maxX)
			.add("maxY", this.maxY)
			.add("maxZ", this.maxZ)
			.toString();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof BoundingBox boundingBox)
				? false
				: this.minX == boundingBox.minX
					&& this.minY == boundingBox.minY
					&& this.minZ == boundingBox.minZ
					&& this.maxX == boundingBox.maxX
					&& this.maxY == boundingBox.maxY
					&& this.maxZ == boundingBox.maxZ;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ});
	}

	public int minX() {
		return this.minX;
	}

	public int minY() {
		return this.minY;
	}

	public int minZ() {
		return this.minZ;
	}

	public int maxX() {
		return this.maxX;
	}

	public int maxY() {
		return this.maxY;
	}

	public int maxZ() {
		return this.maxZ;
	}
}
