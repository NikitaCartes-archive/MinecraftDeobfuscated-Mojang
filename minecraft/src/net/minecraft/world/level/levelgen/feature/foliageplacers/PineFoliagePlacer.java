package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class PineFoliagePlacer extends FoliagePlacer {
	public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance)
				.and(IntProvider.codec(0, 24).fieldOf("height").forGetter(pineFoliagePlacer -> pineFoliagePlacer.height))
				.apply(instance, PineFoliagePlacer::new)
	);
	private final IntProvider height;

	public PineFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3) {
		super(intProvider, intProvider2);
		this.height = intProvider3;
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.PINE_FOLIAGE_PLACER;
	}

	@Override
	protected void createFoliage(
		LevelSimulatedReader levelSimulatedReader,
		FoliagePlacer.FoliageSetter foliageSetter,
		RandomSource randomSource,
		TreeConfiguration treeConfiguration,
		int i,
		FoliagePlacer.FoliageAttachment foliageAttachment,
		int j,
		int k,
		int l
	) {
		int m = 0;

		for (int n = l; n >= l - j; n--) {
			this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, foliageAttachment.pos(), m, n, foliageAttachment.doubleTrunk());
			if (m >= 1 && n == l - j + 1) {
				m--;
			} else if (m < k + foliageAttachment.radiusOffset()) {
				m++;
			}
		}
	}

	@Override
	public int foliageRadius(RandomSource randomSource, int i) {
		return super.foliageRadius(randomSource, i) + randomSource.nextInt(Math.max(i + 1, 1));
	}

	@Override
	public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
		return this.height.sample(randomSource);
	}

	@Override
	protected boolean shouldSkipLocation(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
		return i == l && k == l && l > 0;
	}
}
