/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnderChestBlockEntity
extends BlockEntity
implements LidBlockEntity {
    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter(){

        @Override
        protected void onOpen(Level level, BlockPos blockPos, BlockState blockState) {
            level.playSound(null, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f);
        }

        @Override
        protected void onClose(Level level, BlockPos blockPos, BlockState blockState) {
            level.playSound(null, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.1f + 0.9f);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos blockPos, BlockState blockState, int i, int j) {
            level.blockEvent(EnderChestBlockEntity.this.worldPosition, Blocks.ENDER_CHEST, 1, j);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            return player.getEnderChestInventory().isActiveChest(EnderChestBlockEntity.this);
        }
    };

    public EnderChestBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.ENDER_CHEST, blockPos, blockState);
    }

    public static void lidAnimateTick(Level level, BlockPos blockPos, BlockState blockState, EnderChestBlockEntity enderChestBlockEntity) {
        enderChestBlockEntity.chestLidController.tickLid();
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            this.chestLidController.shouldBeOpen(j > 0);
            return true;
        }
        return super.triggerEvent(i, j);
    }

    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return !(player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) > 64.0);
    }

    public void recheckOpen() {
        this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
    }

    @Override
    public float getOpenNess(float f) {
        return this.chestLidController.getOpenness(f);
    }
}

