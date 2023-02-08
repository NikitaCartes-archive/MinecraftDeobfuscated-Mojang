/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.EntityGetter;
import org.jetbrains.annotations.Nullable;

public interface OwnableEntity {
    @Nullable
    public UUID getOwnerUUID();

    public EntityGetter getLevel();

    @Nullable
    default public LivingEntity getOwner() {
        UUID uUID = this.getOwnerUUID();
        if (uUID == null) {
            return null;
        }
        return this.getLevel().getPlayerByUUID(uUID);
    }
}

