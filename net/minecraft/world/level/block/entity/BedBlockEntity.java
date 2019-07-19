/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BedBlockEntity
extends BlockEntity {
    private DyeColor color;

    public BedBlockEntity() {
        super(BlockEntityType.BED);
    }

    public BedBlockEntity(DyeColor dyeColor) {
        this();
        this.setColor(dyeColor);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 11, this.getUpdateTag());
    }

    @Environment(value=EnvType.CLIENT)
    public DyeColor getColor() {
        if (this.color == null) {
            this.color = ((BedBlock)this.getBlockState().getBlock()).getColor();
        }
        return this.color;
    }

    public void setColor(DyeColor dyeColor) {
        this.color = dyeColor;
    }
}

