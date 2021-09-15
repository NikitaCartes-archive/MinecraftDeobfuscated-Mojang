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

public class StoneShoreSurfaceBuilder
extends NoiseMaterialSurfaceBuilder {
    public StoneShoreSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override
    @Nullable
    protected NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial() {
        return null;
    }

    @Override
    protected BlockState getTopMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j) {
        return this.getMaterial(0.03175, i, j, Blocks.STONE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), -0.05, 0.05);
    }

    @Override
    protected BlockState getMidMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j) {
        return this.getMaterial(0.03175, i, j, Blocks.STONE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), -0.05, 0.05);
    }
}

