/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BedBlockEntity
extends BlockEntity {
    private DyeColor color;

    public BedBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.BED, blockPos, blockState);
        this.color = ((BedBlock)blockState.getBlock()).getColor();
    }

    public BedBlockEntity(BlockPos blockPos, BlockState blockState, DyeColor dyeColor) {
        super(BlockEntityType.BED, blockPos, blockState);
        this.color = dyeColor;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 11, this.getUpdateTag());
    }

    @Environment(value=EnvType.CLIENT)
    public DyeColor getColor() {
        return this.color;
    }

    @Environment(value=EnvType.CLIENT)
    public void setColor(DyeColor dyeColor) {
        this.color = dyeColor;
    }
}

