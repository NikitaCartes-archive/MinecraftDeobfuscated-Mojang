package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class PineFoliagePlacer extends FoliagePlacer {
	private final int height;
	private final int heightRandom;

	public PineFoliagePlacer(int i, int j, int k, int l, int m, int n) {
		super(i, j, k, l, FoliagePlacerType.PINE_FOLIAGE_PLACER);
		this.height = m;
		this.heightRandom = n;
	}

	public <T> PineFoliagePlacer(Dynamic<T> dynamic) {
		this(
			dynamic.get("radius").asInt(0),
			dynamic.get("radius_random").asInt(0),
			dynamic.get("offset").asInt(0),
			dynamic.get("offset_random").asInt(0),
			dynamic.get("height").asInt(0),
			dynamic.get("height_random").asInt(0)
		);
	}

	@Override
	protected void createFoliage(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		TreeConfiguration treeConfiguration,
		int i,
		FoliagePlacer.FoliageAttachment foliageAttachment,
		int j,
		int k,
		Set<BlockPos> set,
		int l
	) {
		int m = 0;

		for (int n = l; n >= l - j; n--) {
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), m, set, n, foliageAttachment.doubleTrunk());
			if (m >= 1 && n == l - j + 1) {
				m--;
			} else if (m < k + foliageAttachment.radiusOffset()) {
				m++;
			}
		}
	}

	@Override
	public int foliageRadius(Random random, int i) {
		return super.foliageRadius(random, i) + random.nextInt(i + 1);
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return this.height + random.nextInt(this.heightRandom + 1);
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return i == l && k == l && l > 0;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("height"), dynamicOps.createInt(this.height))
			.put(dynamicOps.createString("height_random"), dynamicOps.createInt(this.heightRandom));
		return dynamicOps.merge(super.serialize(dynamicOps), dynamicOps.createMap(builder.build()));
	}
}
