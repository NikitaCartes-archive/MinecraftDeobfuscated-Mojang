package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PotatoSproutsBlock extends BushBlock implements BonemealableBlock {
	public static final MapCodec<PotatoSproutsBlock> CODEC = simpleCodec(PotatoSproutsBlock::new);
	protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

	@Override
	public MapCodec<PotatoSproutsBlock> codec() {
		return CODEC;
	}

	public PotatoSproutsBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
		return SHAPE.move(vec3.x, vec3.y, vec3.z);
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(Blocks.CORRUPTED_PEELGRASS_BLOCK) || blockState.is(Blocks.PEELGRASS_BLOCK);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return (double)level.random.nextFloat() < 0.25;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		TreeGrower.POTATO.growTree(serverLevel, serverLevel.getChunkSource().getGenerator(), blockPos, blockState, randomSource);
	}
}
