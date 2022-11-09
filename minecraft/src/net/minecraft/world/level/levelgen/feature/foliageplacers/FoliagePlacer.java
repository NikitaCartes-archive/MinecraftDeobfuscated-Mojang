package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products.P2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
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
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		TreeConfiguration treeConfiguration,
		int i,
		FoliagePlacer.FoliageAttachment foliageAttachment,
		int j,
		int k
	) {
		this.createFoliage(levelSimulatedReader, biConsumer, randomSource, treeConfiguration, i, foliageAttachment, j, k, this.offset(randomSource));
	}

	protected abstract void createFoliage(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
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
		BiConsumer<BlockPos, BlockState> biConsumer,
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
					tryPlaceLeaf(levelSimulatedReader, biConsumer, randomSource, treeConfiguration, mutableBlockPos);
				}
			}
		}
	}

	protected static void tryPlaceLeaf(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		TreeConfiguration treeConfiguration,
		BlockPos blockPos
	) {
		if (TreeFeature.validTreePos(levelSimulatedReader, blockPos)) {
			BlockState blockState = treeConfiguration.foliageProvider.getState(randomSource, blockPos);
			if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
				blockState = blockState.setValue(
					BlockStateProperties.WATERLOGGED, Boolean.valueOf(levelSimulatedReader.isFluidAtPosition(blockPos, fluidState -> fluidState.isSourceOfType(Fluids.WATER)))
				);
			}

			biConsumer.accept(blockPos, blockState);
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
}
