package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StructureVoidBlock extends Block {
	public static final MapCodec<StructureVoidBlock> CODEC = simpleCodec(StructureVoidBlock::new);
	private static final double SIZE = 5.0;
	private static final VoxelShape SHAPE = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

	@Override
	public MapCodec<StructureVoidBlock> codec() {
		return CODEC;
	}

	protected StructureVoidBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return 1.0F;
	}
}
