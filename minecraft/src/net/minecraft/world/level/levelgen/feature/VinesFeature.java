package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VinesFeature extends Feature<NoneFeatureConfiguration> {
	private static final Direction[] DIRECTIONS = Direction.values();

	public VinesFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	public boolean place(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = 64; i < 256; i++) {
			mutableBlockPos.set(blockPos);
			mutableBlockPos.move(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
			mutableBlockPos.setY(i);
			if (worldGenLevel.isEmptyBlock(mutableBlockPos)) {
				for (Direction direction : DIRECTIONS) {
					if (direction != Direction.DOWN && VineBlock.isAcceptableNeighbour(worldGenLevel, mutableBlockPos, direction)) {
						worldGenLevel.setBlock(mutableBlockPos, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction), Boolean.valueOf(true)), 2);
						break;
					}
				}
			}
		}

		return true;
	}
}
