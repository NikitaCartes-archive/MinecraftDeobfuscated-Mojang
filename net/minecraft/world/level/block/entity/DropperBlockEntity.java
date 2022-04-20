/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DropperBlockEntity
extends DispenserBlockEntity {
    public DropperBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.DROPPER, blockPos, blockState);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.dropper");
    }
}

