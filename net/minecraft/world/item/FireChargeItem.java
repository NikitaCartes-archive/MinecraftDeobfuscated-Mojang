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
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FireChargeItem
extends Item {
    public FireChargeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        boolean bl = false;
        if (CampfireBlock.canLight(blockState)) {
            this.playSound(level, blockPos);
            level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(CampfireBlock.LIT, true));
            bl = true;
        } else if (BaseFireBlock.canBePlacedAt(level, blockPos = blockPos.relative(useOnContext.getClickedFace()))) {
            this.playSound(level, blockPos);
            level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(level, blockPos));
            bl = true;
        }
        if (bl) {
            useOnContext.getItemInHand().shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }

    private void playSound(Level level, BlockPos blockPos) {
        level.playSound(null, blockPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f);
    }
}

