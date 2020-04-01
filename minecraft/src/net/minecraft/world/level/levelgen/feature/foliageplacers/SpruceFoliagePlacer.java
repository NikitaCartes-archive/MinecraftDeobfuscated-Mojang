package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class SpruceFoliagePlacer extends FoliagePlacer {
	public SpruceFoliagePlacer(int i, int j) {
		super(i, j, FoliagePlacerType.SPRUCE_FOLIAGE_PLACER);
	}

	public <T> SpruceFoliagePlacer(Dynamic<T> dynamic) {
		this(dynamic.get("radius").asInt(0), dynamic.get("radius_random").asInt(0));
	}

	@Override
	public void createFoliage(
		LevelSimulatedRW levelSimulatedRW, Random random, SmallTreeConfiguration smallTreeConfiguration, int i, int j, int k, BlockPos blockPos, Set<BlockPos> set
	) {
		int l = random.nextInt(2);
		int m = 1;
		int n = 0;

		for (int o = i; o >= j; o--) {
			this.placeLeavesRow(levelSimulatedRW, random, smallTreeConfiguration, i, blockPos, o, l, set);
			if (l >= m) {
				l = n;
				n = 1;
				m = Math.min(m + 1, k);
			} else {
				l++;
			}
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
		return l <= 1 ? 0 : 2;
	}

	public static SpruceFoliagePlacer random(Random random) {
		return new SpruceFoliagePlacer(random.nextInt(10) + 1, random.nextInt(5));
	}
}
