package net.minecraft.world.phys.shapes;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class VoxelShape {
	protected final DiscreteVoxelShape shape;
	@Nullable
	private VoxelShape[] faces;

	VoxelShape(DiscreteVoxelShape discreteVoxelShape) {
		this.shape = discreteVoxelShape;
	}

	public double min(Direction.Axis axis) {
		int i = this.shape.firstFull(axis);
		return i >= this.shape.getSize(axis) ? Double.POSITIVE_INFINITY : this.get(axis, i);
	}

	public double max(Direction.Axis axis) {
		int i = this.shape.lastFull(axis);
		return i <= 0 ? Double.NEGATIVE_INFINITY : this.get(axis, i);
	}

	public AABB bounds() {
		if (this.isEmpty()) {
			throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("No bounds for empty shape."));
		} else {
			return new AABB(
				this.min(Direction.Axis.X),
				this.min(Direction.Axis.Y),
				this.min(Direction.Axis.Z),
				this.max(Direction.Axis.X),
				this.max(Direction.Axis.Y),
				this.max(Direction.Axis.Z)
			);
		}
	}

	protected double get(Direction.Axis axis, int i) {
		return this.getCoords(axis).getDouble(i);
	}

	protected abstract DoubleList getCoords(Direction.Axis axis);

	public boolean isEmpty() {
		return this.shape.isEmpty();
	}

	public VoxelShape move(double d, double e, double f) {
		return (VoxelShape)(this.isEmpty()
			? Shapes.empty()
			: new ArrayVoxelShape(
				this.shape,
				new OffsetDoubleList(this.getCoords(Direction.Axis.X), d),
				new OffsetDoubleList(this.getCoords(Direction.Axis.Y), e),
				new OffsetDoubleList(this.getCoords(Direction.Axis.Z), f)
			));
	}

	public VoxelShape optimize() {
		VoxelShape[] voxelShapes = new VoxelShape[]{Shapes.empty()};
		this.forAllBoxes((d, e, f, g, h, i) -> voxelShapes[0] = Shapes.joinUnoptimized(voxelShapes[0], Shapes.box(d, e, f, g, h, i), BooleanOp.OR));
		return voxelShapes[0];
	}

	@Environment(EnvType.CLIENT)
	public void forAllEdges(Shapes.DoubleLineConsumer doubleLineConsumer) {
		this.shape
			.forAllEdges(
				(i, j, k, l, m, n) -> doubleLineConsumer.consume(
						this.get(Direction.Axis.X, i),
						this.get(Direction.Axis.Y, j),
						this.get(Direction.Axis.Z, k),
						this.get(Direction.Axis.X, l),
						this.get(Direction.Axis.Y, m),
						this.get(Direction.Axis.Z, n)
					),
				true
			);
	}

	public void forAllBoxes(Shapes.DoubleLineConsumer doubleLineConsumer) {
		DoubleList doubleList = this.getCoords(Direction.Axis.X);
		DoubleList doubleList2 = this.getCoords(Direction.Axis.Y);
		DoubleList doubleList3 = this.getCoords(Direction.Axis.Z);
		this.shape
			.forAllBoxes(
				(i, j, k, l, m, n) -> doubleLineConsumer.consume(
						doubleList.getDouble(i), doubleList2.getDouble(j), doubleList3.getDouble(k), doubleList.getDouble(l), doubleList2.getDouble(m), doubleList3.getDouble(n)
					),
				true
			);
	}

	public List<AABB> toAabbs() {
		List<AABB> list = Lists.<AABB>newArrayList();
		this.forAllBoxes((d, e, f, g, h, i) -> list.add(new AABB(d, e, f, g, h, i)));
		return list;
	}

	@Environment(EnvType.CLIENT)
	public double max(Direction.Axis axis, double d, double e) {
		Direction.Axis axis2 = AxisCycle.FORWARD.cycle(axis);
		Direction.Axis axis3 = AxisCycle.BACKWARD.cycle(axis);
		int i = this.findIndex(axis2, d);
		int j = this.findIndex(axis3, e);
		int k = this.shape.lastFull(axis, i, j);
		return k <= 0 ? Double.NEGATIVE_INFINITY : this.get(axis, k);
	}

	protected int findIndex(Direction.Axis axis, double d) {
		return Mth.binarySearch(0, this.shape.getSize(axis) + 1, i -> d < this.get(axis, i)) - 1;
	}

	@Nullable
	public BlockHitResult clip(Vec3 vec3, Vec3 vec32, BlockPos blockPos) {
		if (this.isEmpty()) {
			return null;
		} else {
			Vec3 vec33 = vec32.subtract(vec3);
			if (vec33.lengthSqr() < 1.0E-7) {
				return null;
			} else {
				Vec3 vec34 = vec3.add(vec33.scale(0.001));
				return this.shape
						.isFullWide(
							this.findIndex(Direction.Axis.X, vec34.x - (double)blockPos.getX()),
							this.findIndex(Direction.Axis.Y, vec34.y - (double)blockPos.getY()),
							this.findIndex(Direction.Axis.Z, vec34.z - (double)blockPos.getZ())
						)
					? new BlockHitResult(vec34, Direction.getNearest(vec33.x, vec33.y, vec33.z).getOpposite(), blockPos, true)
					: AABB.clip(this.toAabbs(), vec3, vec32, blockPos);
			}
		}
	}

	public Optional<Vec3> closestPointTo(Vec3 vec3) {
		if (this.isEmpty()) {
			return Optional.empty();
		} else {
			Vec3[] vec3s = new Vec3[1];
			this.forAllBoxes((d, e, f, g, h, i) -> {
				double j = Mth.clamp(vec3.x(), d, g);
				double k = Mth.clamp(vec3.y(), e, h);
				double l = Mth.clamp(vec3.z(), f, i);
				if (vec3s[0] == null || vec3.distanceToSqr(j, k, l) < vec3.distanceToSqr(vec3s[0])) {
					vec3s[0] = new Vec3(j, k, l);
				}
			});
			return Optional.of(vec3s[0]);
		}
	}

	public VoxelShape getFaceShape(Direction direction) {
		if (!this.isEmpty() && this != Shapes.block()) {
			if (this.faces != null) {
				VoxelShape voxelShape = this.faces[direction.ordinal()];
				if (voxelShape != null) {
					return voxelShape;
				}
			} else {
				this.faces = new VoxelShape[6];
			}

			VoxelShape voxelShape = this.calculateFace(direction);
			this.faces[direction.ordinal()] = voxelShape;
			return voxelShape;
		} else {
			return this;
		}
	}

	private VoxelShape calculateFace(Direction direction) {
		Direction.Axis axis = direction.getAxis();
		DoubleList doubleList = this.getCoords(axis);
		if (doubleList.size() == 2 && DoubleMath.fuzzyEquals(doubleList.getDouble(0), 0.0, 1.0E-7) && DoubleMath.fuzzyEquals(doubleList.getDouble(1), 1.0, 1.0E-7)) {
			return this;
		} else {
			Direction.AxisDirection axisDirection = direction.getAxisDirection();
			int i = this.findIndex(axis, axisDirection == Direction.AxisDirection.POSITIVE ? 0.9999999 : 1.0E-7);
			return new SliceShape(this, axis, i);
		}
	}

	public double collide(Direction.Axis axis, AABB aABB, double d) {
		return this.collideX(AxisCycle.between(axis, Direction.Axis.X), aABB, d);
	}

	protected double collideX(AxisCycle axisCycle, AABB aABB, double d) {
		if (this.isEmpty()) {
			return d;
		} else if (Math.abs(d) < 1.0E-7) {
			return 0.0;
		} else {
			AxisCycle axisCycle2 = axisCycle.inverse();
			Direction.Axis axis = axisCycle2.cycle(Direction.Axis.X);
			Direction.Axis axis2 = axisCycle2.cycle(Direction.Axis.Y);
			Direction.Axis axis3 = axisCycle2.cycle(Direction.Axis.Z);
			double e = aABB.max(axis);
			double f = aABB.min(axis);
			int i = this.findIndex(axis, f + 1.0E-7);
			int j = this.findIndex(axis, e - 1.0E-7);
			int k = Math.max(0, this.findIndex(axis2, aABB.min(axis2) + 1.0E-7));
			int l = Math.min(this.shape.getSize(axis2), this.findIndex(axis2, aABB.max(axis2) - 1.0E-7) + 1);
			int m = Math.max(0, this.findIndex(axis3, aABB.min(axis3) + 1.0E-7));
			int n = Math.min(this.shape.getSize(axis3), this.findIndex(axis3, aABB.max(axis3) - 1.0E-7) + 1);
			int o = this.shape.getSize(axis);
			if (d > 0.0) {
				for (int p = j + 1; p < o; p++) {
					for (int q = k; q < l; q++) {
						for (int r = m; r < n; r++) {
							if (this.shape.isFullWide(axisCycle2, p, q, r)) {
								double g = this.get(axis, p) - e;
								if (g >= -1.0E-7) {
									d = Math.min(d, g);
								}

								return d;
							}
						}
					}
				}
			} else if (d < 0.0) {
				for (int p = i - 1; p >= 0; p--) {
					for (int q = k; q < l; q++) {
						for (int rx = m; rx < n; rx++) {
							if (this.shape.isFullWide(axisCycle2, p, q, rx)) {
								double g = this.get(axis, p + 1) - f;
								if (g <= 1.0E-7) {
									d = Math.max(d, g);
								}

								return d;
							}
						}
					}
				}
			}

			return d;
		}
	}

	public String toString() {
		return this.isEmpty() ? "EMPTY" : "VoxelShape[" + this.bounds() + "]";
	}
}
