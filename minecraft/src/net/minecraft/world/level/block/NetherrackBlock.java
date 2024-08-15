package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NetherrackBlock extends Block implements BonemealableBlock {
	public static final MapCodec<NetherrackBlock> CODEC = simpleCodec(NetherrackBlock::new);

	@Override
	public MapCodec<NetherrackBlock> codec() {
		return CODEC;
	}

	public NetherrackBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		if (!levelReader.getBlockState(blockPos.above()).propagatesSkylightDown()) {
			return false;
		} else {
			for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1))) {
				if (levelReader.getBlockState(blockPos2).is(BlockTags.NYLIUM)) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		boolean bl = false;
		boolean bl2 = false;

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1))) {
			BlockState blockState2 = serverLevel.getBlockState(blockPos2);
			if (blockState2.is(Blocks.WARPED_NYLIUM)) {
				bl2 = true;
			}

			if (blockState2.is(Blocks.CRIMSON_NYLIUM)) {
				bl = true;
			}

			if (bl2 && bl) {
				break;
			}
		}

		if (bl2 && bl) {
			serverLevel.setBlock(blockPos, randomSource.nextBoolean() ? Blocks.WARPED_NYLIUM.defaultBlockState() : Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
		} else if (bl2) {
			serverLevel.setBlock(blockPos, Blocks.WARPED_NYLIUM.defaultBlockState(), 3);
		} else if (bl) {
			serverLevel.setBlock(blockPos, Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
		}
	}

	@Override
	public BonemealableBlock.Type getType() {
		return BonemealableBlock.Type.NEIGHBOR_SPREADER;
	}
}
