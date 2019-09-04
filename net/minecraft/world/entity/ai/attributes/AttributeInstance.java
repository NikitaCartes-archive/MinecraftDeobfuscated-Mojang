/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
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

    public Set<AttributeModifier> getModifiers(AttributeModifier.Operation var1);

    public Set<AttributeModifier> getModifiers();

    public boolean hasModifier(AttributeModifier var1);

    @Nullable
    public AttributeModifier getModifier(UUID var1);

    public void addModifier(AttributeModifier var1);

    public void removeModifier(AttributeModifier var1);

    public void removeModifier(UUID var1);

    @Environment(value=EnvType.CLIENT)
    public void removeModifiers();

    public double getValue();

    @Environment(value=EnvType.CLIENT)
    default public void copyFrom(AttributeInstance attributeInstance) {
        this.setBaseValue(attributeInstance.getBaseValue());
        Set<AttributeModifier> set = attributeInstance.getModifiers();
        Set<AttributeModifier> set2 = this.getModifiers();
        ImmutableSet<AttributeModifier> immutableSet = ImmutableSet.copyOf(Sets.difference(set, set2));
        ImmutableSet<AttributeModifier> immutableSet2 = ImmutableSet.copyOf(Sets.difference(set2, set));
        immutableSet.forEach(this::addModifier);
        immutableSet2.forEach(this::removeModifier);
    }
}

