package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FancyFoliagePlacer extends BlobFoliagePlacer {
	public static final Codec<FancyFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> blobParts(instance).apply(instance, FancyFoliagePlacer::new));

	public FancyFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2, int i) {
		super(uniformInt, uniformInt2, i);
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.FANCY_FOLIAGE_PLACER;
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
		for (int m = l; m >= l - j; m--) {
			int n = k + (m != l && m != l - j ? 1 : 0);
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), n, set, m, foliageAttachment.doubleTrunk(), boundingBox);
		}
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return Mth.square((float)i + 0.5F) + Mth.square((float)k + 0.5F) > (float)(l * l);
	}
}
