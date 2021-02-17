/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public interface BaseStoneSource {
    public BlockState getBaseStone(int var1, int var2, int var3, NoiseGeneratorSettings var4);
}

