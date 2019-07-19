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

public class MelonFeature extends Feature<NoneFeatureConfiguration> {
	public MelonFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		for (int i = 0; i < 64; i++) {
			BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
			BlockState blockState = Blocks.MELON.defaultBlockState();
			if (levelAccessor.getBlockState(blockPos2).getMaterial().isReplaceable() && levelAccessor.getBlockState(blockPos2.below()).getBlock() == Blocks.GRASS_BLOCK) {
				levelAccessor.setBlock(blockPos2, blockState, 2);
			}
		}

		return true;
	}
}
