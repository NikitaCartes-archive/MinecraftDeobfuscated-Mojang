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

public class SpruceFoliagePlacer extends FoliagePlacer {
	private final int trunkHeight;
	private final int trunkHeightRandom;

	public SpruceFoliagePlacer(int i, int j, int k, int l, int m, int n) {
		super(i, j, k, l, FoliagePlacerType.SPRUCE_FOLIAGE_PLACER);
		this.trunkHeight = m;
		this.trunkHeightRandom = n;
	}

	public <T> SpruceFoliagePlacer(Dynamic<T> dynamic) {
		this(
			dynamic.get("radius").asInt(0),
			dynamic.get("radius_random").asInt(0),
			dynamic.get("offset").asInt(0),
			dynamic.get("offset_random").asInt(0),
			dynamic.get("trunk_height").asInt(0),
			dynamic.get("trunk_height_random").asInt(0)
		);
	}

	@Override
	public void createFoliage(
		LevelSimulatedRW levelSimulatedRW, Random random, SmallTreeConfiguration smallTreeConfiguration, int i, BlockPos blockPos, int j, int k, Set<BlockPos> set
	) {
		int l = this.offset + random.nextInt(this.offsetRandom + 1);
		int m = random.nextInt(2);
		int n = 1;
		int o = 0;

		for (int p = j + l; p >= 0; p--) {
			this.placeLeavesRow(levelSimulatedRW, random, smallTreeConfiguration, blockPos, j, p, m, set);
			if (m >= n) {
				m = o;
				o = 1;
				n = Math.min(n + 1, k);
			} else {
				m++;
			}
		}
	}

	@Override
	public int foliageRadius(Random random, int i, SmallTreeConfiguration smallTreeConfiguration) {
		return this.radius + random.nextInt(this.radiusRandom + 1);
	}

	@Override
	public int foliageHeight(Random random, int i) {
		return i - this.trunkHeight - random.nextInt(this.trunkHeightRandom + 1);
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, int m) {
		return Math.abs(j) == m && Math.abs(l) == m && m > 0;
	}

	@Override
	public int getTreeRadiusForHeight(int i, int j, int k) {
		return k <= 1 ? 0 : 2;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("trunk_height"), dynamicOps.createInt(this.trunkHeight))
			.put(dynamicOps.createString("trunk_height_random"), dynamicOps.createInt(this.trunkHeightRandom));
		return dynamicOps.merge(super.serialize(dynamicOps), dynamicOps.createMap(builder.build()));
	}
}
