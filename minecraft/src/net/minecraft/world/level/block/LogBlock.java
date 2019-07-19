package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;

public class LogBlock extends RotatedPillarBlock {
	private final MaterialColor woodMaterialColor;

	public LogBlock(MaterialColor materialColor, Block.Properties properties) {
		super(properties);
		this.woodMaterialColor = materialColor;
	}

	@Override
	public MaterialColor getMapColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.getValue(AXIS) == Direction.Axis.Y ? this.woodMaterialColor : this.materialColor;
	}
}
