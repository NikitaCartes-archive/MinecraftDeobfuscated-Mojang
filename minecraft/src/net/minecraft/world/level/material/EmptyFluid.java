package net.minecraft.world.level.material;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EmptyFluid extends Fluid {
	@Override
	public Item getBucket() {
		return Items.AIR;
	}

	@Override
	public boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
		return true;
	}

	@Override
	public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos, FluidState fluidState) {
		return Vec3.ZERO;
	}

	@Override
	public int getTickDelay(LevelReader levelReader) {
		return 0;
	}

	@Override
	protected boolean isEmpty() {
		return true;
	}

	@Override
	protected float getExplosionResistance() {
		return 0.0F;
	}

	@Override
	public float getHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
		return 0.0F;
	}

	@Override
	public float getOwnHeight(FluidState fluidState) {
		return 0.0F;
	}

	@Override
	protected BlockState createLegacyBlock(FluidState fluidState) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean isSource(FluidState fluidState) {
		return false;
	}

	@Override
	public int getAmount(FluidState fluidState) {
		return 0;
	}

	@Override
	public VoxelShape getShape(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
		return Shapes.empty();
	}
}
