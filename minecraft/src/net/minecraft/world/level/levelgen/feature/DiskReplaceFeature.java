package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature extends BaseDiskFeature {
	public DiskReplaceFeature(Codec<DiskConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DiskConfiguration diskConfiguration) {
		return !worldGenLevel.getFluidState(blockPos).is(FluidTags.WATER) ? false : super.place(worldGenLevel, chunkGenerator, random, blockPos, diskConfiguration);
	}
}
