package net.minecraft.world.phys.shapes;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class SubShape extends DiscreteVoxelShape {
	private final DiscreteVoxelShape parent;
	private final int startX;
	private final int startY;
	private final int startZ;
	private final int endX;
	private final int endY;
	private final int endZ;

	protected SubShape(DiscreteVoxelShape discreteVoxelShape, int i, int j, int k, int l, int m, int n) {
		super(l - i, m - j, n - k);
		this.parent = discreteVoxelShape;
		this.startX = i;
		this.startY = j;
		this.startZ = k;
		this.endX = l;
		this.endY = m;
		this.endZ = n;
	}

	@Override
	public boolean isFull(int i, int j, int k) {
		return this.parent.isFull(this.startX + i, this.startY + j, this.startZ + k);
	}

	@Override
	public void fill(int i, int j, int k) {
		this.parent.fill(this.startX + i, this.startY + j, this.startZ + k);
	}

	@Override
	public int firstFull(Direction.Axis axis) {
		return this.clampToShape(axis, this.parent.firstFull(axis));
	}

	@Override
	public int lastFull(Direction.Axis axis) {
		return this.clampToShape(axis, this.parent.lastFull(axis));
	}

	private int clampToShape(Direction.Axis axis, int i) {
		int j = axis.choose(this.startX, this.startY, this.startZ);
		int k = axis.choose(this.endX, this.endY, this.endZ);
		return Mth.clamp(i, j, k) - j;
	}
}
