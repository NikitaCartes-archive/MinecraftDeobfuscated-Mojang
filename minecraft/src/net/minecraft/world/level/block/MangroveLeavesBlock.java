package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MangroveLeavesBlock extends LeavesBlock implements BonemealableBlock {
	public static final MapCodec<MangroveLeavesBlock> CODEC = simpleCodec(MangroveLeavesBlock::new);

	@Override
	public MapCodec<MangroveLeavesBlock> codec() {
		return CODEC;
	}

	public MangroveLeavesBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return levelReader.getBlockState(blockPos.below()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		serverLevel.setBlock(blockPos.below(), MangrovePropaguleBlock.createNewHangingPropagule(), 2);
	}

	@Override
	public BlockPos getParticlePos(BlockPos blockPos) {
		return blockPos.below();
	}
}
