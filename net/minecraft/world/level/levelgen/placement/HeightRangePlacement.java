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
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class HeightRangePlacement
extends PlacementModifier {
    public static final Codec<HeightRangePlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)HeightProvider.CODEC.fieldOf("height")).forGetter(heightRangePlacement -> heightRangePlacement.height)).apply((Applicative<HeightRangePlacement, ?>)instance, HeightRangePlacement::new));
    private final HeightProvider height;

    private HeightRangePlacement(HeightProvider heightProvider) {
        this.height = heightProvider;
    }

    public static HeightRangePlacement of(HeightProvider heightProvider) {
        return new HeightRangePlacement(heightProvider);
    }

    public static HeightRangePlacement uniform(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
        return HeightRangePlacement.of(UniformHeight.of(verticalAnchor, verticalAnchor2));
    }

    public static HeightRangePlacement triangle(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
        return HeightRangePlacement.of(TrapezoidHeight.of(verticalAnchor, verticalAnchor2));
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos) {
        return Stream.of(blockPos.atY(this.height.sample(random, placementContext)));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.HEIGHT_RANGE;
    }
}

