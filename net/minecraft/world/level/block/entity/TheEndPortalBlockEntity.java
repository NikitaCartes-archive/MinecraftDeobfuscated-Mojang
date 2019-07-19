/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TheEndPortalBlockEntity
extends BlockEntity {
    public TheEndPortalBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    public TheEndPortalBlockEntity() {
        this(BlockEntityType.END_PORTAL);
    }

    @Environment(value=EnvType.CLIENT)
    public boolean shouldRenderFace(Direction direction) {
        return direction == Direction.UP;
    }
}

