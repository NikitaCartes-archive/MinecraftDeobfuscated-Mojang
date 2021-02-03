package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
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
		Random random = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		float f = (float)(random.nextInt(3) + 4);

		for (int i = 0; f > 0.5F; i--) {
			for (int j = Mth.floor(-f); j <= Mth.ceil(f); j++) {
				for (int k = Mth.floor(-f); k <= Mth.ceil(f); k++) {
					if ((float)(j * j + k * k) <= (f + 1.0F) * (f + 1.0F)) {
						this.setBlock(worldGenLevel, blockPos.offset(j, i, k), Blocks.END_STONE.defaultBlockState());
					}
				}
			}

			f = (float)((double)f - ((double)random.nextInt(2) + 0.5));
		}

		return true;
	}
}
