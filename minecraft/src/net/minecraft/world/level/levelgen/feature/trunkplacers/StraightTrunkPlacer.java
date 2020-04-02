package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StraightTrunkPlacer extends TrunkPlacer {
	public StraightTrunkPlacer(int i, int j, int k) {
		super(i, j, k, TrunkPlacerType.STRAIGHT_TRUNK_PLACER);
	}

	public <T> StraightTrunkPlacer(Dynamic<T> dynamic) {
		this(dynamic.get("base_height").asInt(0), dynamic.get("height_rand_a").asInt(0), dynamic.get("height_rand_b").asInt(0));
	}

	@Override
	public Map<BlockPos, Integer> placeTrunk(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		int i,
		BlockPos blockPos,
		int j,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
		for (int k = 0; k < i; k++) {
			AbstractTreeFeature.placeLog(levelSimulatedRW, random, blockPos.above(k), set, boundingBox, smallTreeConfiguration);
		}

		return ImmutableMap.of(blockPos.above(i), j);
	}
}
