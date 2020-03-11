package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Optional;
import java.util.Spliterator.OfInt;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.minecraft.util.Mth;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Immutable
public class BlockPos extends Vec3i implements Serializable {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final BlockPos ZERO = new BlockPos(0, 0, 0);
	private static final int PACKED_X_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
	private static final int PACKED_Z_LENGTH = PACKED_X_LENGTH;
	private static final int PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
	private static final long PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
	private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
	private static final long PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
	private static final int Z_OFFSET = PACKED_Y_LENGTH;
	private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;

	public BlockPos(int i, int j, int k) {
		super(i, j, k);
	}

	public BlockPos(double d, double e, double f) {
		super(d, e, f);
	}

	public BlockPos(Vec3 vec3) {
		this(vec3.x, vec3.y, vec3.z);
	}

	public BlockPos(Position position) {
		this(position.x(), position.y(), position.z());
	}

	public BlockPos(Vec3i vec3i) {
		this(vec3i.getX(), vec3i.getY(), vec3i.getZ());
	}

	public static <T> BlockPos deserialize(Dynamic<T> dynamic) {
		OfInt ofInt = dynamic.asIntStream().spliterator();
		int[] is = new int[3];
		if (ofInt.tryAdvance(i -> is[0] = i) && ofInt.tryAdvance(i -> is[1] = i)) {
			ofInt.tryAdvance(i -> is[2] = i);
		}

		return new BlockPos(is[0], is[1], is[2]);
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return dynamicOps.createIntList(IntStream.of(new int[]{this.getX(), this.getY(), this.getZ()}));
	}

	public static long offset(long l, Direction direction) {
		return offset(l, direction.getStepX(), direction.getStepY(), direction.getStepZ());
	}

	public static long offset(long l, int i, int j, int k) {
		return asLong(getX(l) + i, getY(l) + j, getZ(l) + k);
	}

	public static int getX(long l) {
		return (int)(l << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
	}

	public static int getY(long l) {
		return (int)(l << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
	}

	public static int getZ(long l) {
		return (int)(l << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
	}

	public static BlockPos of(long l) {
		return new BlockPos(getX(l), getY(l), getZ(l));
	}

	public static long asLong(int i, int j, int k) {
		long l = 0L;
		l |= ((long)i & PACKED_X_MASK) << X_OFFSET;
		l |= ((long)j & PACKED_Y_MASK) << 0;
		return l | ((long)k & PACKED_Z_MASK) << Z_OFFSET;
	}

	public static long getFlatIndex(long l) {
		return l & -16L;
	}

	public long asLong() {
		return asLong(this.getX(), this.getY(), this.getZ());
	}

	public BlockPos offset(double d, double e, double f) {
		return d == 0.0 && e == 0.0 && f == 0.0 ? this : new BlockPos((double)this.getX() + d, (double)this.getY() + e, (double)this.getZ() + f);
	}

	public BlockPos offset(int i, int j, int k) {
		return i == 0 && j == 0 && k == 0 ? this : new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
	}

	public BlockPos offset(Vec3i vec3i) {
		return this.offset(vec3i.getX(), vec3i.getY(), vec3i.getZ());
	}

	public BlockPos subtract(Vec3i vec3i) {
		return this.offset(-vec3i.getX(), -vec3i.getY(), -vec3i.getZ());
	}

	public BlockPos above() {
		return this.relative(Direction.UP);
	}

	public BlockPos above(int i) {
		return this.relative(Direction.UP, i);
	}

	public BlockPos below() {
		return this.relative(Direction.DOWN);
	}

	public BlockPos below(int i) {
		return this.relative(Direction.DOWN, i);
	}

	public BlockPos north() {
		return this.relative(Direction.NORTH);
	}

	public BlockPos north(int i) {
		return this.relative(Direction.NORTH, i);
	}

	public BlockPos south() {
		return this.relative(Direction.SOUTH);
	}

	public BlockPos south(int i) {
		return this.relative(Direction.SOUTH, i);
	}

	public BlockPos west() {
		return this.relative(Direction.WEST);
	}

	public BlockPos west(int i) {
		return this.relative(Direction.WEST, i);
	}

	public BlockPos east() {
		return this.relative(Direction.EAST);
	}

	public BlockPos east(int i) {
		return this.relative(Direction.EAST, i);
	}

	public BlockPos relative(Direction direction) {
		return new BlockPos(this.getX() + direction.getStepX(), this.getY() + direction.getStepY(), this.getZ() + direction.getStepZ());
	}

	public BlockPos relative(Direction direction, int i) {
		return i == 0 ? this : new BlockPos(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
	}

	public BlockPos rotate(Rotation rotation) {
		switch (rotation) {
			case NONE:
			default:
				return this;
			case CLOCKWISE_90:
				return new BlockPos(-this.getZ(), this.getY(), this.getX());
			case CLOCKWISE_180:
				return new BlockPos(-this.getX(), this.getY(), -this.getZ());
			case COUNTERCLOCKWISE_90:
				return new BlockPos(this.getZ(), this.getY(), -this.getX());
		}
	}

	public BlockPos cross(Vec3i vec3i) {
		return new BlockPos(
			this.getY() * vec3i.getZ() - this.getZ() * vec3i.getY(),
			this.getZ() * vec3i.getX() - this.getX() * vec3i.getZ(),
			this.getX() * vec3i.getY() - this.getY() * vec3i.getX()
		);
	}

	public BlockPos immutable() {
		return this;
	}

	public BlockPos.MutableBlockPos mutable() {
		return new BlockPos.MutableBlockPos(this.getX(), this.getY(), this.getZ());
	}

	public static Iterable<BlockPos> withinManhattan(BlockPos blockPos, int i, int j, int k) {
		int l = i + j + k;
		return () -> new AbstractIterator<BlockPos>() {
				private int currentDepth;
				private int maxX;
				private int maxY;
				private int x;
				private int y;
				@Nullable
				private BlockPos pendingBlockPos;

				protected BlockPos computeNext() {
					if (this.pendingBlockPos != null) {
						BlockPos blockPos = this.pendingBlockPos;
						this.pendingBlockPos = null;
						return blockPos;
					} else {
						BlockPos blockPos;
						for (blockPos = null; blockPos == null; this.y++) {
							if (this.y > this.maxY) {
								this.x++;
								if (this.x > this.maxX) {
									this.currentDepth++;
									if (this.currentDepth > l) {
										return this.endOfData();
									}

									this.maxX = Math.min(i, this.currentDepth);
									this.x = -this.maxX;
								}

								this.maxY = Math.min(j, this.currentDepth - Math.abs(this.x));
								this.y = -this.maxY;
							}

							int i = this.x;
							int j = this.y;
							int k = this.currentDepth - Math.abs(i) - Math.abs(j);
							if (k <= k) {
								if (k != 0) {
									this.pendingBlockPos = blockPos.offset(i, j, -k);
								}

								blockPos = blockPos.offset(i, j, k);
							}
						}

						return blockPos;
					}
				}
			};
	}

	public static Optional<BlockPos> findClosestMatch(BlockPos blockPos, int i, int j, Predicate<BlockPos> predicate) {
		return withinManhattanStream(blockPos, i, j, i).filter(predicate).findFirst();
	}

	public static Stream<BlockPos> withinManhattanStream(BlockPos blockPos, int i, int j, int k) {
		return StreamSupport.stream(withinManhattan(blockPos, i, j, k).spliterator(), false);
	}

	public static Iterable<BlockPos> betweenClosed(BlockPos blockPos, BlockPos blockPos2) {
		return betweenClosed(
			Math.min(blockPos.getX(), blockPos2.getX()),
			Math.min(blockPos.getY(), blockPos2.getY()),
			Math.min(blockPos.getZ(), blockPos2.getZ()),
			Math.max(blockPos.getX(), blockPos2.getX()),
			Math.max(blockPos.getY(), blockPos2.getY()),
			Math.max(blockPos.getZ(), blockPos2.getZ())
		);
	}

	public static Stream<BlockPos> betweenClosedStream(BlockPos blockPos, BlockPos blockPos2) {
		return StreamSupport.stream(betweenClosed(blockPos, blockPos2).spliterator(), false);
	}

	public static Stream<BlockPos> betweenClosedStream(BoundingBox boundingBox) {
		return betweenClosedStream(
			Math.min(boundingBox.x0, boundingBox.x1),
			Math.min(boundingBox.y0, boundingBox.y1),
			Math.min(boundingBox.z0, boundingBox.z1),
			Math.max(boundingBox.x0, boundingBox.x1),
			Math.max(boundingBox.y0, boundingBox.y1),
			Math.max(boundingBox.z0, boundingBox.z1)
		);
	}

	public static Stream<BlockPos> betweenClosedStream(int i, int j, int k, int l, int m, int n) {
		return StreamSupport.stream(betweenClosed(i, j, k, l, m, n).spliterator(), false);
	}

	public static Iterable<BlockPos> betweenClosed(int i, int j, int k, int l, int m, int n) {
		int o = l - i + 1;
		int p = m - j + 1;
		int q = n - k + 1;
		int r = o * p * q;
		return () -> new AbstractIterator<BlockPos>() {
				private int index;

				protected BlockPos computeNext() {
					if (this.index == r) {
						return this.endOfData();
					} else {
						int i = this.index % o;
						int j = this.index / o;
						int k = j % p;
						int l = j / p;
						this.index++;
						return new BlockPos(i + i, j + k, k + l);
					}
				}
			};
	}

	public static class MutableBlockPos extends BlockPos {
		public MutableBlockPos() {
			this(0, 0, 0);
		}

		public MutableBlockPos(int i, int j, int k) {
			super(i, j, k);
		}

		public MutableBlockPos(double d, double e, double f) {
			this(Mth.floor(d), Mth.floor(e), Mth.floor(f));
		}

		@Override
		public BlockPos offset(double d, double e, double f) {
			return super.offset(d, e, f).immutable();
		}

		@Override
		public BlockPos offset(int i, int j, int k) {
			return super.offset(i, j, k).immutable();
		}

		@Override
		public BlockPos relative(Direction direction, int i) {
			return super.relative(direction, i).immutable();
		}

		@Override
		public BlockPos rotate(Rotation rotation) {
			return super.rotate(rotation).immutable();
		}

		public BlockPos.MutableBlockPos set(int i, int j, int k) {
			this.setX(i);
			this.setY(j);
			this.setZ(k);
			return this;
		}

		public BlockPos.MutableBlockPos set(double d, double e, double f) {
			return this.set(Mth.floor(d), Mth.floor(e), Mth.floor(f));
		}

		public BlockPos.MutableBlockPos set(Vec3i vec3i) {
			return this.set(vec3i.getX(), vec3i.getY(), vec3i.getZ());
		}

		public BlockPos.MutableBlockPos set(long l) {
			return this.set(getX(l), getY(l), getZ(l));
		}

		public BlockPos.MutableBlockPos set(AxisCycle axisCycle, int i, int j, int k) {
			return this.set(axisCycle.cycle(i, j, k, Direction.Axis.X), axisCycle.cycle(i, j, k, Direction.Axis.Y), axisCycle.cycle(i, j, k, Direction.Axis.Z));
		}

		public BlockPos.MutableBlockPos setWithOffset(Vec3i vec3i, Direction direction) {
			return this.set(vec3i.getX() + direction.getStepX(), vec3i.getY() + direction.getStepY(), vec3i.getZ() + direction.getStepZ());
		}

		public BlockPos.MutableBlockPos setWithOffset(Vec3i vec3i, int i, int j, int k) {
			return this.set(vec3i.getX() + i, vec3i.getY() + j, vec3i.getZ() + k);
		}

		public BlockPos.MutableBlockPos move(Direction direction) {
			return this.move(direction, 1);
		}

		public BlockPos.MutableBlockPos move(Direction direction, int i) {
			return this.set(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
		}

		public BlockPos.MutableBlockPos move(int i, int j, int k) {
			return this.set(this.getX() + i, this.getY() + j, this.getZ() + k);
		}

		@Override
		public void setX(int i) {
			super.setX(i);
		}

		@Override
		public void setY(int i) {
			super.setY(i);
		}

		@Override
		public void setZ(int i) {
			super.setZ(i);
		}

		@Override
		public BlockPos immutable() {
			return new BlockPos(this);
		}
	}
}
