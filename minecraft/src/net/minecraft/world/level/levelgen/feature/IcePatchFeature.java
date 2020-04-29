package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureRadiusConfiguration;

public class IcePatchFeature extends Feature<FeatureRadiusConfiguration> {
	private final Block block = Blocks.PACKED_ICE;

	public IcePatchFeature(Function<Dynamic<?>, ? extends FeatureRadiusConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		FeatureRadiusConfiguration featureRadiusConfiguration
	) {
		while (levelAccessor.isEmptyBlock(blockPos) && blockPos.getY() > 2) {
			blockPos = blockPos.below();
		}

		if (!levelAccessor.getBlockState(blockPos).is(Blocks.SNOW_BLOCK)) {
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
							Block block = levelAccessor.getBlockState(blockPos2).getBlock();
							if (isDirt(block) || block == Blocks.SNOW_BLOCK || block == Blocks.ICE) {
								levelAccessor.setBlock(blockPos2, this.block.defaultBlockState(), 2);
							}
						}
					}
				}
			}

			return true;
		}
	}
}
