/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GameMasterBlockItem
extends BlockItem {
    public GameMasterBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
        Player player = blockPlaceContext.getPlayer();
        return player == null || player.canUseGameMasterBlocks() ? super.getPlacementState(blockPlaceContext) : null;
    }
}

