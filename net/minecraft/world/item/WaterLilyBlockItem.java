/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class WaterLilyBlockItem
extends BlockItem {
    public WaterLilyBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        HitResult hitResult = WaterLilyBlockItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStack);
        }
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getDirection();
            if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos.relative(direction), direction, itemStack)) {
                return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, itemStack);
            }
            BlockPos blockPos2 = blockPos.above();
            BlockState blockState = level.getBlockState(blockPos);
            Material material = blockState.getMaterial();
            FluidState fluidState = level.getFluidState(blockPos);
            if ((fluidState.getType() == Fluids.WATER || material == Material.ICE) && level.isEmptyBlock(blockPos2)) {
                level.setBlock(blockPos2, Blocks.LILY_PAD.defaultBlockState(), 11);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos2, itemStack);
                }
                if (!player.abilities.instabuild) {
                    itemStack.shrink(1);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                level.playSound(player, blockPos, SoundEvents.LILY_PAD_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStack);
            }
        }
        return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, itemStack);
    }
}

