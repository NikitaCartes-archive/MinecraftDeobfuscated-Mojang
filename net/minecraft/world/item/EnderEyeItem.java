/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EnderEyeItem
extends Item {
    public EnderEyeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (blockState.getBlock() != Blocks.END_PORTAL_FRAME || blockState.getValue(EndPortalFrameBlock.HAS_EYE).booleanValue()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockState blockState2 = (BlockState)blockState.setValue(EndPortalFrameBlock.HAS_EYE, true);
        Block.pushEntitiesUp(blockState, blockState2, level, blockPos);
        level.setBlock(blockPos, blockState2, 2);
        level.updateNeighbourForOutputSignal(blockPos, Blocks.END_PORTAL_FRAME);
        useOnContext.getItemInHand().shrink(1);
        level.levelEvent(1503, blockPos, 0);
        BlockPattern.BlockPatternMatch blockPatternMatch = EndPortalFrameBlock.getOrCreatePortalShape().find(level, blockPos);
        if (blockPatternMatch != null) {
            BlockPos blockPos2 = blockPatternMatch.getFrontTopLeft().offset(-3, 0, -3);
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    level.setBlock(blockPos2.offset(i, 0, j), Blocks.END_PORTAL.defaultBlockState(), 2);
                }
            }
            level.globalLevelEvent(1038, blockPos2.offset(1, 0, 1), 0);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        BlockPos blockPos;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        HitResult hitResult = EnderEyeItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitResult.getType() == HitResult.Type.BLOCK && level.getBlockState(((BlockHitResult)hitResult).getBlockPos()).getBlock() == Blocks.END_PORTAL_FRAME) {
            return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStack);
        }
        player.startUsingItem(interactionHand);
        if (!level.isClientSide && (blockPos = level.getChunkSource().getGenerator().findNearestMapFeature(level, "Stronghold", new BlockPos(player), 100, false)) != null) {
            EyeOfEnder eyeOfEnder = new EyeOfEnder(level, player.x, player.y + (double)(player.getBbHeight() / 2.0f), player.z);
            eyeOfEnder.setItem(itemStack);
            eyeOfEnder.signalTo(blockPos);
            level.addFreshEntity(eyeOfEnder);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayer)player, blockPos);
            }
            level.playSound(null, player.x, player.y, player.z, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
            level.levelEvent(null, 1003, new BlockPos(player), 0);
            if (!player.abilities.instabuild) {
                itemStack.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStack);
        }
        return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStack);
    }
}

