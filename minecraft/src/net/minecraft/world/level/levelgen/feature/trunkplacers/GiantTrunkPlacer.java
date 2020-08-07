package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

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
		LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		BlockPos blockPos2 = blockPos.below();
		setDirtAt(levelSimulatedRW, blockPos2);
		setDirtAt(levelSimulatedRW, blockPos2.east());
		setDirtAt(levelSimulatedRW, blockPos2.south());
		setDirtAt(levelSimulatedRW, blockPos2.south().east());
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < i; j++) {
			placeLogIfFreeWithOffset(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration, blockPos, 0, j, 0);
			if (j < i - 1) {
				placeLogIfFreeWithOffset(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration, blockPos, 1, j, 0);
				placeLogIfFreeWithOffset(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration, blockPos, 1, j, 1);
				placeLogIfFreeWithOffset(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration, blockPos, 0, j, 1);
			}
		}

		return ImmutableList.of(new FoliagePlacer.FoliageAttachment(blockPos.above(i), 0, true));
	}

	private static void placeLogIfFreeWithOffset(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		TreeConfiguration treeConfiguration,
		BlockPos blockPos,
		int i,
		int j,
		int k
	) {
		mutableBlockPos.setWithOffset(blockPos, i, j, k);
		placeLogIfFree(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, treeConfiguration);
	}
}
