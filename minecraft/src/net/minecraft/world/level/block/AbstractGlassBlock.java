package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractGlassBlock extends HalfTransparentBlock {
	protected AbstractGlassBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected abstract MapCodec<? extends AbstractGlassBlock> codec();

	@Override
	public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Shapes.empty();
	}

	@Override
	public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return 1.0F;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return true;
	}
}
