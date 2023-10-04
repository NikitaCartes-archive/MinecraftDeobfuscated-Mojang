package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TallFlowerBlock extends DoublePlantBlock implements BonemealableBlock {
	public static final MapCodec<TallFlowerBlock> CODEC = simpleCodec(TallFlowerBlock::new);

	@Override
	public MapCodec<TallFlowerBlock> codec() {
		return CODEC;
	}

	public TallFlowerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		popResource(serverLevel, blockPos, new ItemStack(this));
	}
}
