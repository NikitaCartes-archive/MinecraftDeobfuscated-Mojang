package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BonusChestFeature extends Feature<NoneFeatureConfiguration> {
	public BonusChestFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
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
		ChunkPos chunkPos = new ChunkPos(blockPos);
		List<Integer> list = (List<Integer>)IntStream.rangeClosed(chunkPos.getMinBlockX(), chunkPos.getMaxBlockX()).boxed().collect(Collectors.toList());
		Collections.shuffle(list, random);
		List<Integer> list2 = (List<Integer>)IntStream.rangeClosed(chunkPos.getMinBlockZ(), chunkPos.getMaxBlockZ()).boxed().collect(Collectors.toList());
		Collections.shuffle(list2, random);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Integer integer : list) {
			for (Integer integer2 : list2) {
				mutableBlockPos.set(integer, 0, integer2);
				BlockPos blockPos2 = levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos);
				if (levelAccessor.isEmptyBlock(blockPos2) || levelAccessor.getBlockState(blockPos2).getCollisionShape(levelAccessor, blockPos2).isEmpty()) {
					levelAccessor.setBlock(blockPos2, Blocks.CHEST.defaultBlockState(), 2);
					RandomizableContainerBlockEntity.setLootTable(levelAccessor, random, blockPos2, BuiltInLootTables.SPAWN_BONUS_CHEST);
					BlockState blockState = Blocks.TORCH.defaultBlockState();

					for (Direction direction : Direction.Plane.HORIZONTAL) {
						BlockPos blockPos3 = blockPos2.relative(direction);
						if (blockState.canSurvive(levelAccessor, blockPos3)) {
							levelAccessor.setBlock(blockPos3, blockState, 2);
						}
					}

					return true;
				}
			}
		}

		return false;
	}
}
