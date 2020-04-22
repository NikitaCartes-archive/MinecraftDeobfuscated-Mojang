package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class TrunkPlacer {
	private final int baseHeight;
	private final int heightRandA;
	private final int heightRandB;
	protected final TrunkPlacerType<?> type;

	public TrunkPlacer(int i, int j, int k, TrunkPlacerType<?> trunkPlacerType) {
		this.baseHeight = i;
		this.heightRandA = j;
		this.heightRandB = k;
		this.type = trunkPlacerType;
	}

	public abstract List<FoliagePlacer.FoliageAttachment> placeTrunk(
		LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	);

	public int getTreeHeight(Random random) {
		return this.baseHeight + random.nextInt(this.heightRandA + 1) + random.nextInt(this.heightRandB + 1);
	}

	protected static void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState, BoundingBox boundingBox) {
		TreeFeature.setBlockKnownShape(levelWriter, blockPos, blockState);
		boundingBox.expand(new BoundingBox(blockPos, blockPos));
	}

	private static boolean isDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
			Block block = blockState.getBlock();
			return Feature.isDirt(block) && block != Blocks.GRASS_BLOCK && block != Blocks.MYCELIUM;
		});
	}

	protected static void setDirtAt(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos) {
		if (!isDirt(levelSimulatedRW, blockPos)) {
			TreeFeature.setBlockKnownShape(levelSimulatedRW, blockPos, Blocks.DIRT.defaultBlockState());
		}
	}

	protected static boolean placeLog(
		LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		if (TreeFeature.validTreePos(levelSimulatedRW, blockPos)) {
			setBlock(levelSimulatedRW, blockPos, treeConfiguration.trunkProvider.getState(random, blockPos), boundingBox);
			set.add(blockPos.immutable());
			return true;
		} else {
			return false;
		}
	}

	protected static void placeLogIfFree(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		TreeConfiguration treeConfiguration
	) {
		if (TreeFeature.isFree(levelSimulatedRW, mutableBlockPos)) {
			placeLog(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration);
		}
	}

	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("type"), dynamicOps.createString(Registry.TRUNK_PLACER_TYPES.getKey(this.type).toString()))
			.put(dynamicOps.createString("base_height"), dynamicOps.createInt(this.baseHeight))
			.put(dynamicOps.createString("height_rand_a"), dynamicOps.createInt(this.heightRandA))
			.put(dynamicOps.createString("height_rand_b"), dynamicOps.createInt(this.heightRandB));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build())).getValue();
	}
}
