package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class CherryFoliagePlacer extends FoliagePlacer {
	public static final Codec<CherryFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance)
				.<IntProvider, Float, Float, Float, Float>and(
					instance.group(
						IntProvider.codec(4, 16).fieldOf("height").forGetter(cherryFoliagePlacer -> cherryFoliagePlacer.height),
						Codec.floatRange(0.0F, 1.0F).fieldOf("wide_bottom_layer_hole_chance").forGetter(cherryFoliagePlacer -> cherryFoliagePlacer.wideBottomLayerHoleChance),
						Codec.floatRange(0.0F, 1.0F).fieldOf("corner_hole_chance").forGetter(cherryFoliagePlacer -> cherryFoliagePlacer.wideBottomLayerHoleChance),
						Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_chance").forGetter(cherryFoliagePlacer -> cherryFoliagePlacer.hangingLeavesChance),
						Codec.floatRange(0.0F, 1.0F)
							.fieldOf("hanging_leaves_extension_chance")
							.forGetter(cherryFoliagePlacer -> cherryFoliagePlacer.hangingLeavesExtensionChance)
					)
				)
				.apply(instance, CherryFoliagePlacer::new)
	);
	private final IntProvider height;
	private final float wideBottomLayerHoleChance;
	private final float cornerHoleChance;
	private final float hangingLeavesChance;
	private final float hangingLeavesExtensionChance;

	public CherryFoliagePlacer(IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3, float f, float g, float h, float i) {
		super(intProvider, intProvider2);
		this.height = intProvider3;
		this.wideBottomLayerHoleChance = f;
		this.cornerHoleChance = g;
		this.hangingLeavesChance = h;
		this.hangingLeavesExtensionChance = i;
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.CHERRY_FOLIAGE_PLACER;
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
		boolean bl = foliageAttachment.doubleTrunk();
		BlockPos blockPos = foliageAttachment.pos().above(l);
		int m = k + foliageAttachment.radiusOffset() - 1;
		this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, m - 2, j - 3, bl);
		this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, m - 1, j - 4, bl);

		for (int n = j - 5; n >= 0; n--) {
			this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, m, n, bl);
		}

		this.placeLeavesRowWithHangingLeavesBelow(
			levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, m, -1, bl, this.hangingLeavesChance, this.hangingLeavesExtensionChance
		);
		this.placeLeavesRowWithHangingLeavesBelow(
			levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, m - 1, -2, bl, this.hangingLeavesChance, this.hangingLeavesExtensionChance
		);
	}

	@Override
	public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
		return this.height.sample(randomSource);
	}

	@Override
	protected boolean shouldSkipLocation(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
		if (j == -1 && (i == l || k == l) && randomSource.nextFloat() < this.wideBottomLayerHoleChance) {
			return true;
		} else {
			boolean bl2 = i == l && k == l;
			boolean bl3 = l > 2;
			return bl3 ? bl2 || i + k > l * 2 - 2 && randomSource.nextFloat() < this.cornerHoleChance : bl2 && randomSource.nextFloat() < this.cornerHoleChance;
		}
	}
}
