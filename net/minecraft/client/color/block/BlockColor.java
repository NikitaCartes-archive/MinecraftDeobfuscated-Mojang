/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.color.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface BlockColor {
    public int getColor(BlockState var1, @Nullable BlockAndBiomeGetter var2, @Nullable BlockPos var3, int var4);
}

