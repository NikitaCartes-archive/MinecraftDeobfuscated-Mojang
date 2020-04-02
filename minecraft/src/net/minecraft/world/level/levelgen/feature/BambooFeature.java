package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class BambooFeature extends Feature<ProbabilityFeatureConfiguration> {
	private static final BlockState BAMBOO_TRUNK = Blocks.BAMBOO
		.defaultBlockState()
		.setValue(BambooBlock.AGE, Integer.valueOf(1))
		.setValue(BambooBlock.LEAVES, BambooLeaves.NONE)
		.setValue(BambooBlock.STAGE, Integer.valueOf(0));
	private static final BlockState BAMBOO_FINAL_LARGE = BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE)
		.setValue(BambooBlock.STAGE, Integer.valueOf(1));
	private static final BlockState BAMBOO_TOP_LARGE = BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE);
	private static final BlockState BAMBOO_TOP_SMALL = BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.SMALL);

	public BambooFeature(Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		ProbabilityFeatureConfiguration probabilityFeatureConfiguration
	) {
		int i = 0;
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		BlockPos.MutableBlockPos mutableBlockPos2 = blockPos.mutable();
		if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
			if (Blocks.BAMBOO.defaultBlockState().canSurvive(levelAccessor, mutableBlockPos)) {
				int j = random.nextInt(12) + 5;
				if (random.nextFloat() < probabilityFeatureConfiguration.probability) {
					int k = random.nextInt(4) + 1;

					for (int l = blockPos.getX() - k; l <= blockPos.getX() + k; l++) {
						for (int m = blockPos.getZ() - k; m <= blockPos.getZ() + k; m++) {
							int n = l - blockPos.getX();
							int o = m - blockPos.getZ();
							if (n * n + o * o <= k * k) {
								mutableBlockPos2.set(l, levelAccessor.getHeight(Heightmap.Types.WORLD_SURFACE, l, m) - 1, m);
								if (isDirt(levelAccessor.getBlockState(mutableBlockPos2).getBlock())) {
									levelAccessor.setBlock(mutableBlockPos2, Blocks.PODZOL.defaultBlockState(), 2);
								}
							}
						}
					}
				}

				for (int k = 0; k < j && levelAccessor.isEmptyBlock(mutableBlockPos); k++) {
					levelAccessor.setBlock(mutableBlockPos, BAMBOO_TRUNK, 2);
					mutableBlockPos.move(Direction.UP, 1);
				}

				if (mutableBlockPos.getY() - blockPos.getY() >= 3) {
					levelAccessor.setBlock(mutableBlockPos, BAMBOO_FINAL_LARGE, 2);
					levelAccessor.setBlock(mutableBlockPos.move(Direction.DOWN, 1), BAMBOO_TOP_LARGE, 2);
					levelAccessor.setBlock(mutableBlockPos.move(Direction.DOWN, 1), BAMBOO_TOP_SMALL, 2);
				}
			}

			i++;
		}

		return i > 0;
	}
}
