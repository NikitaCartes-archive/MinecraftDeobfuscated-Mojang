package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndIslandFeature extends Feature<NoneFeatureConfiguration> {
	public EndIslandFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		RandomSource randomSource = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		float f = (float)randomSource.nextInt(3) + 4.0F;

		for (int i = 0; f > 0.5F; i--) {
			for (int j = Mth.floor(-f); j <= Mth.ceil(f); j++) {
				for (int k = Mth.floor(-f); k <= Mth.ceil(f); k++) {
					if ((float)(j * j + k * k) <= (f + 1.0F) * (f + 1.0F)) {
						this.setBlock(worldGenLevel, blockPos.offset(j, i, k), Blocks.END_STONE.defaultBlockState());
					}
				}
			}

			f -= (float)randomSource.nextInt(2) + 0.5F;
		}

		return true;
	}
}
