package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VinesFeature extends Feature<NoneFeatureConfiguration> {
	private static final Direction[] DIRECTIONS = Direction.values();

	public VinesFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = blockPos.getY(); i < 256; i++) {
			mutableBlockPos.set(blockPos);
			mutableBlockPos.move(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
			mutableBlockPos.setY(i);
			if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
				for (Direction direction : DIRECTIONS) {
					if (direction != Direction.DOWN && VineBlock.isAcceptableNeighbour(levelAccessor, mutableBlockPos, direction)) {
						levelAccessor.setBlock(mutableBlockPos, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction), Boolean.valueOf(true)), 2);
						break;
					}
				}
			}
		}

		return true;
	}
}
