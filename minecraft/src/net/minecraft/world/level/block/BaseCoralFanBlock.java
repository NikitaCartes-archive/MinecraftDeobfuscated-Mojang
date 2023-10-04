package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BaseCoralFanBlock extends BaseCoralPlantTypeBlock {
	public static final MapCodec<BaseCoralFanBlock> CODEC = simpleCodec(BaseCoralFanBlock::new);
	private static final VoxelShape AABB = Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);

	@Override
	public MapCodec<? extends BaseCoralFanBlock> codec() {
		return CODEC;
	}

	protected BaseCoralFanBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return AABB;
	}
}
