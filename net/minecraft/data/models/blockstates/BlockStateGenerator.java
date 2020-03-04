/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.models.blockstates;

import com.google.gson.JsonElement;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;

public interface BlockStateGenerator
extends Supplier<JsonElement> {
    public Block getBlock();
}

