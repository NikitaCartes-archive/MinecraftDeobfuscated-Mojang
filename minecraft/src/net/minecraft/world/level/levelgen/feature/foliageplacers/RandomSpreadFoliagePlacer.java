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

public class RandomSpreadFoliagePlacer extends FoliagePlacer {
	public static final Codec<RandomSpreadFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance)
				.<UniformInt, Integer>and(
					instance.group(
						UniformInt.codec(1, 256, 256).fieldOf("foliage_height").forGetter(randomSpreadFoliagePlacer -> randomSpreadFoliagePlacer.foliageHeight),
						Codec.intRange(0, 256).fieldOf("leaf_placement_attempts").forGetter(randomSpreadFoliagePlacer -> randomSpreadFoliagePlacer.leafPlacementAttempts)
					)
				)
				.apply(instance, RandomSpreadFoliagePlacer::new)
	);
	private final UniformInt foliageHeight;
	private final int leafPlacementAttempts;

	public RandomSpreadFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2, UniformInt uniformInt3, int i) {
		super(uniformInt, uniformInt2);
		this.foliageHeight = uniformInt3;
		this.leafPlacementAttempts = i;
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.RANDOM_SPREAD_FOLIAGE_PLACER;
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
		BlockPos blockPos = foliageAttachment.foliagePos();
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int m = 0; m < this.leafPlacementAttempts; m++) {
			mutableBlockPos.setWithOffset(blockPos, random.nextInt(k) - random.nextInt(k), random.nextInt(j) - random.nextInt(j), random.nextInt(k) - random.nextInt(k));
			this.tryPlaceLeaf(levelSimulatedRW, random, treeConfiguration, set, boundingBox, mutableBlockPos);
		}
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return this.foliageHeight.sample(random);
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return false;
	}
}
