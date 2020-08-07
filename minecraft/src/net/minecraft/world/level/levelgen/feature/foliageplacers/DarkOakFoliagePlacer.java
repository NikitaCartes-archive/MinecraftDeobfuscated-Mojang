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

public class DarkOakFoliagePlacer extends FoliagePlacer {
	public static final Codec<DarkOakFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance).apply(instance, DarkOakFoliagePlacer::new)
	);

	public DarkOakFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2) {
		super(uniformInt, uniformInt2);
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.DARK_OAK_FOLIAGE_PLACER;
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
		BlockPos blockPos = foliageAttachment.foliagePos().above(l);
		boolean bl = foliageAttachment.doubleTrunk();
		if (bl) {
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k + 2, set, -1, bl, boundingBox);
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k + 3, set, 0, bl, boundingBox);
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k + 2, set, 1, bl, boundingBox);
			if (random.nextBoolean()) {
				this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k, set, 2, bl, boundingBox);
			}
		} else {
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k + 2, set, -1, bl, boundingBox);
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, blockPos, k + 1, set, 0, bl, boundingBox);
		}
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return 4;
	}

	@Override
	protected boolean shouldSkipLocationSigned(Random random, int i, int j, int k, int l, boolean bl) {
		return j != 0 || !bl || i != -l && i < l || k != -l && k < l ? super.shouldSkipLocationSigned(random, i, j, k, l, bl) : true;
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		if (j == -1 && !bl) {
			return i == l && k == l;
		} else {
			return j == 1 ? i + k > l * 2 - 2 : false;
		}
	}
}
