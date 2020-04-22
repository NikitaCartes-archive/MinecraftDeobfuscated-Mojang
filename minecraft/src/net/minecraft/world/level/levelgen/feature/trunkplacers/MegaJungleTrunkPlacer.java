package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaJungleTrunkPlacer extends GiantTrunkPlacer {
	public MegaJungleTrunkPlacer(int i, int j, int k) {
		super(i, j, k, TrunkPlacerType.MEGA_JUNGLE_TRUNK_PLACER);
	}

	public <T> MegaJungleTrunkPlacer(Dynamic<T> dynamic) {
		this(dynamic.get("base_height").asInt(0), dynamic.get("height_rand_a").asInt(0), dynamic.get("height_rand_b").asInt(0));
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(
		LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		List<FoliagePlacer.FoliageAttachment> list = Lists.<FoliagePlacer.FoliageAttachment>newArrayList();
		list.addAll(super.placeTrunk(levelSimulatedRW, random, i, blockPos, set, boundingBox, treeConfiguration));

		for (int j = i - 2 - random.nextInt(4); j > i / 2; j -= 2 + random.nextInt(4)) {
			float f = random.nextFloat() * (float) (Math.PI * 2);
			int k = 0;
			int l = 0;

			for (int m = 0; m < 5; m++) {
				k = (int)(1.5F + Mth.cos(f) * (float)m);
				l = (int)(1.5F + Mth.sin(f) * (float)m);
				BlockPos blockPos2 = blockPos.offset(k, j - 3 + m / 2, l);
				placeLog(levelSimulatedRW, random, blockPos2, set, boundingBox, treeConfiguration);
			}

			list.add(new FoliagePlacer.FoliageAttachment(blockPos.offset(k, j, l), -2, false));
		}

		return list;
	}
}
