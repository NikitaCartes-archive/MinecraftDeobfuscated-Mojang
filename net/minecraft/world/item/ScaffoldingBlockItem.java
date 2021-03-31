/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ScaffoldingBlockItem
extends BlockItem {
    public ScaffoldingBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext blockPlaceContext) {
        Block block;
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(block = this.getBlock())) {
            Direction direction = blockPlaceContext.isSecondaryUseActive() ? (blockPlaceContext.isInside() ? blockPlaceContext.getClickedFace().getOpposite() : blockPlaceContext.getClickedFace()) : (blockPlaceContext.getClickedFace() == Direction.UP ? blockPlaceContext.getHorizontalDirection() : Direction.UP);
            int i = 0;
            BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(direction);
            while (i < 7) {
                if (!level.isClientSide && !level.isInWorldBounds(mutableBlockPos)) {
                    Player player = blockPlaceContext.getPlayer();
                    int j = level.getMaxBuildHeight();
                    if (!(player instanceof ServerPlayer) || mutableBlockPos.getY() < j) break;
                    ((ServerPlayer)player).sendMessage(new TranslatableComponent("build.tooHigh", j - 1).withStyle(ChatFormatting.RED), ChatType.GAME_INFO, Util.NIL_UUID);
                    break;
                }
                blockState = level.getBlockState(mutableBlockPos);
                if (!blockState.is(this.getBlock())) {
                    if (!blockState.canBeReplaced(blockPlaceContext)) break;
                    return BlockPlaceContext.at(blockPlaceContext, mutableBlockPos, direction);
                }
                mutableBlockPos.move(direction);
                if (!direction.getAxis().isHorizontal()) continue;
                ++i;
            }
            return null;
        }
        if (ScaffoldingBlock.getDistance(level, blockPos) == 7) {
            return null;
        }
        return blockPlaceContext;
    }

    @Override
    protected boolean mustSurvive() {
        return false;
    }
}

