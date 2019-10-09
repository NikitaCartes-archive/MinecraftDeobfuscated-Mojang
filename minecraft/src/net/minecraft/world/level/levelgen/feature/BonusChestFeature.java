package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BonusChestFeature extends Feature<NoneFeatureConfiguration> {
	public BonusChestFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
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
			(blockState.isAir() || blockState.is(BlockTags.LEAVES)) && blockPos.getY() > 1;
			blockState = levelAccessor.getBlockState(blockPos)
		) {
			blockPos = blockPos.below();
		}

		if (blockPos.getY() < 1) {
			return false;
		} else {
			blockPos = blockPos.above();

			for (int i = 0; i < 4; i++) {
				BlockPos blockPos2 = blockPos.offset(random.nextInt(4) - random.nextInt(4), random.nextInt(3) - random.nextInt(3), random.nextInt(4) - random.nextInt(4));
				if (levelAccessor.isEmptyBlock(blockPos2)) {
					levelAccessor.setBlock(blockPos2, Blocks.CHEST.defaultBlockState(), 2);
					RandomizableContainerBlockEntity.setLootTable(levelAccessor, random, blockPos2, BuiltInLootTables.SPAWN_BONUS_CHEST);
					BlockState blockState2 = Blocks.TORCH.defaultBlockState();

					for (Direction direction : Direction.Plane.HORIZONTAL) {
						BlockPos blockPos3 = blockPos2.relative(direction);
						if (blockState2.canSurvive(levelAccessor, blockPos3)) {
							levelAccessor.setBlock(blockPos3, blockState2, 2);
						}
					}

					return true;
				}
			}

			return false;
		}
	}
}
