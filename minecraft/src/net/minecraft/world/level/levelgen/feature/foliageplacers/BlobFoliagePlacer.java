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

public class BlobFoliagePlacer extends FoliagePlacer {
	protected final int height;

	protected BlobFoliagePlacer(int i, int j, int k, int l, int m, FoliagePlacerType<?> foliagePlacerType) {
		super(i, j, k, l, foliagePlacerType);
		this.height = m;
	}

	public BlobFoliagePlacer(int i, int j, int k, int l, int m) {
		this(i, j, k, l, m, FoliagePlacerType.BLOB_FOLIAGE_PLACER);
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
		for (int m = l; m >= l - j; m--) {
			int n = Math.max(k + foliageAttachment.radiusOffset() - 1 - m / 2, 0);
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), n, set, m, foliageAttachment.doubleTrunk());
		}
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return this.height;
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return i == l && k == l && (random.nextInt(2) == 0 || j == 0);
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("height"), dynamicOps.createInt(this.height));
		return dynamicOps.merge(super.serialize(dynamicOps), dynamicOps.createMap(builder.build()));
	}
}
