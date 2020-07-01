package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EndPodiumFeature extends Feature<NoneFeatureConfiguration> {
	public static final BlockPos END_PODIUM_LOCATION = BlockPos.ZERO;
	private final boolean active;

	public EndPodiumFeature(boolean bl) {
		super(NoneFeatureConfiguration.CODEC);
		this.active = bl;
	}

	public boolean place(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration
	) {
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
