package net.minecraft.world.phys.shapes;

import net.minecraft.core.Direction;

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
	public void setFull(int i, int j, int k, boolean bl, boolean bl2) {
		this.parent.setFull(this.startX + i, this.startY + j, this.startZ + k, bl, bl2);
	}

	@Override
	public int firstFull(Direction.Axis axis) {
		return Math.max(0, this.parent.firstFull(axis) - axis.choose(this.startX, this.startY, this.startZ));
	}

	@Override
	public int lastFull(Direction.Axis axis) {
		return Math.min(axis.choose(this.endX, this.endY, this.endZ), this.parent.lastFull(axis) - axis.choose(this.startX, this.startY, this.startZ));
	}
}
