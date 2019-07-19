/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.attributes;

import org.jetbrains.annotations.Nullable;

public interface Attribute {
    public String getName();

    public double sanitizeValue(double var1);

    public double getDefaultValue();

    public boolean isClientSyncable();

    @Nullable
    public Attribute getParentAttribute();
}

