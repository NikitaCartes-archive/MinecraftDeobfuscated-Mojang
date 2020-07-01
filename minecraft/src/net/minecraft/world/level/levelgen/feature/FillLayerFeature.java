package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;

public class FillLayerFeature extends Feature<LayerConfiguration> {
	public FillLayerFeature(Codec<LayerConfiguration> codec) {
		super(codec);
	}

	public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, LayerConfiguration layerConfiguration) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				int k = blockPos.getX() + i;
				int l = blockPos.getZ() + j;
				int m = layerConfiguration.height;
				mutableBlockPos.set(k, m, l);
				if (worldGenLevel.getBlockState(mutableBlockPos).isAir()) {
					worldGenLevel.setBlock(mutableBlockPos, layerConfiguration.state, 2);
				}
			}
		}

		return true;
	}
}
