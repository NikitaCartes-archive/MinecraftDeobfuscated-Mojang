package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class GiantTrunkPlacer extends TrunkPlacer {
	public static final Codec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> trunkPlacerParts(instance).apply(instance, GiantTrunkPlacer::new));

	public GiantTrunkPlacer(int i, int j, int k) {
		super(i, j, k);
	}

	@Override
	protected TrunkPlacerType<?> type() {
		return TrunkPlacerType.GIANT_TRUNK_PLACER;
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		Random random,
		int i,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	) {
		BlockPos blockPos2 = blockPos.below();
		setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2, treeConfiguration);
		setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2.east(), treeConfiguration);
		setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2.south(), treeConfiguration);
		setDirtAt(levelSimulatedReader, biConsumer, random, blockPos2.south().east(), treeConfiguration);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < i; j++) {
			placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration, blockPos, 0, j, 0);
			if (j < i - 1) {
				placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration, blockPos, 1, j, 0);
				placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration, blockPos, 1, j, 1);
				placeLogIfFreeWithOffset(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration, blockPos, 0, j, 1);
			}
		}

		return ImmutableList.of(new FoliagePlacer.FoliageAttachment(blockPos.above(i), 0, true));
	}

	private static void placeLogIfFreeWithOffset(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		TreeConfiguration treeConfiguration,
		BlockPos blockPos,
		int i,
		int j,
		int k
	) {
		mutableBlockPos.setWithOffset(blockPos, i, j, k);
		placeLogIfFree(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration);
	}
}
