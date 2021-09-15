/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public record BlockEventData(BlockPos pos, Block block, int paramA, int paramB) {
}

