/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core.dispenser;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class ShearsDispenseItemBehavior
extends OptionalDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        ServerLevel level = blockSource.getLevel();
        if (!level.isClientSide()) {
            BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
            this.setSuccess(ShearsDispenseItemBehavior.tryShearBeehive(level, blockPos) || ShearsDispenseItemBehavior.tryShearLivingEntity(level, blockPos));
            if (this.isSuccess() && itemStack.hurt(1, level.getRandom(), null)) {
                itemStack.setCount(0);
            }
        }
        return itemStack;
    }

    private static boolean tryShearBeehive(ServerLevel serverLevel, BlockPos blockPos) {
        int i;
        BlockState blockState = serverLevel.getBlockState(blockPos);
        if (blockState.is(BlockTags.BEEHIVES) && (i = blockState.getValue(BeehiveBlock.HONEY_LEVEL).intValue()) >= 5) {
            serverLevel.playSound(null, blockPos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
            BeehiveBlock.dropHoneycomb(serverLevel, blockPos);
            ((BeehiveBlock)blockState.getBlock()).releaseBeesAndResetHoneyLevel(serverLevel, blockState, blockPos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
            serverLevel.gameEvent(null, GameEvent.SHEAR, blockPos);
            return true;
        }
        return false;
    }

    private static boolean tryShearLivingEntity(ServerLevel serverLevel, BlockPos blockPos) {
        List<Entity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos), EntitySelector.NO_SPECTATORS);
        for (LivingEntity livingEntity : list) {
            Shearable shearable;
            if (!(livingEntity instanceof Shearable) || !(shearable = (Shearable)((Object)livingEntity)).readyForShearing()) continue;
            shearable.shear(SoundSource.BLOCKS);
            serverLevel.gameEvent(null, GameEvent.SHEAR, blockPos);
            return true;
        }
        return false;
    }
}

