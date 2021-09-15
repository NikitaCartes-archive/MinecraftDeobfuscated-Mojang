/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.material;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.jetbrains.annotations.Nullable;

public interface WorldGenMaterialRule {
    @Nullable
    public BlockState apply(NoiseChunk var1, int var2, int var3, int var4);
}

