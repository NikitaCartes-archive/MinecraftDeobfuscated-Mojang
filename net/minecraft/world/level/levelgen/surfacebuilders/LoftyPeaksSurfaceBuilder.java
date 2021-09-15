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

public class LoftyPeaksSurfaceBuilder
extends NoiseMaterialSurfaceBuilder {
    private final NoiseMaterialSurfaceBuilder.SteepMaterial steepMaterial = new NoiseMaterialSurfaceBuilder.SteepMaterial(Blocks.STONE.defaultBlockState(), true, false, false, true);

    public LoftyPeaksSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override
    @Nullable
    protected NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial() {
        return this.steepMaterial;
    }

    @Override
    protected BlockState getTopMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j) {
        return surfaceBuilderBaseConfiguration.getTopMaterial();
    }

    @Override
    protected BlockState getMidMaterial(SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration, int i, int j) {
        return surfaceBuilderBaseConfiguration.getUnderMaterial();
    }
}

