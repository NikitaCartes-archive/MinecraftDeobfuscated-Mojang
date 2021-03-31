/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface OwnableEntity {
    @Nullable
    public UUID getOwnerUUID();

    @Nullable
    public Entity getOwner();
}

