package net.minecraft.world.level.block;

import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FungiBlock extends BushBlock implements BonemealableBlock {
	protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 9.0, 12.0);
	private final Supplier<ConfiguredFeature<?, ?>> feature;

	protected FungiBlock(Block.Properties properties, Supplier<ConfiguredFeature<?, ?>> supplier) {
		super(properties);
		this.feature = supplier;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		Block block = blockState.getBlock();
		return blockState.is(BlockTags.NYLIUM) || block == Blocks.SOUL_SOIL || super.mayPlaceOn(blockState, blockGetter, blockPos);
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return (double)random.nextFloat() < 0.4;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		((ConfiguredFeature)this.feature.get())
			.place(serverLevel, (ChunkGenerator<? extends ChunkGeneratorSettings>)serverLevel.getChunkSource().getGenerator(), random, blockPos);
	}
}
