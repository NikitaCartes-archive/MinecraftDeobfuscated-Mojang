package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class AcaciaFoliagePlacer extends FoliagePlacer {
	public AcaciaFoliagePlacer(int i, int j, int k, int l) {
		super(i, j, k, l, FoliagePlacerType.ACACIA_FOLIAGE_PLACER);
	}

	public <T> AcaciaFoliagePlacer(Dynamic<T> dynamic) {
		this(dynamic.get("radius").asInt(0), dynamic.get("radius_random").asInt(0), dynamic.get("offset").asInt(0), dynamic.get("offset_random").asInt(0));
	}

	@Override
	public void createFoliage(
		LevelSimulatedRW levelSimulatedRW, Random random, SmallTreeConfiguration smallTreeConfiguration, int i, BlockPos blockPos, int j, int k, Set<BlockPos> set
	) {
		int l = this.offset + random.nextInt(this.offsetRandom + 1);
		smallTreeConfiguration.foliagePlacer.placeLeavesRow(levelSimulatedRW, random, smallTreeConfiguration, blockPos, j, l - 1, k, set);
		smallTreeConfiguration.foliagePlacer.placeLeavesRow(levelSimulatedRW, random, smallTreeConfiguration, blockPos, j, l, 1, set);

		for (int m = -1; m <= 1; m++) {
			for (int n = -1; n <= 1; n++) {
				this.placeLeaf(levelSimulatedRW, random, blockPos.offset(m, 0, n), smallTreeConfiguration, set);
			}
		}

		for (int m = 2; m <= k - 1; m++) {
			this.placeLeaf(levelSimulatedRW, random, blockPos.above(l).east(m), smallTreeConfiguration, set);
			this.placeLeaf(levelSimulatedRW, random, blockPos.above(l).west(m), smallTreeConfiguration, set);
			this.placeLeaf(levelSimulatedRW, random, blockPos.above(l).south(m), smallTreeConfiguration, set);
			this.placeLeaf(levelSimulatedRW, random, blockPos.above(l).north(m), smallTreeConfiguration, set);
		}
	}

	@Override
	public int foliageRadius(Random random, int i, SmallTreeConfiguration smallTreeConfiguration) {
		return this.radius + random.nextInt(this.radiusRandom + 1);
	}

	@Override
	public int foliageHeight(Random random, int i) {
		return 0;
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, int m) {
		return Math.abs(j) == m && Math.abs(l) == m && m > 0;
	}

	@Override
	public int getTreeRadiusForHeight(int i, int j, int k) {
		return k == 0 ? 0 : 2;
	}
}
