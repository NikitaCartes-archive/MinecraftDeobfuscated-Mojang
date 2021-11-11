/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class EnvironmentScanPlacement
extends PlacementModifier {
    private final Direction directionOfSearch;
    private final BlockPredicate targetCondition;
    private final BlockPredicate allowedSearchCondition;
    private final int maxSteps;
    public static final Codec<EnvironmentScanPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Direction.VERTICAL_CODEC.fieldOf("direction_of_search")).forGetter(environmentScanPlacement -> environmentScanPlacement.directionOfSearch), ((MapCodec)BlockPredicate.CODEC.fieldOf("target_condition")).forGetter(environmentScanPlacement -> environmentScanPlacement.targetCondition), BlockPredicate.CODEC.optionalFieldOf("allowed_search_condition", BlockPredicate.alwaysTrue()).forGetter(environmentScanPlacement -> environmentScanPlacement.allowedSearchCondition), ((MapCodec)Codec.intRange(1, 32).fieldOf("max_steps")).forGetter(environmentScanPlacement -> environmentScanPlacement.maxSteps)).apply((Applicative<EnvironmentScanPlacement, ?>)instance, EnvironmentScanPlacement::new));

    private EnvironmentScanPlacement(Direction direction, BlockPredicate blockPredicate, BlockPredicate blockPredicate2, int i) {
        this.directionOfSearch = direction;
        this.targetCondition = blockPredicate;
        this.allowedSearchCondition = blockPredicate2;
        this.maxSteps = i;
    }

    public static EnvironmentScanPlacement scanningFor(Direction direction, BlockPredicate blockPredicate, BlockPredicate blockPredicate2, int i) {
        return new EnvironmentScanPlacement(direction, blockPredicate, blockPredicate2, i);
    }

    public static EnvironmentScanPlacement scanningFor(Direction direction, BlockPredicate blockPredicate, int i) {
        return EnvironmentScanPlacement.scanningFor(direction, blockPredicate, BlockPredicate.alwaysTrue(), i);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        WorldGenLevel worldGenLevel = placementContext.getLevel();
        if (!this.allowedSearchCondition.test(worldGenLevel, mutableBlockPos)) {
            return Stream.of(new BlockPos[0]);
        }
        for (int i = 0; i < this.maxSteps; ++i) {
            if (this.targetCondition.test(worldGenLevel, mutableBlockPos)) {
                return Stream.of(mutableBlockPos);
            }
            mutableBlockPos.move(this.directionOfSearch);
            if (worldGenLevel.isOutsideBuildHeight(mutableBlockPos.getY())) {
                return Stream.of(new BlockPos[0]);
            }
            if (!this.allowedSearchCondition.test(worldGenLevel, mutableBlockPos)) break;
        }
        if (this.targetCondition.test(worldGenLevel, mutableBlockPos)) {
            return Stream.of(mutableBlockPos);
        }
        return Stream.of(new BlockPos[0]);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.ENVIRONMENT_SCAN;
    }
}

