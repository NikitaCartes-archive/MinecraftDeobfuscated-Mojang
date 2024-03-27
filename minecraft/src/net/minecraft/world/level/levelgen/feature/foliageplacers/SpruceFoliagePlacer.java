package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class SpruceFoliagePlacer extends FoliagePlacer {
	public static final MapCodec<SpruceFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
		instance -> foliagePlacerParts(instance)
				.and(IntProvider.codec(0, 24).fieldOf("trunk_height").forGetter(spruceFoliagePlacer -> spruceFoliagePlacer.trunkHeight))
				.apply(instance, SpruceFoliagePlacer::new)
	);
	private final IntProvider trunkHeight;

	public SpruceFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3) {
		super(intProvider, intProvider2);
		this.trunkHeight = intProvider3;
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.SPRUCE_FOLIAGE_PLACER;
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
		BlockPos blockPos = foliageAttachment.pos();
		int m = randomSource.nextInt(2);
		int n = 1;
		int o = 0;

		for (int p = l; p >= -j; p--) {
			this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, m, p, foliageAttachment.doubleTrunk());
			if (m >= n) {
				m = o;
				o = 1;
				n = Math.min(n + 1, k + foliageAttachment.radiusOffset());
			} else {
				m++;
			}
		}
	}

	@Override
	public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
		return Math.max(4, i - this.trunkHeight.sample(randomSource));
	}

	@Override
	protected boolean shouldSkipLocation(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
		return i == l && k == l && l > 0;
	}
}
