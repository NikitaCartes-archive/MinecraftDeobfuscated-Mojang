package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class BaseDiskFeature extends Feature<DiskConfiguration> {
	public BaseDiskFeature(Codec<DiskConfiguration> codec) {
		super(codec);
	}

	public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DiskConfiguration diskConfiguration) {
		boolean bl = false;
		int i = diskConfiguration.radius.sample(random);

		for (int j = blockPos.getX() - i; j <= blockPos.getX() + i; j++) {
			for (int k = blockPos.getZ() - i; k <= blockPos.getZ() + i; k++) {
				int l = j - blockPos.getX();
				int m = k - blockPos.getZ();
				if (l * l + m * m <= i * i) {
					for (int n = blockPos.getY() - diskConfiguration.halfHeight; n <= blockPos.getY() + diskConfiguration.halfHeight; n++) {
						BlockPos blockPos2 = new BlockPos(j, n, k);
						Block block = worldGenLevel.getBlockState(blockPos2).getBlock();

						for (BlockState blockState : diskConfiguration.targets) {
							if (blockState.is(block)) {
								worldGenLevel.setBlock(blockPos2, diskConfiguration.state, 2);
								bl = true;
								break;
							}
						}
					}
				}
			}
		}

		return bl;
	}
}
