/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.surfacebuilders.NoiseMaterialSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import org.jetbrains.annotations.Nullable;

public class StonyPeaksSurfaceBuilder
extends NoiseMaterialSurfaceBuilder {
    public StonyPeaksSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override
    @Nullable
    protected NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial() {
        return null;
    }

    @Override
    protected BlockState getTopMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j) {
        return this.getMaterial(0.015, i, j, Blocks.STONE.defaultBlockState(), Blocks.CALCITE.defaultBlockState(), -0.0125, 0.0125);
    }

    @Override
    protected BlockState getMidMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j) {
        return this.getMaterial(0.015, i, j, Blocks.STONE.defaultBlockState(), Blocks.CALCITE.defaultBlockState(), -0.0125, 0.0125);
    }
}

