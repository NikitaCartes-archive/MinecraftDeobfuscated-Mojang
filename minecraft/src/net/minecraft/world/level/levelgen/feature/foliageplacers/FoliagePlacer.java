package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products.P2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.Fluids;

public abstract class FoliagePlacer {
	public static final Codec<FoliagePlacer> CODEC = BuiltInRegistries.FOLIAGE_PLACER_TYPE.byNameCodec().dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
	protected final IntProvider radius;
	protected final IntProvider offset;

	protected static <P extends FoliagePlacer> P2<Mu<P>, IntProvider, IntProvider> foliagePlacerParts(Instance<P> instance) {
		return instance.group(
			IntProvider.codec(0, 16).fieldOf("radius").forGetter(foliagePlacer -> foliagePlacer.radius),
			IntProvider.codec(0, 16).fieldOf("offset").forGetter(foliagePlacer -> foliagePlacer.offset)
		);
	}

	public FoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
		this.radius = intProvider;
		this.offset = intProvider2;
	}

	protected abstract FoliagePlacerType<?> type();

	public void createFoliage(
		LevelSimulatedReader levelSimulatedReader,
		FoliagePlacer.FoliageSetter foliageSetter,
		RandomSource randomSource,
		TreeConfiguration treeConfiguration,
		int i,
		FoliagePlacer.FoliageAttachment foliageAttachment,
		int j,
		int k
	) {
		this.createFoliage(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, i, foliageAttachment, j, k, this.offset(randomSource));
	}

	protected abstract void createFoliage(
		LevelSimulatedReader levelSimulatedReader,
		FoliagePlacer.FoliageSetter foliageSetter,
		RandomSource randomSource,
		TreeConfiguration treeConfiguration,
		int i,
		FoliagePlacer.FoliageAttachment foliageAttachment,
		int j,
		int k,
		int l
	);

	public abstract int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration);

	public int foliageRadius(RandomSource randomSource, int i) {
		return this.radius.sample(randomSource);
	}

	private int offset(RandomSource randomSource) {
		return this.offset.sample(randomSource);
	}

	protected abstract boolean shouldSkipLocation(RandomSource randomSource, int i, int j, int k, int l, boolean bl);

	protected boolean shouldSkipLocationSigned(RandomSource randomSource, int i, int j, int k, int l, boolean bl) {
		int m;
		int n;
		if (bl) {
			m = Math.min(Math.abs(i), Math.abs(i - 1));
			n = Math.min(Math.abs(k), Math.abs(k - 1));
		} else {
			m = Math.abs(i);
			n = Math.abs(k);
		}

		return this.shouldSkipLocation(randomSource, m, j, n, l, bl);
	}

	protected void placeLeavesRow(
		LevelSimulatedReader levelSimulatedReader,
		FoliagePlacer.FoliageSetter foliageSetter,
		RandomSource randomSource,
		TreeConfiguration treeConfiguration,
		BlockPos blockPos,
		int i,
		int j,
		boolean bl
	) {
		int k = bl ? 1 : 0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int l = -i; l <= i + k; l++) {
			for (int m = -i; m <= i + k; m++) {
				if (!this.shouldSkipLocationSigned(randomSource, l, j, m, i, bl)) {
					mutableBlockPos.setWithOffset(blockPos, l, j, m);
					tryPlaceLeaf(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, mutableBlockPos);
				}
			}
		}
	}

	protected final void placeLeavesRowWithHangingLeavesBelow(
		LevelSimulatedReader levelSimulatedReader,
		FoliagePlacer.FoliageSetter foliageSetter,
		RandomSource randomSource,
		TreeConfiguration treeConfiguration,
		BlockPos blockPos,
		int i,
		int j,
		boolean bl,
		float f,
		float g
	) {
		this.placeLeavesRow(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, blockPos, i, j, bl);
		int k = bl ? 1 : 0;
		BlockPos blockPos2 = blockPos.below();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			Direction direction2 = direction.getClockWise();
			int l = direction2.getAxisDirection() == Direction.AxisDirection.POSITIVE ? i + k : i;
			mutableBlockPos.setWithOffset(blockPos, 0, j - 1, 0).move(direction2, l).move(direction, -i);
			int m = -i;

			while (m < i + k) {
				boolean bl2 = foliageSetter.isSet(mutableBlockPos.move(Direction.UP));
				mutableBlockPos.move(Direction.DOWN);
				if (bl2 && tryPlaceExtension(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, f, blockPos2, mutableBlockPos)) {
					mutableBlockPos.move(Direction.DOWN);
					tryPlaceExtension(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, g, blockPos2, mutableBlockPos);
					mutableBlockPos.move(Direction.UP);
				}

				m++;
				mutableBlockPos.move(direction);
			}
		}
	}

	private static boolean tryPlaceExtension(
		LevelSimulatedReader levelSimulatedReader,
		FoliagePlacer.FoliageSetter foliageSetter,
		RandomSource randomSource,
		TreeConfiguration treeConfiguration,
		float f,
		BlockPos blockPos,
		BlockPos.MutableBlockPos mutableBlockPos
	) {
		if (mutableBlockPos.distManhattan(blockPos) >= 7) {
			return false;
		} else {
			return randomSource.nextFloat() > f ? false : tryPlaceLeaf(levelSimulatedReader, foliageSetter, randomSource, treeConfiguration, mutableBlockPos);
		}
	}

	protected static boolean tryPlaceLeaf(
		LevelSimulatedReader levelSimulatedReader,
		FoliagePlacer.FoliageSetter foliageSetter,
		RandomSource randomSource,
		TreeConfiguration treeConfiguration,
		BlockPos blockPos
	) {
		if (!TreeFeature.validTreePos(levelSimulatedReader, blockPos)) {
			return false;
		} else {
			BlockState blockState = treeConfiguration.foliageProvider.getState(randomSource, blockPos);
			if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
				blockState = blockState.setValue(
					BlockStateProperties.WATERLOGGED, Boolean.valueOf(levelSimulatedReader.isFluidAtPosition(blockPos, fluidState -> fluidState.isSourceOfType(Fluids.WATER)))
				);
			}

			foliageSetter.set(blockPos, blockState);
			return true;
		}
	}

	public static final class FoliageAttachment {
		private final BlockPos pos;
		private final int radiusOffset;
		private final boolean doubleTrunk;

		public FoliageAttachment(BlockPos blockPos, int i, boolean bl) {
			this.pos = blockPos;
			this.radiusOffset = i;
			this.doubleTrunk = bl;
		}

		public BlockPos pos() {
			return this.pos;
		}

		public int radiusOffset() {
			return this.radiusOffset;
		}

		public boolean doubleTrunk() {
			return this.doubleTrunk;
		}
	}

	public interface FoliageSetter {
		void set(BlockPos blockPos, BlockState blockState);

		boolean isSet(BlockPos blockPos);
	}
}
