/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface TraceableEntity {
    @Nullable
    public Entity getOwner();
}

