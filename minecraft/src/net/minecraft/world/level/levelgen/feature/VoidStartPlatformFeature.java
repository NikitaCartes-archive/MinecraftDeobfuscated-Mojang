package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VoidStartPlatformFeature extends Feature<NoneFeatureConfiguration> {
	private static final BlockPos PLATFORM_OFFSET = new BlockPos(8, 3, 8);
	private static final ChunkPos PLATFORM_ORIGIN_CHUNK = new ChunkPos(PLATFORM_OFFSET);
	private static final int PLATFORM_RADIUS = 16;
	private static final int PLATFORM_RADIUS_CHUNKS = 1;

	public VoidStartPlatformFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	private static int checkerboardDistance(int i, int j, int k, int l) {
		return Math.max(Math.abs(i - k), Math.abs(j - l));
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		ChunkPos chunkPos = new ChunkPos(featurePlaceContext.origin());
		if (checkerboardDistance(chunkPos.x, chunkPos.z, PLATFORM_ORIGIN_CHUNK.x, PLATFORM_ORIGIN_CHUNK.z) > 1) {
			return true;
		} else {
			BlockPos blockPos = PLATFORM_OFFSET.atY(featurePlaceContext.origin().getY() + PLATFORM_OFFSET.getY());
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = chunkPos.getMinBlockZ(); i <= chunkPos.getMaxBlockZ(); i++) {
				for (int j = chunkPos.getMinBlockX(); j <= chunkPos.getMaxBlockX(); j++) {
					if (checkerboardDistance(blockPos.getX(), blockPos.getZ(), j, i) <= 16) {
						mutableBlockPos.set(j, blockPos.getY(), i);
						if (mutableBlockPos.equals(blockPos)) {
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
