package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
	public ReplaceBlockFeature(Function<Dynamic<?>, ? extends ReplaceBlockConfiguration> function) {
		super(function);
	}

	public boolean place(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BlockPos blockPos,
		ReplaceBlockConfiguration replaceBlockConfiguration
	) {
		if (worldGenLevel.getBlockState(blockPos).is(replaceBlockConfiguration.target.getBlock())) {
			worldGenLevel.setBlock(blockPos, replaceBlockConfiguration.state, 2);
		}

		return true;
	}
}
