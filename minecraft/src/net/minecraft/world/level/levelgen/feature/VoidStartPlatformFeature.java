package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VoidStartPlatformFeature extends Feature<NoneFeatureConfiguration> {
	private static final BlockPos PLATFORM_ORIGIN = new BlockPos(8, 3, 8);
	private static final ChunkPos PLATFORM_ORIGIN_CHUNK = new ChunkPos(PLATFORM_ORIGIN);

	public VoidStartPlatformFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	private static int checkerboardDistance(int i, int j, int k, int l) {
		return Math.max(Math.abs(i - k), Math.abs(j - l));
	}

	public boolean place(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration
	) {
		ChunkPos chunkPos = new ChunkPos(blockPos);
		if (checkerboardDistance(chunkPos.x, chunkPos.z, PLATFORM_ORIGIN_CHUNK.x, PLATFORM_ORIGIN_CHUNK.z) > 1) {
			return true;
		} else {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = chunkPos.getMinBlockZ(); i <= chunkPos.getMaxBlockZ(); i++) {
				for (int j = chunkPos.getMinBlockX(); j <= chunkPos.getMaxBlockX(); j++) {
					if (checkerboardDistance(PLATFORM_ORIGIN.getX(), PLATFORM_ORIGIN.getZ(), j, i) <= 16) {
						mutableBlockPos.set(j, PLATFORM_ORIGIN.getY(), i);
						if (mutableBlockPos.equals(PLATFORM_ORIGIN)) {
							worldGenLevel.setBlock(mutableBlockPos, Blocks.COBBLESTONE.defaultBlockState(), 2);
						} else {
							worldGenLevel.setBlock(mutableBlockPos, Blocks.STONE.defaultBlockState(), 2);
						}
					}
				}
			}

			return true;
		}
	}
}
