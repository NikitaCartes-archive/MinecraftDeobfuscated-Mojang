/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PumpkinBlock
extends StemGrownBlock {
    protected PumpkinBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player2, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemStack = player2.getItemInHand(interactionHand);
        if (itemStack.getItem() == Items.SHEARS) {
            if (!level.isClientSide) {
                Direction direction = blockHitResult.getDirection();
                Direction direction2 = direction.getAxis() == Direction.Axis.Y ? player2.getDirection().getOpposite() : direction;
                level.playSound(null, blockPos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.setBlock(blockPos, (BlockState)Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, direction2), 11);
                ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX() + 0.5 + (double)direction2.getStepX() * 0.65, (double)blockPos.getY() + 0.1, (double)blockPos.getZ() + 0.5 + (double)direction2.getStepZ() * 0.65, new ItemStack(Items.PUMPKIN_SEEDS, 4));
                itemEntity.setDeltaMovement(0.05 * (double)direction2.getStepX() + level.random.nextDouble() * 0.02, 0.05, 0.05 * (double)direction2.getStepZ() + level.random.nextDouble() * 0.02);
                level.addFreshEntity(itemEntity);
                itemStack.hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(interactionHand));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(blockState, level, blockPos, player2, interactionHand, blockHitResult);
    }

    @Override
    public StemBlock getStem() {
        return (StemBlock)Blocks.PUMPKIN_STEM;
    }

    @Override
    public AttachedStemBlock getAttachedStem() {
        return (AttachedStemBlock)Blocks.ATTACHED_PUMPKIN_STEM;
    }
}

