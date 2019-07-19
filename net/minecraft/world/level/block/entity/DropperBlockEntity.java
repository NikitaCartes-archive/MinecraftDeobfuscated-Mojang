/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;

public class DropperBlockEntity
extends DispenserBlockEntity {
    public DropperBlockEntity() {
        super(BlockEntityType.DROPPER);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.dropper", new Object[0]);
    }
}

