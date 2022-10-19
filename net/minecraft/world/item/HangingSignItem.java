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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HangingSignItem
extends StandingAndWallBlockItem {
    public HangingSignItem(Block block, Block block2, Item.Properties properties) {
        super(block, block2, properties, Direction.UP);
    }

    @Override
    protected boolean canPlace(LevelReader levelReader, BlockState blockState, BlockPos blockPos) {
        WallHangingSignBlock wallHangingSignBlock;
        Block block = blockState.getBlock();
        if (block instanceof WallHangingSignBlock && !(wallHangingSignBlock = (WallHangingSignBlock)block).canPlace(blockState, levelReader, blockPos)) {
            return false;
        }
        return super.canPlace(levelReader, blockState, blockPos);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos blockPos, Level level, @Nullable Player player, ItemStack itemStack, BlockState blockState) {
        BlockEntity blockEntity;
        boolean bl = super.updateCustomBlockEntityTag(blockPos, level, player, itemStack, blockState);
        if (!level.isClientSide && !bl && player != null && (blockEntity = level.getBlockEntity(blockPos)) instanceof SignBlockEntity) {
            SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
            player.openTextEdit(signBlockEntity);
        }
        return bl;
    }
}

