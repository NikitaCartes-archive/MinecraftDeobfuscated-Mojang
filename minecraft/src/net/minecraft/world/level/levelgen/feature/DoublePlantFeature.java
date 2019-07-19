package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class DoublePlantFeature extends Feature<DoublePlantConfiguration> {
	public DoublePlantFeature(Function<Dynamic<?>, ? extends DoublePlantConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		DoublePlantConfiguration doublePlantConfiguration
	) {
		boolean bl = false;

		for (int i = 0; i < 64; i++) {
			BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
			if (levelAccessor.isEmptyBlock(blockPos2) && blockPos2.getY() < 254 && doublePlantConfiguration.state.canSurvive(levelAccessor, blockPos2)) {
				((DoublePlantBlock)doublePlantConfiguration.state.getBlock()).placeAt(levelAccessor, blockPos2, 2);
				bl = true;
			}
		}

		return bl;
	}
}
