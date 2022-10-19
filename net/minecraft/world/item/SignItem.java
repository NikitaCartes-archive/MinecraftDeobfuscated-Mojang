/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SignItem
extends StandingAndWallBlockItem {
    public SignItem(Item.Properties properties, Block block, Block block2) {
        super(block, block2, properties, Direction.DOWN);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos blockPos, Level level, @Nullable Player player, ItemStack itemStack, BlockState blockState) {
        boolean bl = super.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState);
        if (!level.isClientSide && !bl && player != null) {
            player.openTextEdit((SignBlockEntity)level.getBlockEntity(blockPos));
        }
        return bl;
    }
}

