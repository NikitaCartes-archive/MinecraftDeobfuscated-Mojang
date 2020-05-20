package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureRadiusConfiguration;

public class IcePatchFeature extends Feature<FeatureRadiusConfiguration> {
	private final Block block = Blocks.PACKED_ICE;

	public IcePatchFeature(Codec<FeatureRadiusConfiguration> codec) {
		super(codec);
	}

	public boolean place(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BlockPos blockPos,
		FeatureRadiusConfiguration featureRadiusConfiguration
	) {
		while (worldGenLevel.isEmptyBlock(blockPos) && blockPos.getY() > 2) {
			blockPos = blockPos.below();
		}

		if (!worldGenLevel.getBlockState(blockPos).is(Blocks.SNOW_BLOCK)) {
			return false;
		} else {
			int i = random.nextInt(featureRadiusConfiguration.radius) + 2;
			int j = 1;

			for (int k = blockPos.getX() - i; k <= blockPos.getX() + i; k++) {
				for (int l = blockPos.getZ() - i; l <= blockPos.getZ() + i; l++) {
					int m = k - blockPos.getX();
					int n = l - blockPos.getZ();
					if (m * m + n * n <= i * i) {
						for (int o = blockPos.getY() - 1; o <= blockPos.getY() + 1; o++) {
							BlockPos blockPos2 = new BlockPos(k, o, l);
							Block block = worldGenLevel.getBlockState(blockPos2).getBlock();
							if (isDirt(block) || block == Blocks.SNOW_BLOCK || block == Blocks.ICE) {
								worldGenLevel.setBlock(blockPos2, this.block.defaultBlockState(), 2);
							}
						}
					}
				}
			}

			return true;
		}
	}
}
