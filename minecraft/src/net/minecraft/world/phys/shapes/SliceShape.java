package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;

public class SliceShape extends VoxelShape {
	private final VoxelShape delegate;
	private final Direction.Axis axis;
	private static final DoubleList SLICE_COORDS = new CubePointRange(1);

	public SliceShape(VoxelShape voxelShape, Direction.Axis axis, int i) {
		super(makeSlice(voxelShape.shape, axis, i));
		this.delegate = voxelShape;
		this.axis = axis;
	}

	private static DiscreteVoxelShape makeSlice(DiscreteVoxelShape discreteVoxelShape, Direction.Axis axis, int i) {
		return new SubShape(
			discreteVoxelShape,
			axis.choose(i, 0, 0),
			axis.choose(0, i, 0),
			axis.choose(0, 0, i),
			axis.choose(i + 1, discreteVoxelShape.xSize, discreteVoxelShape.xSize),
			axis.choose(discreteVoxelShape.ySize, i + 1, discreteVoxelShape.ySize),
			axis.choose(discreteVoxelShape.zSize, discreteVoxelShape.zSize, i + 1)
		);
	}

	@Override
	public DoubleList getCoords(Direction.Axis axis) {
		return axis == this.axis ? SLICE_COORDS : this.delegate.getCoords(axis);
	}
}
