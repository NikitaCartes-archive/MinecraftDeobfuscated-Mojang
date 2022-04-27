package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public abstract class RootPlacer {
	public static final Codec<RootPlacer> CODEC = Registry.ROOT_PLACER_TYPES.byNameCodec().dispatch(RootPlacer::type, RootPlacerType::codec);
	protected final IntProvider trunkOffsetY;
	protected final BlockStateProvider rootProvider;
	protected final Optional<AboveRootPlacement> aboveRootPlacement;

	protected static <P extends RootPlacer> P3<Mu<P>, IntProvider, BlockStateProvider, Optional<AboveRootPlacement>> rootPlacerParts(Instance<P> instance) {
		return instance.group(
			IntProvider.CODEC.fieldOf("trunk_offset_y").forGetter(rootPlacer -> rootPlacer.trunkOffsetY),
			BlockStateProvider.CODEC.fieldOf("root_provider").forGetter(rootPlacer -> rootPlacer.rootProvider),
			AboveRootPlacement.CODEC.optionalFieldOf("above_root_placement").forGetter(rootPlacer -> rootPlacer.aboveRootPlacement)
		);
	}

	public RootPlacer(IntProvider intProvider, BlockStateProvider blockStateProvider, Optional<AboveRootPlacement> optional) {
		this.trunkOffsetY = intProvider;
		this.rootProvider = blockStateProvider;
		this.aboveRootPlacement = optional;
	}

	protected abstract RootPlacerType<?> type();

	public abstract boolean placeRoots(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		BlockPos blockPos,
		BlockPos blockPos2,
		TreeConfiguration treeConfiguration
	);

	protected boolean canPlaceRoot(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return TreeFeature.validTreePos(levelSimulatedReader, blockPos);
	}

	protected void placeRoot(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	) {
		if (this.canPlaceRoot(levelSimulatedReader, blockPos)) {
			biConsumer.accept(blockPos, this.getPotentiallyWaterloggedState(levelSimulatedReader, blockPos, this.rootProvider.getState(randomSource, blockPos)));
			if (this.aboveRootPlacement.isPresent()) {
				AboveRootPlacement aboveRootPlacement = (AboveRootPlacement)this.aboveRootPlacement.get();
				BlockPos blockPos2 = blockPos.above();
				if (randomSource.nextFloat() < aboveRootPlacement.aboveRootPlacementChance()
					&& levelSimulatedReader.isStateAtPosition(blockPos2, BlockBehaviour.BlockStateBase::isAir)) {
					biConsumer.accept(
						blockPos2, this.getPotentiallyWaterloggedState(levelSimulatedReader, blockPos2, aboveRootPlacement.aboveRootProvider().getState(randomSource, blockPos2))
					);
				}
			}
		}
	}

	protected BlockState getPotentiallyWaterloggedState(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, BlockState blockState) {
		if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
			boolean bl = levelSimulatedReader.isFluidAtPosition(blockPos, fluidState -> fluidState.is(FluidTags.WATER));
			return blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(bl));
		} else {
			return blockState;
		}
	}

	public BlockPos getTrunkOrigin(BlockPos blockPos, RandomSource randomSource) {
		return blockPos.above(this.trunkOffsetY.sample(randomSource));
	}
}
