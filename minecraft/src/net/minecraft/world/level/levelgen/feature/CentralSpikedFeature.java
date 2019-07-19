package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class CentralSpikedFeature extends Feature<NoneFeatureConfiguration> {
	protected final BlockState blockState;

	public CentralSpikedFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, BlockState blockState) {
		super(function);
		this.blockState = blockState;
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		int i = 0;

		for (int j = 0; j < 64; j++) {
			BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
			if (levelAccessor.isEmptyBlock(blockPos2) && levelAccessor.getBlockState(blockPos2.below()).getBlock() == Blocks.GRASS_BLOCK) {
				levelAccessor.setBlock(blockPos2, this.blockState, 2);
				i++;
			}
		}

		return i > 0;
	}
}
