package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class PineFoliagePlacer extends FoliagePlacer {
	public PineFoliagePlacer(int i, int j) {
		super(i, j, FoliagePlacerType.PINE_FOLIAGE_PLACER);
	}

	public <T> PineFoliagePlacer(Dynamic<T> dynamic) {
		this(dynamic.get("radius").asInt(0), dynamic.get("radius_random").asInt(0));
	}

	@Override
	public void createFoliage(
		LevelSimulatedRW levelSimulatedRW, Random random, SmallTreeConfiguration smallTreeConfiguration, int i, int j, int k, BlockPos blockPos, Set<BlockPos> set
	) {
		int l = 0;

		for (int m = i; m >= j; m--) {
			this.placeLeavesRow(levelSimulatedRW, random, smallTreeConfiguration, i, blockPos, m, l, set);
			if (l >= 1 && m == j + 1) {
				l--;
			} else if (l < k) {
				l++;
			}
		}
	}

	@Override
	public int foliageRadius(Random random, int i, int j, SmallTreeConfiguration smallTreeConfiguration) {
		return this.radius + random.nextInt(this.radiusRandom + 1) + random.nextInt(j - i + 1);
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, int m) {
		return Math.abs(j) == m && Math.abs(l) == m && m > 0;
	}

	@Override
	public int getTreeRadiusForHeight(int i, int j, int k, int l) {
		return l <= 1 ? 0 : 2;
	}
}
