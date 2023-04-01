package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CraterFeatureConfiguration;

public class CraterFeature extends Feature<CraterFeatureConfiguration> {
	public CraterFeature(Codec<CraterFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<CraterFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = worldGenLevel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, featurePlaceContext.origin()).below();
		RandomSource randomSource = featurePlaceContext.random();
		CraterFeatureConfiguration craterFeatureConfiguration = featurePlaceContext.config();
		int i = craterFeatureConfiguration.radius().sample(randomSource);
		int j = craterFeatureConfiguration.depth().sample(randomSource);
		if (j > i) {
			return false;
		} else {
			int k = (j * j + i * i) / (2 * j);
			BlockPos blockPos2 = blockPos.above(k - j);
			BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
			Consumer<LevelAccessor> consumer = levelAccessor -> {
				for (int kx = -j; kx <= k; kx++) {
					boolean bl = false;

					for (int l = -k; l <= k; l++) {
						for (int m = -k; m <= k; m++) {
							mutableBlockPos.setWithOffset(blockPos, l, kx, m);
							if (mutableBlockPos.distSqr(blockPos2) < (double)(k * k) && !levelAccessor.getBlockState(mutableBlockPos).isAir()) {
								bl = true;
								levelAccessor.setBlock(mutableBlockPos, Blocks.AIR.defaultBlockState(), 3);
							}
						}
					}

					if (!bl && kx > 0) {
						break;
					}
				}
			};
			if (k < 15) {
				consumer.accept(worldGenLevel);
			} else {
				ServerLevel serverLevel = worldGenLevel.getLevel();
				serverLevel.getServer().execute(() -> consumer.accept(serverLevel));
			}

			return true;
		}
	}
}
