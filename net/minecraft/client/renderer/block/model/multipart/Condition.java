/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model.multipart;

import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface Condition {
    public static final Condition TRUE = stateDefinition -> blockState -> true;
    public static final Condition FALSE = stateDefinition -> blockState -> false;

    public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> var1);
}

