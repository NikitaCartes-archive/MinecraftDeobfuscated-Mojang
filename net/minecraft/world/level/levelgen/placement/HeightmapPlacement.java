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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class HeightmapPlacement
extends PlacementModifier {
    public static final Codec<HeightmapPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Heightmap.Types.CODEC.fieldOf("heightmap")).forGetter(heightmapPlacement -> heightmapPlacement.heightmap)).apply((Applicative<HeightmapPlacement, ?>)instance, HeightmapPlacement::new));
    private final Heightmap.Types heightmap;

    private HeightmapPlacement(Heightmap.Types types) {
        this.heightmap = types;
    }

    public static HeightmapPlacement onHeightmap(Heightmap.Types types) {
        return new HeightmapPlacement(types);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos) {
        int j;
        int i = blockPos.getX();
        int k = placementContext.getHeight(this.heightmap, i, j = blockPos.getZ());
        if (k > placementContext.getMinBuildHeight()) {
            return Stream.of(new BlockPos(i, k, j));
        }
        return Stream.of(new BlockPos[0]);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.HEIGHTMAP;
    }
}

