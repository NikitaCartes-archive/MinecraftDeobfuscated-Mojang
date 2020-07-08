package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class AcaciaFoliagePlacer extends FoliagePlacer {
	public static final Codec<AcaciaFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance).apply(instance, AcaciaFoliagePlacer::new)
	);

	public AcaciaFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2) {
		super(uniformInt, uniformInt2);
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.ACACIA_FOLIAGE_PLACER;
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
		int l,
		BoundingBox boundingBox
	) {
		boolean bl = foliageAttachment.doubleTrunk();
		BlockPos blockPos = foliageAttachment.foliagePos().above(l);
		this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k + foliageAttachment.radiusOffset(), set, -1 - j, bl, boundingBox);
		this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k - 1, set, -j, bl, boundingBox);
		this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k + foliageAttachment.radiusOffset() - 1, set, 0, bl, boundingBox);
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
