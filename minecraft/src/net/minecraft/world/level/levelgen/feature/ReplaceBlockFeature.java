package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
	public ReplaceBlockFeature(Function<Dynamic<?>, ? extends ReplaceBlockConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		ReplaceBlockConfiguration replaceBlockConfiguration
	) {
		if (levelAccessor.getBlockState(blockPos).getBlock() == replaceBlockConfiguration.target.getBlock()) {
			levelAccessor.setBlock(blockPos, replaceBlockConfiguration.state, 2);
		}

		return true;
	}
}
