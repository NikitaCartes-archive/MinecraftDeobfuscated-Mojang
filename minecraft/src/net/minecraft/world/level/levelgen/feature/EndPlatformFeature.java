package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndPlatformFeature extends Feature<NoneFeatureConfiguration> {
	public EndPlatformFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		createEndPlatform(featurePlaceContext.level(), featurePlaceContext.origin(), false);
		return true;
	}

	public static void createEndPlatform(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, boolean bl) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				for (int k = -1; k < 3; k++) {
					BlockPos blockPos2 = mutableBlockPos.set(blockPos).move(j, k, i);
					Block block = k == -1 ? Blocks.OBSIDIAN : Blocks.AIR;
					if (!serverLevelAccessor.getBlockState(blockPos2).is(block)) {
						if (bl) {
							serverLevelAccessor.destroyBlock(blockPos2, true, null);
						}

						serverLevelAccessor.setBlock(blockPos2, block.defaultBlockState(), 3);
					}
				}
			}
		}
	}
}
