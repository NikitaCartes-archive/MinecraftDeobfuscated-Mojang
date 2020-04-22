package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class FancyFoliagePlacer extends BlobFoliagePlacer {
	public FancyFoliagePlacer(int i, int j, int k, int l, int m) {
		super(i, j, k, l, m, FoliagePlacerType.FANCY_FOLIAGE_PLACER);
	}

	public <T> FancyFoliagePlacer(Dynamic<T> dynamic) {
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
			int n = k + (m != l && m != l - j ? 1 : 0);
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), n, set, m, foliageAttachment.doubleTrunk());
		}
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return Mth.square((float)i + 0.5F) + Mth.square((float)k + 0.5F) > (float)(l * l);
	}
}
