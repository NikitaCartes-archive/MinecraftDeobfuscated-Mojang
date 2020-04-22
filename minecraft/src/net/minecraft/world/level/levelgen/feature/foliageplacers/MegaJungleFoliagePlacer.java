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

public class MegaJungleFoliagePlacer extends FoliagePlacer {
	protected final int height;

	public MegaJungleFoliagePlacer(int i, int j, int k, int l, int m) {
		super(i, j, k, l, FoliagePlacerType.MEGA_JUNGLE_FOLIAGE_PLACER);
		this.height = m;
	}

	public <T> MegaJungleFoliagePlacer(Dynamic<T> dynamic) {
		this(
			dynamic.get("radius").asInt(0),
			dynamic.get("radius_random").asInt(0),
			dynamic.get("offset").asInt(0),
			dynamic.get("offset_random").asInt(0),
			dynamic.get("height").asInt(0)
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
		int m = foliageAttachment.doubleTrunk() ? j : 1 + random.nextInt(2);

		for (int n = l; n >= l - m; n--) {
			int o = k + foliageAttachment.radiusOffset() + 1 - n;
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), o, set, n, foliageAttachment.doubleTrunk());
		}
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return this.height;
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return i + k >= 7 ? true : i * i + k * k > l * l;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("height"), dynamicOps.createInt(this.height));
		return dynamicOps.merge(super.serialize(dynamicOps), dynamicOps.createMap(builder.build()));
	}
}
