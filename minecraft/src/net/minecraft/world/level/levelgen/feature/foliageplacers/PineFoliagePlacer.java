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

public class PineFoliagePlacer extends FoliagePlacer {
	public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance)
				.and(UniformInt.codec(0, 16, 8).fieldOf("height").forGetter(pineFoliagePlacer -> pineFoliagePlacer.height))
				.apply(instance, PineFoliagePlacer::new)
	);
	private final UniformInt height;

	public PineFoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2, UniformInt uniformInt3) {
		super(uniformInt, uniformInt2);
		this.height = uniformInt3;
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.PINE_FOLIAGE_PLACER;
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
		int m = 0;

		for (int n = l; n >= l - j; n--) {
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), m, set, n, foliageAttachment.doubleTrunk(), boundingBox);
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
		return this.height.sample(random);
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return i == l && k == l && l > 0;
	}
}
