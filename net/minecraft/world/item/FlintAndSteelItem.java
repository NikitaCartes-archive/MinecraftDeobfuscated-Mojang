/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FlintAndSteelItem
extends Item {
    public FlintAndSteelItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Player player2 = useOnContext.getPlayer();
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (CampfireBlock.canLight(blockState) || CandleBlock.canLight(blockState) || CandleCakeBlock.canLight(blockState)) {
            level.playSound(player2, blockPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.4f + 0.8f);
            level.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.LIT, true), 11);
            if (player2 != null) {
                useOnContext.getItemInHand().hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(useOnContext.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        BlockPos blockPos2 = blockPos.relative(useOnContext.getClickedFace());
        if (BaseFireBlock.canBePlacedAt(level, blockPos2, useOnContext.getHorizontalDirection())) {
            level.playSound(player2, blockPos2, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, level.getRandom().nextFloat() * 0.4f + 0.8f);
            BlockState blockState2 = BaseFireBlock.getState(level, blockPos2);
            level.setBlock(blockPos2, blockState2, 11);
            ItemStack itemStack = useOnContext.getItemInHand();
            if (player2 instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player2, blockPos2, itemStack);
                itemStack.hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(useOnContext.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.FAIL;
    }
}

