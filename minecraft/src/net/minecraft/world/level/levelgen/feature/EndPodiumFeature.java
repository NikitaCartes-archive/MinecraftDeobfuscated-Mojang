package net.minecraft.world.level.levelgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndPodiumFeature extends Feature<NoneFeatureConfiguration> {
	public static final int PODIUM_RADIUS = 4;
	public static final int PODIUM_PILLAR_HEIGHT = 4;
	public static final int RIM_RADIUS = 1;
	public static final float CORNER_ROUNDING = 0.5F;
	public static final BlockPos END_PODIUM_LOCATION = BlockPos.ZERO;
	private final boolean active;

	public EndPodiumFeature(boolean bl) {
		super(NoneFeatureConfiguration.CODEC);
		this.active = bl;
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();

		for (BlockPos blockPos2 : BlockPos.betweenClosed(
			new BlockPos(blockPos.getX() - 4, blockPos.getY() - 1, blockPos.getZ() - 4), new BlockPos(blockPos.getX() + 4, blockPos.getY() + 32, blockPos.getZ() + 4)
		)) {
			boolean bl = blockPos2.closerThan(blockPos, 2.5);
			if (bl || blockPos2.closerThan(blockPos, 3.5)) {
				if (blockPos2.getY() < blockPos.getY()) {
					if (bl) {
						this.setBlock(worldGenLevel, blockPos2, Blocks.BEDROCK.defaultBlockState());
					} else if (blockPos2.getY() < blockPos.getY()) {
						this.setBlock(worldGenLevel, blockPos2, Blocks.END_STONE.defaultBlockState());
					}
				} else if (blockPos2.getY() > blockPos.getY()) {
					this.setBlock(worldGenLevel, blockPos2, Blocks.AIR.defaultBlockState());
				} else if (!bl) {
					this.setBlock(worldGenLevel, blockPos2, Blocks.BEDROCK.defaultBlockState());
				} else if (this.active) {
					this.setBlock(worldGenLevel, new BlockPos(blockPos2), Blocks.END_PORTAL.defaultBlockState());
				} else {
					this.setBlock(worldGenLevel, new BlockPos(blockPos2), Blocks.AIR.defaultBlockState());
				}
			}
		}

		for (int i = 0; i < 4; i++) {
			this.setBlock(worldGenLevel, blockPos.above(i), Blocks.BEDROCK.defaultBlockState());
		}

		BlockPos blockPos3 = blockPos.above(2);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			this.setBlock(worldGenLevel, blockPos3.relative(direction), Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, direction));
		}

		return true;
	}
}
