/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FireChargeItem
extends Item {
    public FireChargeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.getBlock() == Blocks.CAMPFIRE) {
            if (!blockState.getValue(CampfireBlock.LIT).booleanValue() && !blockState.getValue(CampfireBlock.WATERLOGGED).booleanValue()) {
                this.playSound(level, blockPos);
                level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(CampfireBlock.LIT, true));
            }
        } else if (level.getBlockState(blockPos = blockPos.relative(useOnContext.getClickedFace())).isAir()) {
            this.playSound(level, blockPos);
            level.setBlockAndUpdate(blockPos, ((FireBlock)Blocks.FIRE).getStateForPlacement(level, blockPos));
        }
        useOnContext.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    private void playSound(Level level, BlockPos blockPos) {
        level.playSound(null, blockPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f);
    }
}

