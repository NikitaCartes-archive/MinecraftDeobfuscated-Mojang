package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SnowAndFreezeFeature extends Feature<NoneFeatureConfiguration> {
	public SnowAndFreezeFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				int k = blockPos.getX() + i;
				int l = blockPos.getZ() + j;
				int m = worldGenLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
				mutableBlockPos.set(k, m, l);
				mutableBlockPos2.set(mutableBlockPos).move(Direction.DOWN, 1);
				Biome biome = worldGenLevel.getBiome(mutableBlockPos);
				if (biome.shouldFreeze(worldGenLevel, mutableBlockPos2, false)) {
					worldGenLevel.setBlock(mutableBlockPos2, Blocks.ICE.defaultBlockState(), 2);
				}

				if (biome.shouldSnow(worldGenLevel, mutableBlockPos)) {
					worldGenLevel.setBlock(mutableBlockPos, Blocks.SNOW.defaultBlockState(), 2);
					BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos2);
					if (blockState.hasProperty(SnowyDirtBlock.SNOWY)) {
						worldGenLevel.setBlock(mutableBlockPos2, blockState.setValue(SnowyDirtBlock.SNOWY, Boolean.valueOf(true)), 2);
					}
				}
			}
		}

		return true;
	}
}
