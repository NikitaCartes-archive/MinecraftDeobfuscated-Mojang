package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BonusChestFeature extends Feature<NoneFeatureConfiguration> {
	public BonusChestFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		RandomSource randomSource = featurePlaceContext.random();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		ChunkPos chunkPos = new ChunkPos(featurePlaceContext.origin());
		IntArrayList intArrayList = Util.toShuffledList(IntStream.rangeClosed(chunkPos.getMinBlockX(), chunkPos.getMaxBlockX()), randomSource);
		IntArrayList intArrayList2 = Util.toShuffledList(IntStream.rangeClosed(chunkPos.getMinBlockZ(), chunkPos.getMaxBlockZ()), randomSource);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Integer integer : intArrayList) {
			for (Integer integer2 : intArrayList2) {
				mutableBlockPos.set(integer, 0, integer2);
				BlockPos blockPos = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos);
				if (worldGenLevel.isEmptyBlock(blockPos) || worldGenLevel.getBlockState(blockPos).getCollisionShape(worldGenLevel, blockPos).isEmpty()) {
					worldGenLevel.setBlock(blockPos, Blocks.CHEST.defaultBlockState(), 2);
					RandomizableContainer.setBlockEntityLootTable(worldGenLevel, randomSource, blockPos, BuiltInLootTables.SPAWN_BONUS_CHEST);
					BlockState blockState = Blocks.TORCH.defaultBlockState();

					for (Direction direction : Direction.Plane.HORIZONTAL) {
						BlockPos blockPos2 = blockPos.relative(direction);
						if (blockState.canSurvive(worldGenLevel, blockPos2)) {
							worldGenLevel.setBlock(blockPos2, blockState, 2);
						}
					}

					return true;
				}
			}
		}

		return false;
	}
}
