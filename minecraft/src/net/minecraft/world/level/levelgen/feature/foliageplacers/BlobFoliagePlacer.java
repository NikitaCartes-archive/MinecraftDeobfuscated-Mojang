package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
	public BlobFoliagePlacer(int i, int j) {
		super(i, j, FoliagePlacerType.BLOB_FOLIAGE_PLACER);
	}

	public <T> BlobFoliagePlacer(Dynamic<T> dynamic) {
		this(dynamic.get("radius").asInt(0), dynamic.get("radius_random").asInt(0));
	}

	@Override
	public void createFoliage(
		LevelSimulatedRW levelSimulatedRW, Random random, SmallTreeConfiguration smallTreeConfiguration, int i, int j, int k, BlockPos blockPos, Set<BlockPos> set
	) {
		for (int l = i; l >= j; l--) {
			int m = Math.max(k - 1 - (l - i) / 2, 0);
			this.placeLeavesRow(levelSimulatedRW, random, smallTreeConfiguration, i, blockPos, l, m, set);
		}
	}

	@Override
	public int foliageRadius(Random random, int i, int j, SmallTreeConfiguration smallTreeConfiguration) {
		return this.radius + random.nextInt(this.radiusRandom + 1);
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, int m) {
		return Math.abs(j) == m && Math.abs(l) == m && (random.nextInt(2) == 0 || k == i);
	}

	@Override
	public int getTreeRadiusForHeight(int i, int j, int k, int l) {
		return l == 0 ? 0 : 1;
	}

	public static BlobFoliagePlacer random(Random random) {
		return new BlobFoliagePlacer(random.nextInt(10) + 1, random.nextInt(5));
	}
}
