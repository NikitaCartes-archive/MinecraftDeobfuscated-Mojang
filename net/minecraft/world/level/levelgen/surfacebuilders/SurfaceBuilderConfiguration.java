/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import net.minecraft.world.level.block.state.BlockState;

public interface SurfaceBuilderConfiguration {
    public BlockState getTopMaterial();

    public BlockState getUnderMaterial();

    public BlockState getUnderwaterMaterial();
}

