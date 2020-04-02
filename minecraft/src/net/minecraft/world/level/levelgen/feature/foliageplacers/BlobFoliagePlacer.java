package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
	private final int height;

	public BlobFoliagePlacer(int i, int j, int k, int l, int m) {
		super(i, j, k, l, FoliagePlacerType.BLOB_FOLIAGE_PLACER);
		this.height = m;
	}

	public <T> BlobFoliagePlacer(Dynamic<T> dynamic) {
		this(
			dynamic.get("radius").asInt(0),
			dynamic.get("radius_random").asInt(0),
			dynamic.get("offset").asInt(0),
			dynamic.get("offset_random").asInt(0),
			dynamic.get("height").asInt(0)
		);
	}

	@Override
	public void createFoliage(
		LevelSimulatedRW levelSimulatedRW, Random random, SmallTreeConfiguration smallTreeConfiguration, int i, BlockPos blockPos, int j, int k, Set<BlockPos> set
	) {
		int l = this.offset + random.nextInt(this.offsetRandom + 1);

		for (int m = j + l; m >= l; m--) {
			int n = Math.max(k - 1 - (m - j) / 2, 0);
			this.placeLeavesRow(levelSimulatedRW, random, smallTreeConfiguration, blockPos, j, m, n, set);
		}
	}

	@Override
	public int foliageRadius(Random random, int i, SmallTreeConfiguration smallTreeConfiguration) {
		return this.radius + random.nextInt(this.radiusRandom + 1);
	}

	@Override
	public int foliageHeight(Random random, int i) {
		return this.height;
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, int m) {
		return Math.abs(j) == m && Math.abs(l) == m && (random.nextInt(2) == 0 || k == i);
	}

	@Override
	public int getTreeRadiusForHeight(int i, int j, int k) {
		return k == 0 ? 0 : 1;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("height"), dynamicOps.createInt(this.height));
		return dynamicOps.merge(super.serialize(dynamicOps), dynamicOps.createMap(builder.build()));
	}
}
