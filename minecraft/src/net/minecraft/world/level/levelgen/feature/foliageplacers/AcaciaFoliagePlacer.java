package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class AcaciaFoliagePlacer extends FoliagePlacer {
	public AcaciaFoliagePlacer(int i, int j) {
		super(i, j, FoliagePlacerType.ACACIA_FOLIAGE_PLACER);
	}

	public <T> AcaciaFoliagePlacer(Dynamic<T> dynamic) {
		this(dynamic.get("radius").asInt(0), dynamic.get("radius_random").asInt(0));
	}

	@Override
	public void createFoliage(
		LevelSimulatedRW levelSimulatedRW, Random random, SmallTreeConfiguration smallTreeConfiguration, int i, int j, int k, BlockPos blockPos, Set<BlockPos> set
	) {
		smallTreeConfiguration.foliagePlacer.placeLeavesRow(levelSimulatedRW, random, smallTreeConfiguration, i, blockPos, 0, k, set);
		smallTreeConfiguration.foliagePlacer.placeLeavesRow(levelSimulatedRW, random, smallTreeConfiguration, i, blockPos, 1, 1, set);
		BlockPos blockPos2 = blockPos.above();

		for (int l = 2; l <= k - 1; l++) {
			this.placeLeaf(levelSimulatedRW, random, blockPos2.east(l), smallTreeConfiguration, set);
			this.placeLeaf(levelSimulatedRW, random, blockPos2.west(l), smallTreeConfiguration, set);
			this.placeLeaf(levelSimulatedRW, random, blockPos2.south(l), smallTreeConfiguration, set);
			this.placeLeaf(levelSimulatedRW, random, blockPos2.north(l), smallTreeConfiguration, set);
		}
	}

	@Override
	public int foliageRadius(Random random, int i, int j, SmallTreeConfiguration smallTreeConfiguration) {
		return this.radius + random.nextInt(this.radiusRandom + 1);
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, int m) {
		return Math.abs(j) == m && Math.abs(l) == m && m > 0;
	}

	@Override
	public int getTreeRadiusForHeight(int i, int j, int k, int l) {
		if (l == 0) {
			return 0;
		} else {
			return l >= 1 + j - 2 ? k : 1;
		}
	}
}
