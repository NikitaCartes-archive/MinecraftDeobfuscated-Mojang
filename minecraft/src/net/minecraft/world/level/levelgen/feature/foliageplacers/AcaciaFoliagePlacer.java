package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class AcaciaFoliagePlacer extends FoliagePlacer {
	public AcaciaFoliagePlacer(int i, int j, int k, int l) {
		super(i, j, k, l, FoliagePlacerType.ACACIA_FOLIAGE_PLACER);
	}

	public <T> AcaciaFoliagePlacer(Dynamic<T> dynamic) {
		this(dynamic.get("radius").asInt(0), dynamic.get("radius_random").asInt(0), dynamic.get("offset").asInt(0), dynamic.get("offset_random").asInt(0));
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
		boolean bl = foliageAttachment.doubleTrunk();
		BlockPos blockPos = foliageAttachment.foliagePos().above(l);
		this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k + foliageAttachment.radiusOffset(), set, -1 - j, bl);
		this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k - 1, set, -j, bl);
		this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k + foliageAttachment.radiusOffset() - 1, set, 0, bl);
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return 0;
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return j == 0 ? (i > 1 || k > 1) && i != 0 && k != 0 : i == l && k == l && l > 0;
	}
}
