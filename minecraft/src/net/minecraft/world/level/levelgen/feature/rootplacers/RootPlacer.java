package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public abstract class RootPlacer {
	public static final Codec<RootPlacer> CODEC = Registry.ROOT_PLACER_TYPES.byNameCodec().dispatch(RootPlacer::type, RootPlacerType::codec);
	protected final BlockStateProvider rootProvider;

	protected static <P extends RootPlacer> P1<Mu<P>, BlockStateProvider> rootPlacerParts(Instance<P> instance) {
		return instance.group(BlockStateProvider.CODEC.fieldOf("root_provider").forGetter(rootPlacer -> rootPlacer.rootProvider));
	}

	public RootPlacer(BlockStateProvider blockStateProvider) {
		this.rootProvider = blockStateProvider;
	}

	protected abstract RootPlacerType<?> type();

	public abstract Optional<BlockPos> placeRoots(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	);

	protected void placeRoot(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	) {
		biConsumer.accept(blockPos, this.getPotentiallyWaterloggedState(levelSimulatedReader, blockPos, this.rootProvider.getState(randomSource, blockPos)));
	}

	protected BlockState getPotentiallyWaterloggedState(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, BlockState blockState) {
		if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
			boolean bl = levelSimulatedReader.isFluidAtPosition(blockPos, fluidState -> fluidState.is(FluidTags.WATER));
			return blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(bl));
		} else {
			return blockState;
		}
	}
}
