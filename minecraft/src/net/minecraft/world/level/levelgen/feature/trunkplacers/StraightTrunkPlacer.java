package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StraightTrunkPlacer extends TrunkPlacer {
	public StraightTrunkPlacer(int i, int j, int k) {
		super(i, j, k, TrunkPlacerType.STRAIGHT_TRUNK_PLACER);
	}

	public <T> StraightTrunkPlacer(Dynamic<T> dynamic) {
		this(dynamic.get("base_height").asInt(0), dynamic.get("height_rand_a").asInt(0), dynamic.get("height_rand_b").asInt(0));
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(
		LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		setDirtAt(levelSimulatedRW, blockPos.below());

		for (int j = 0; j < i; j++) {
			placeLog(levelSimulatedRW, random, blockPos.above(j), set, boundingBox, treeConfiguration);
		}

		return ImmutableList.of(new FoliagePlacer.FoliageAttachment(blockPos.above(i), 0, false));
	}
}
