package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products.P5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
	public static final Codec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> blobParts(instance).apply(instance, BlobFoliagePlacer::new));
	protected final int height;

	protected static <P extends BlobFoliagePlacer> P5<Mu<P>, Integer, Integer, Integer, Integer, Integer> blobParts(Instance<P> instance) {
		return foliagePlacerParts(instance).and(Codec.INT.fieldOf("height").forGetter(blobFoliagePlacer -> blobFoliagePlacer.height));
	}

	protected BlobFoliagePlacer(int i, int j, int k, int l, int m, FoliagePlacerType<?> foliagePlacerType) {
		super(i, j, k, l);
		this.height = m;
	}

	public BlobFoliagePlacer(int i, int j, int k, int l, int m) {
		this(i, j, k, l, m, FoliagePlacerType.BLOB_FOLIAGE_PLACER);
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
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
		for (int m = l; m >= l - j; m--) {
			int n = Math.max(k + foliageAttachment.radiusOffset() - 1 - m / 2, 0);
			this.placeLeavesRow(levelSimulatedRW, random, treeConfiguration, foliageAttachment.foliagePos(), n, set, m, foliageAttachment.doubleTrunk());
		}
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return this.height;
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return i == l && k == l && (random.nextInt(2) == 0 || j == 0);
	}
}
