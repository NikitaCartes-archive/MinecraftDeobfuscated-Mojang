package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherSproutsBlock extends BushBlock {
	public static final MapCodec<NetherSproutsBlock> CODEC = simpleCodec(NetherSproutsBlock::new);
	protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0);

	@Override
	public MapCodec<NetherSproutsBlock> codec() {
		return CODEC;
	}

	public NetherSproutsBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(BlockTags.NYLIUM) || blockState.is(Blocks.SOUL_SOIL) || super.mayPlaceOn(blockState, blockGetter, blockPos);
	}
}
