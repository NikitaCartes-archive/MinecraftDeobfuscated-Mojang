/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public abstract class RootPlacer {
    public static final Codec<RootPlacer> CODEC = Registry.ROOT_PLACER_TYPES.byNameCodec().dispatch(RootPlacer::type, RootPlacerType::codec);
    protected final BlockStateProvider rootProvider;

    protected static <P extends RootPlacer> Products.P1<RecordCodecBuilder.Mu<P>, BlockStateProvider> rootPlacerParts(RecordCodecBuilder.Instance<P> instance) {
        return instance.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("root_provider")).forGetter(rootPlacer -> rootPlacer.rootProvider));
    }

    public RootPlacer(BlockStateProvider blockStateProvider) {
        this.rootProvider = blockStateProvider;
    }

    protected abstract RootPlacerType<?> type();

    public abstract Optional<BlockPos> placeRoots(LevelSimulatedReader var1, BiConsumer<BlockPos, BlockState> var2, RandomSource var3, BlockPos var4, TreeConfiguration var5);

    protected void placeRoot(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        biConsumer.accept(blockPos, this.getPotentiallyWaterloggedState(levelSimulatedReader, blockPos, this.rootProvider.getState(randomSource, blockPos)));
    }

    protected BlockState getPotentiallyWaterloggedState(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, BlockState blockState) {
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            boolean bl = levelSimulatedReader.isFluidAtPosition(blockPos, fluidState -> fluidState.is(FluidTags.WATER));
            return (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, bl);
        }
        return blockState;
    }
}

