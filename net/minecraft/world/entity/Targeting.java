/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface Targeting {
    @Nullable
    public LivingEntity getTarget();
}

