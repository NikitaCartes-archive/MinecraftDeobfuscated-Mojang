package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DeadBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class DeadBushFeature extends Feature<NoneFeatureConfiguration> {
	private static final DeadBushBlock DEAD_BUSH = (DeadBushBlock)Blocks.DEAD_BUSH;

	public DeadBushFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		for (BlockState blockState = levelAccessor.getBlockState(blockPos);
			(blockState.isAir() || blockState.is(BlockTags.LEAVES)) && blockPos.getY() > 0;
			blockState = levelAccessor.getBlockState(blockPos)
		) {
			blockPos = blockPos.below();
		}

		BlockState blockState2 = DEAD_BUSH.defaultBlockState();

		for (int i = 0; i < 4; i++) {
			BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
			if (levelAccessor.isEmptyBlock(blockPos2) && blockState2.canSurvive(levelAccessor, blockPos2)) {
				levelAccessor.setBlock(blockPos2, blockState2, 2);
			}
		}

		return true;
	}
}
