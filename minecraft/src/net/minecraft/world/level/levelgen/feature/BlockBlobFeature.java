package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class BlockBlobFeature extends Feature<BlockBlobConfiguration> {
	public BlockBlobFeature(Function<Dynamic<?>, ? extends BlockBlobConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		BlockBlobConfiguration blockBlobConfiguration
	) {
		while (blockPos.getY() > 3) {
			if (!levelAccessor.isEmptyBlock(blockPos.below())) {
				Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
				if (block == Blocks.GRASS_BLOCK || Block.equalsDirt(block) || Block.equalsStone(block)) {
					break;
				}
			}

			blockPos = blockPos.below();
		}

		if (blockPos.getY() <= 3) {
			return false;
		} else {
			int i = blockBlobConfiguration.startRadius;

			for (int j = 0; i >= 0 && j < 3; j++) {
				int k = i + random.nextInt(2);
				int l = i + random.nextInt(2);
				int m = i + random.nextInt(2);
				float f = (float)(k + l + m) * 0.333F + 0.5F;

				for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-k, -l, -m), blockPos.offset(k, l, m))) {
					if (blockPos2.distSqr(blockPos) <= (double)(f * f)) {
						levelAccessor.setBlock(blockPos2, blockBlobConfiguration.state, 4);
					}
				}

				blockPos = blockPos.offset(-(i + 1) + random.nextInt(2 + i * 2), 0 - random.nextInt(2), -(i + 1) + random.nextInt(2 + i * 2));
			}

			return true;
		}
	}
}
