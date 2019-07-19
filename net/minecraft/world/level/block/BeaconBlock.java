/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BeaconBlock
extends BaseEntityBlock
implements BeaconBeamBlock {
    public BeaconBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new BeaconBlockEntity();
    }

    @Override
    public boolean use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return true;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BeaconBlockEntity) {
            player.openMenu((BeaconBlockEntity)blockEntity);
            player.awardStat(Stats.INTERACT_WITH_BEACON);
        }
        return true;
    }

    @Override
    public boolean isRedstoneConductor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        BlockEntity blockEntity;
        if (itemStack.hasCustomHoverName() && (blockEntity = level.getBlockEntity(blockPos)) instanceof BeaconBlockEntity) {
            ((BeaconBlockEntity)blockEntity).setCustomName(itemStack.getHoverName());
        }
    }

    @Override
    public BlockLayer getRenderLayer() {
        return BlockLayer.CUTOUT;
    }
}

