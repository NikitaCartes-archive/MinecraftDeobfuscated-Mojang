package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;

public class FillLayerFeature extends Feature<LayerConfiguration> {
	public FillLayerFeature(Codec<LayerConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<LayerConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		LayerConfiguration layerConfiguration = featurePlaceContext.config();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				int k = blockPos.getX() + i;
				int l = blockPos.getZ() + j;
				int m = worldGenLevel.getMinY() + layerConfiguration.height;
				mutableBlockPos.set(k, m, l);
				if (worldGenLevel.getBlockState(mutableBlockPos).isAir()) {
					worldGenLevel.setBlock(mutableBlockPos, layerConfiguration.state, 2);
				}
			}
		}

		return true;
	}
}
