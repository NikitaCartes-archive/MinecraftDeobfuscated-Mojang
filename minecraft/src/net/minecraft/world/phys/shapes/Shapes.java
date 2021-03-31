package net.minecraft.world.phys.shapes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class Shapes {
	public static final double EPSILON = 1.0E-7;
	public static final double BIG_EPSILON = 1.0E-6;
	private static final VoxelShape BLOCK = Util.make(() -> {
		DiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(1, 1, 1);
		discreteVoxelShape.fill(0, 0, 0);
		return new CubeVoxelShape(discreteVoxelShape);
	});
	public static final VoxelShape INFINITY = box(
		Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
	);
	private static final VoxelShape EMPTY = new ArrayVoxelShape(
		new BitSetDiscreteVoxelShape(0, 0, 0), new DoubleArrayList(new double[]{0.0}), new DoubleArrayList(new double[]{0.0}), new DoubleArrayList(new double[]{0.0})
	);

	public static VoxelShape empty() {
		return EMPTY;
	}

	public static VoxelShape block() {
		return BLOCK;
	}

	public static VoxelShape box(double d, double e, double f, double g, double h, double i) {
		if (!(d > g) && !(e > h) && !(f > i)) {
			return create(d, e, f, g, h, i);
		} else {
			throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
		}
	}

	public static VoxelShape create(double d, double e, double f, double g, double h, double i) {
		if (!(g - d < 1.0E-7) && !(h - e < 1.0E-7) && !(i - f < 1.0E-7)) {
			int j = findBits(d, g);
			int k = findBits(e, h);
			int l = findBits(f, i);
			if (j < 0 || k < 0 || l < 0) {
				return new ArrayVoxelShape(
					BLOCK.shape, DoubleArrayList.wrap(new double[]{d, g}), DoubleArrayList.wrap(new double[]{e, h}), DoubleArrayList.wrap(new double[]{f, i})
				);
			} else if (j == 0 && k == 0 && l == 0) {
				return block();
			} else {
				int m = 1 << j;
				int n = 1 << k;
				int o = 1 << l;
				BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = BitSetDiscreteVoxelShape.withFilledBounds(
					m,
					n,
					o,
					(int)Math.round(d * (double)m),
					(int)Math.round(e * (double)n),
					(int)Math.round(f * (double)o),
					(int)Math.round(g * (double)m),
					(int)Math.round(h * (double)n),
					(int)Math.round(i * (double)o)
				);
				return new CubeVoxelShape(bitSetDiscreteVoxelShape);
			}
		} else {
			return empty();
		}
	}

	public static VoxelShape create(AABB aABB) {
		return create(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
	}

	@VisibleForTesting
	protected static int findBits(double d, double e) {
		if (!(d < -1.0E-7) && !(e > 1.0000001)) {
			for (int i = 0; i <= 3; i++) {
				int j = 1 << i;
				double f = d * (double)j;
				double g = e * (double)j;
				boolean bl = Math.abs(f - (double)Math.round(f)) < 1.0E-7 * (double)j;
				boolean bl2 = Math.abs(g - (double)Math.round(g)) < 1.0E-7 * (double)j;
				if (bl && bl2) {
					return i;
				}
			}

			return -1;
		} else {
			return -1;
		}
	}

	protected static long lcm(int i, int j) {
		return (long)i * (long)(j / IntMath.gcd(i, j));
	}

	public static VoxelShape or(VoxelShape voxelShape, VoxelShape voxelShape2) {
		return join(voxelShape, voxelShape2, BooleanOp.OR);
	}

	public static VoxelShape or(VoxelShape voxelShape, VoxelShape... voxelShapes) {
		return (VoxelShape)Arrays.stream(voxelShapes).reduce(voxelShape, Shapes::or);
	}

	public static VoxelShape join(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
		return joinUnoptimized(voxelShape, voxelShape2, booleanOp).optimize();
	}

	public static VoxelShape joinUnoptimized(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
		if (booleanOp.apply(false, false)) {
			throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
		} else if (voxelShape == voxelShape2) {
			return booleanOp.apply(true, true) ? voxelShape : empty();
		} else {
			boolean bl = booleanOp.apply(true, false);
			boolean bl2 = booleanOp.apply(false, true);
			if (voxelShape.isEmpty()) {
				return bl2 ? voxelShape2 : empty();
			} else if (voxelShape2.isEmpty()) {
				return bl ? voxelShape : empty();
			} else {
				IndexMerger indexMerger = createIndexMerger(1, voxelShape.getCoords(Direction.Axis.X), voxelShape2.getCoords(Direction.Axis.X), bl, bl2);
				IndexMerger indexMerger2 = createIndexMerger(
					indexMerger.size() - 1, voxelShape.getCoords(Direction.Axis.Y), voxelShape2.getCoords(Direction.Axis.Y), bl, bl2
				);
				IndexMerger indexMerger3 = createIndexMerger(
					(indexMerger.size() - 1) * (indexMerger2.size() - 1), voxelShape.getCoords(Direction.Axis.Z), voxelShape2.getCoords(Direction.Axis.Z), bl, bl2
				);
				BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = BitSetDiscreteVoxelShape.join(
					voxelShape.shape, voxelShape2.shape, indexMerger, indexMerger2, indexMerger3, booleanOp
				);
				return (VoxelShape)(indexMerger instanceof DiscreteCubeMerger && indexMerger2 instanceof DiscreteCubeMerger && indexMerger3 instanceof DiscreteCubeMerger
					? new CubeVoxelShape(bitSetDiscreteVoxelShape)
					: new ArrayVoxelShape(bitSetDiscreteVoxelShape, indexMerger.getList(), indexMerger2.getList(), indexMerger3.getList()));
			}
		}
	}

	public static boolean joinIsNotEmpty(VoxelShape voxelShape, VoxelShape voxelShape2, BooleanOp booleanOp) {
		if (booleanOp.apply(false, false)) {
			throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
		} else {
			boolean bl = voxelShape.isEmpty();
			boolean bl2 = voxelShape2.isEmpty();
			if (!bl && !bl2) {
				if (voxelShape == voxelShape2) {
					return booleanOp.apply(true, true);
				} else {
					boolean bl3 = booleanOp.apply(true, false);
					boolean bl4 = booleanOp.apply(false, true);

					for (Direction.Axis axis : AxisCycle.AXIS_VALUES) {
						if (voxelShape.max(axis) < voxelShape2.min(axis) - 1.0E-7) {
							return bl3 || bl4;
						}

						if (voxelShape2.max(axis) < voxelShape.min(axis) - 1.0E-7) {
							return bl3 || bl4;
						}
					}

					IndexMerger indexMerger = createIndexMerger(1, voxelShape.getCoords(Direction.Axis.X), voxelShape2.getCoords(Direction.Axis.X), bl3, bl4);
					IndexMerger indexMerger2 = createIndexMerger(
						indexMerger.size() - 1, voxelShape.getCoords(Direction.Axis.Y), voxelShape2.getCoords(Direction.Axis.Y), bl3, bl4
					);
					IndexMerger indexMerger3 = createIndexMerger(
						(indexMerger.size() - 1) * (indexMerger2.size() - 1), voxelShape.getCoords(Direction.Axis.Z), voxelShape2.getCoords(Direction.Axis.Z), bl3, bl4
					);
					return joinIsNotEmpty(indexMerger, indexMerger2, indexMerger3, voxelShape.shape, voxelShape2.shape, booleanOp);
				}
			} else {
				return booleanOp.apply(!bl, !bl2);
			}
		}
	}

	private static boolean joinIsNotEmpty(
		IndexMerger indexMerger,
		IndexMerger indexMerger2,
		IndexMerger indexMerger3,
		DiscreteVoxelShape discreteVoxelShape,
		DiscreteVoxelShape discreteVoxelShape2,
		BooleanOp booleanOp
	) {
		return !indexMerger.forMergedIndexes(
			(i, j, k) -> indexMerger2.forMergedIndexes(
					(kx, l, m) -> indexMerger3.forMergedIndexes(
							(mx, n, o) -> !booleanOp.apply(discreteVoxelShape.isFullWide(i, kx, mx), discreteVoxelShape2.isFullWide(j, l, n))
						)
				)
		);
	}

	public static double collide(Direction.Axis axis, AABB aABB, Stream<VoxelShape> stream, double d) {
		Iterator<VoxelShape> iterator = stream.iterator();

		while (iterator.hasNext()) {
			if (Math.abs(d) < 1.0E-7) {
				return 0.0;
			}

			d = ((VoxelShape)iterator.next()).collide(axis, aABB, d);
		}

		return d;
	}

	public static double collide(Direction.Axis axis, AABB aABB, LevelReader levelReader, double d, CollisionContext collisionContext, Stream<VoxelShape> stream) {
		return collide(aABB, levelReader, d, collisionContext, AxisCycle.between(axis, Direction.Axis.Z), stream);
	}

	private static double collide(AABB aABB, LevelReader levelReader, double d, CollisionContext collisionContext, AxisCycle axisCycle, Stream<VoxelShape> stream) {
		if (aABB.getXsize() < 1.0E-6 || aABB.getYsize() < 1.0E-6 || aABB.getZsize() < 1.0E-6) {
			return d;
		} else if (Math.abs(d) < 1.0E-7) {
			return 0.0;
		} else {
			AxisCycle axisCycle2 = axisCycle.inverse();
			Direction.Axis axis = axisCycle2.cycle(Direction.Axis.X);
			Direction.Axis axis2 = axisCycle2.cycle(Direction.Axis.Y);
			Direction.Axis axis3 = axisCycle2.cycle(Direction.Axis.Z);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			int i = Mth.floor(aABB.min(axis) - 1.0E-7) - 1;
			int j = Mth.floor(aABB.max(axis) + 1.0E-7) + 1;
			int k = Mth.floor(aABB.min(axis2) - 1.0E-7) - 1;
			int l = Mth.floor(aABB.max(axis2) + 1.0E-7) + 1;
			double e = aABB.min(axis3) - 1.0E-7;
			double f = aABB.max(axis3) + 1.0E-7;
			boolean bl = d > 0.0;
			int m = bl ? Mth.floor(aABB.max(axis3) - 1.0E-7) - 1 : Mth.floor(aABB.min(axis3) + 1.0E-7) + 1;
			int n = lastC(d, e, f);
			int o = bl ? 1 : -1;

			for (int p = m; bl ? p <= n : p >= n; p += o) {
				for (int q = i; q <= j; q++) {
					for (int r = k; r <= l; r++) {
						int s = 0;
						if (q == i || q == j) {
							s++;
						}

						if (r == k || r == l) {
							s++;
						}

						if (p == m || p == n) {
							s++;
						}

						if (s < 3) {
							mutableBlockPos.set(axisCycle2, q, r, p);
							BlockState blockState = levelReader.getBlockState(mutableBlockPos);
							if ((s != 1 || blockState.hasLargeCollisionShape()) && (s != 2 || blockState.is(Blocks.MOVING_PISTON))) {
								d = blockState.getCollisionShape(levelReader, mutableBlockPos, collisionContext)
									.collide(axis3, aABB.move((double)(-mutableBlockPos.getX()), (double)(-mutableBlockPos.getY()), (double)(-mutableBlockPos.getZ())), d);
								if (Math.abs(d) < 1.0E-7) {
									return 0.0;
								}

								n = lastC(d, e, f);
							}
						}
					}
				}
			}

			double[] ds = new double[]{d};
			stream.forEach(voxelShape -> ds[0] = voxelShape.collide(axis3, aABB, ds[0]));
			return ds[0];
		}
	}

	private static int lastC(double d, double e, double f) {
		return d > 0.0 ? Mth.floor(f + d) + 1 : Mth.floor(e + d) - 1;
	}

	public static boolean blockOccudes(VoxelShape voxelShape, VoxelShape voxelShape2, Direction direction) {
		if (voxelShape == block() && voxelShape2 == block()) {
			return true;
		} else if (voxelShape2.isEmpty()) {
			return false;
		} else {
			Direction.Axis axis = direction.getAxis();
			Direction.AxisDirection axisDirection = direction.getAxisDirection();
			VoxelShape voxelShape3 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape : voxelShape2;
			VoxelShape voxelShape4 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape2 : voxelShape;
			BooleanOp booleanOp = axisDirection == Direction.AxisDirection.POSITIVE ? BooleanOp.ONLY_FIRST : BooleanOp.ONLY_SECOND;
			return DoubleMath.fuzzyEquals(voxelShape3.max(axis), 1.0, 1.0E-7)
				&& DoubleMath.fuzzyEquals(voxelShape4.min(axis), 0.0, 1.0E-7)
				&& !joinIsNotEmpty(new SliceShape(voxelShape3, axis, voxelShape3.shape.getSize(axis) - 1), new SliceShape(voxelShape4, axis, 0), booleanOp);
		}
	}

	public static VoxelShape getFaceShape(VoxelShape voxelShape, Direction direction) {
		if (voxelShape == block()) {
			return block();
		} else {
			Direction.Axis axis = direction.getAxis();
			boolean bl;
			int i;
			if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
				bl = DoubleMath.fuzzyEquals(voxelShape.max(axis), 1.0, 1.0E-7);
				i = voxelShape.shape.getSize(axis) - 1;
			} else {
				bl = DoubleMath.fuzzyEquals(voxelShape.min(axis), 0.0, 1.0E-7);
				i = 0;
			}

			return (VoxelShape)(!bl ? empty() : new SliceShape(voxelShape, axis, i));
		}
	}

	public static boolean mergedFaceOccludes(VoxelShape voxelShape, VoxelShape voxelShape2, Direction direction) {
		if (voxelShape != block() && voxelShape2 != block()) {
			Direction.Axis axis = direction.getAxis();
			Direction.AxisDirection axisDirection = direction.getAxisDirection();
			VoxelShape voxelShape3 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape : voxelShape2;
			VoxelShape voxelShape4 = axisDirection == Direction.AxisDirection.POSITIVE ? voxelShape2 : voxelShape;
			if (!DoubleMath.fuzzyEquals(voxelShape3.max(axis), 1.0, 1.0E-7)) {
				voxelShape3 = empty();
			}

			if (!DoubleMath.fuzzyEquals(voxelShape4.min(axis), 0.0, 1.0E-7)) {
				voxelShape4 = empty();
			}

			return !joinIsNotEmpty(
				block(),
				joinUnoptimized(new SliceShape(voxelShape3, axis, voxelShape3.shape.getSize(axis) - 1), new SliceShape(voxelShape4, axis, 0), BooleanOp.OR),
				BooleanOp.ONLY_FIRST
			);
		} else {
			return true;
		}
	}

	public static boolean faceShapeOccludes(VoxelShape voxelShape, VoxelShape voxelShape2) {
		if (voxelShape == block() || voxelShape2 == block()) {
			return true;
		} else {
			return voxelShape.isEmpty() && voxelShape2.isEmpty()
				? false
				: !joinIsNotEmpty(block(), joinUnoptimized(voxelShape, voxelShape2, BooleanOp.OR), BooleanOp.ONLY_FIRST);
		}
	}

	@VisibleForTesting
	protected static IndexMerger createIndexMerger(int i, DoubleList doubleList, DoubleList doubleList2, boolean bl, boolean bl2) {
		int j = doubleList.size() - 1;
		int k = doubleList2.size() - 1;
		if (doubleList instanceof CubePointRange && doubleList2 instanceof CubePointRange) {
			long l = lcm(j, k);
			if ((long)i * l <= 256L) {
				return new DiscreteCubeMerger(j, k);
			}
		}

		if (doubleList.getDouble(j) < doubleList2.getDouble(0) - 1.0E-7) {
			return new NonOverlappingMerger(doubleList, doubleList2, false);
		} else if (doubleList2.getDouble(k) < doubleList.getDouble(0) - 1.0E-7) {
			return new NonOverlappingMerger(doubleList2, doubleList, true);
		} else {
			return (IndexMerger)(j == k && Objects.equals(doubleList, doubleList2)
				? new IdenticalMerger(doubleList)
				: new IndirectMerger(doubleList, doubleList2, bl, bl2));
		}
	}

	public interface DoubleLineConsumer {
		void consume(double d, double e, double f, double g, double h, double i);
	}
}
