/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CaveDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class CaveSurfaceDecorator
extends FeatureDecorator<CaveDecoratorConfiguration> {
    public CaveSurfaceDecorator(Codec<CaveDecoratorConfiguration> codec) {
        super(codec);
    }

    public static boolean isAirOrWater(BlockState blockState) {
        return blockState.isAir() || blockState.is(Blocks.WATER);
    }

    private static Predicate<BlockState> getInsideColumnPredicate(boolean bl) {
        if (bl) {
            return CaveSurfaceDecorator::isAirOrWater;
        }
        return BlockBehaviour.BlockStateBase::isAir;
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, CaveDecoratorConfiguration caveDecoratorConfiguration, BlockPos blockPos) {
        OptionalInt optionalInt;
        Optional<Column> optional = Column.scan(decorationContext.getLevel(), blockPos, caveDecoratorConfiguration.floorToCeilingSearchRange, CaveSurfaceDecorator.getInsideColumnPredicate(caveDecoratorConfiguration.allowWater), blockState -> blockState.getMaterial().isSolid());
        if (optional.isEmpty()) {
            return Stream.of(new BlockPos[0]);
        }
        OptionalInt optionalInt2 = optionalInt = caveDecoratorConfiguration.surface == CaveSurface.CEILING ? optional.get().getCeiling() : optional.get().getFloor();
        if (optionalInt.isEmpty()) {
            return Stream.of(new BlockPos[0]);
        }
        return Stream.of(blockPos.atY(optionalInt.getAsInt() - caveDecoratorConfiguration.surface.getY()));
    }

    @Override
    public /* synthetic */ Stream getPositions(DecorationContext decorationContext, Random random, DecoratorConfiguration decoratorConfiguration, BlockPos blockPos) {
        return this.getPositions(decorationContext, random, (CaveDecoratorConfiguration)decoratorConfiguration, blockPos);
    }
}

