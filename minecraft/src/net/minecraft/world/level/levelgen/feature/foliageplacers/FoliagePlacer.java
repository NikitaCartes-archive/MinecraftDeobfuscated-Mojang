package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products.P2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class FoliagePlacer {
	public static final Codec<FoliagePlacer> CODEC = Registry.FOLIAGE_PLACER_TYPES.dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
	protected final UniformInt radius;
	protected final UniformInt offset;

	protected static <P extends FoliagePlacer> P2<Mu<P>, UniformInt, UniformInt> foliagePlacerParts(Instance<P> instance) {
		return instance.group(
			UniformInt.codec(0, 8, 8).fieldOf("radius").forGetter(foliagePlacer -> foliagePlacer.radius),
			UniformInt.codec(0, 8, 8).fieldOf("offset").forGetter(foliagePlacer -> foliagePlacer.offset)
		);
	}

	public FoliagePlacer(UniformInt uniformInt, UniformInt uniformInt2) {
		this.radius = uniformInt;
		this.offset = uniformInt2;
	}

	protected abstract FoliagePlacerType<?> type();

	public void createFoliage(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		TreeConfiguration treeConfiguration,
		int i,
		FoliagePlacer.FoliageAttachment foliageAttachment,
		int j,
		int k,
		Set<BlockPos> set,
		BoundingBox boundingBox
	) {
		this.createFoliage(levelSimulatedRW, random, treeConfiguration, i, foliageAttachment, j, k, set, this.offset(random), boundingBox);
	}

	protected abstract void createFoliage(
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
	);

	public abstract int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration);

	public int foliageRadius(Random random, int i) {
		return this.radius.sample(random);
	}

	private int offset(Random random) {
		return this.offset.sample(random);
	}

	protected abstract boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl);

	protected boolean shouldSkipLocationSigned(Random random, int i, int j, int k, int l, boolean bl) {
		int m;
		int n;
		if (bl) {
			m = Math.min(Math.abs(i), Math.abs(i - 1));
			n = Math.min(Math.abs(k), Math.abs(k - 1));
		} else {
			m = Math.abs(i);
			n = Math.abs(k);
		}

		return this.shouldSkipLocation(random, m, j, n, l, bl);
	}

	protected void placeLeavesRow(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		TreeConfiguration treeConfiguration,
		BlockPos blockPos,
		int i,
		Set<BlockPos> set,
		int j,
		boolean bl,
		BoundingBox boundingBox
	) {
		int k = bl ? 1 : 0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int l = -i; l <= i + k; l++) {
			for (int m = -i; m <= i + k; m++) {
				if (!this.shouldSkipLocationSigned(random, l, j, m, i, bl)) {
					mutableBlockPos.setWithOffset(blockPos, l, j, m);
					if (TreeFeature.validTreePos(levelSimulatedRW, mutableBlockPos)) {
						levelSimulatedRW.setBlock(mutableBlockPos, treeConfiguration.leavesProvider.getState(random, mutableBlockPos), 19);
						boundingBox.expand(new BoundingBox(mutableBlockPos, mutableBlockPos));
						set.add(mutableBlockPos.immutable());
					}
				}
			}
		}
	}

	public static final class FoliageAttachment {
		private final BlockPos foliagePos;
		private final int radiusOffset;
		private final boolean doubleTrunk;

		public FoliageAttachment(BlockPos blockPos, int i, boolean bl) {
			this.foliagePos = blockPos;
			this.radiusOffset = i;
			this.doubleTrunk = bl;
		}

		public BlockPos foliagePos() {
			return this.foliagePos;
		}

		public int radiusOffset() {
			return this.radiusOffset;
		}

		public boolean doubleTrunk() {
			return this.doubleTrunk;
		}
	}
}
