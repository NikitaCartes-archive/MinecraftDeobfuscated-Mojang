/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.attributes;

import java.util.Collection;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

public interface AttributeInstance {
    public Attribute getAttribute();

    public double getBaseValue();

    public void setBaseValue(double var1);

    public Collection<AttributeModifier> getModifiers(AttributeModifier.Operation var1);

    public Collection<AttributeModifier> getModifiers();

    public boolean hasModifier(AttributeModifier var1);

    @Nullable
    public AttributeModifier getModifier(UUID var1);

    public void addModifier(AttributeModifier var1);

    public void removeModifier(AttributeModifier var1);

    public void removeModifier(UUID var1);

    @Environment(value=EnvType.CLIENT)
    public void removeModifiers();

    public double getValue();
}

