package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaJungleFoliagePlacer extends FoliagePlacer {
	public static final Codec<MegaJungleFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance)
				.and(Codec.INT.fieldOf("height").forGetter(megaJungleFoliagePlacer -> megaJungleFoliagePlacer.height))
				.apply(instance, MegaJungleFoliagePlacer::new)
	);
	protected final int height;

	public MegaJungleFoliagePlacer(int i, int j, int k, int l, int m) {
		super(i, j, k, l);
		this.height = m;
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.MEGA_JUNGLE_FOLIAGE_PLACER;
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
		int m = foliageAttachment.doubleTrunk() ? j : 1 + random.nextInt(2);

		for (int n = l; n >= l - m; n--) {
			int o = k + foliageAttachment.radiusOffset() + 1 - n;
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), o, set, n, foliageAttachment.doubleTrunk());
		}
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return this.height;
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return i + k >= 7 ? true : i * i + k * k > l * l;
	}
}
