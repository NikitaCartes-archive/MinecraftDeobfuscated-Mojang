package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;

public class EndGatewayFeature extends Feature<EndGatewayConfiguration> {
	public EndGatewayFeature(Codec<EndGatewayConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<EndGatewayConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		EndGatewayConfiguration endGatewayConfiguration = featurePlaceContext.config();

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-1, -2, -1), blockPos.offset(1, 2, 1))) {
			boolean bl = blockPos2.getX() == blockPos.getX();
			boolean bl2 = blockPos2.getY() == blockPos.getY();
			boolean bl3 = blockPos2.getZ() == blockPos.getZ();
			boolean bl4 = Math.abs(blockPos2.getY() - blockPos.getY()) == 2;
			if (bl && bl2 && bl3) {
				BlockPos blockPos3 = blockPos2.immutable();
				this.setBlock(worldGenLevel, blockPos3, Blocks.END_GATEWAY.defaultBlockState());
				endGatewayConfiguration.getExit().ifPresent(blockPos2x -> {
					if (worldGenLevel.getBlockEntity(blockPos3) instanceof TheEndGatewayBlockEntity theEndGatewayBlockEntity) {
						theEndGatewayBlockEntity.setExitPosition(blockPos2x, endGatewayConfiguration.isExitExact());
					}
				});
			} else if (bl2) {
				this.setBlock(worldGenLevel, blockPos2, Blocks.AIR.defaultBlockState());
			} else if (bl4 && bl && bl3) {
				this.setBlock(worldGenLevel, blockPos2, Blocks.BEDROCK.defaultBlockState());
			} else if ((bl || bl3) && !bl4) {
				this.setBlock(worldGenLevel, blockPos2, Blocks.BEDROCK.defaultBlockState());
			} else {
				this.setBlock(worldGenLevel, blockPos2, Blocks.AIR.defaultBlockState());
			}
		}

		return true;
	}
}
