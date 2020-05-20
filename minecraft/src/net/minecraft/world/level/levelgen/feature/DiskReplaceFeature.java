package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature extends Feature<DiskConfiguration> {
	public DiskReplaceFeature(Codec<DiskConfiguration> codec) {
		super(codec);
	}

	public boolean place(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BlockPos blockPos,
		DiskConfiguration diskConfiguration
	) {
		if (!worldGenLevel.getFluidState(blockPos).is(FluidTags.WATER)) {
			return false;
		} else {
			int i = 0;
			int j = random.nextInt(diskConfiguration.radius - 2) + 2;

			for (int k = blockPos.getX() - j; k <= blockPos.getX() + j; k++) {
				for (int l = blockPos.getZ() - j; l <= blockPos.getZ() + j; l++) {
					int m = k - blockPos.getX();
					int n = l - blockPos.getZ();
					if (m * m + n * n <= j * j) {
						for (int o = blockPos.getY() - diskConfiguration.ySize; o <= blockPos.getY() + diskConfiguration.ySize; o++) {
							BlockPos blockPos2 = new BlockPos(k, o, l);
							BlockState blockState = worldGenLevel.getBlockState(blockPos2);

							for (BlockState blockState2 : diskConfiguration.targets) {
								if (blockState2.is(blockState.getBlock())) {
									worldGenLevel.setBlock(blockPos2, diskConfiguration.state, 2);
									i++;
									break;
								}
							}
						}
					}
				}
			}

			return i > 0;
		}
	}
}
