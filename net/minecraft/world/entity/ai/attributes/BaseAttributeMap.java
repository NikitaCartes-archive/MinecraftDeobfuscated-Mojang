/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.InsensitiveStringMap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

public abstract class BaseAttributeMap {
    protected final Map<Attribute, AttributeInstance> attributesByObject = Maps.newHashMap();
    protected final Map<String, AttributeInstance> attributesByName = new InsensitiveStringMap<AttributeInstance>();
    protected final Multimap<Attribute, Attribute> descendantsByParent = HashMultimap.create();

    @Nullable
    public AttributeInstance getInstance(Attribute attribute) {
        return this.attributesByObject.get(attribute);
    }

    @Nullable
    public AttributeInstance getInstance(String string) {
        return this.attributesByName.get(string);
    }

    public AttributeInstance registerAttribute(Attribute attribute) {
        if (this.attributesByName.containsKey(attribute.getName())) {
            throw new IllegalArgumentException("Attribute is already registered!");
        }
        AttributeInstance attributeInstance = this.createAttributeInstance(attribute);
        this.attributesByName.put(attribute.getName(), attributeInstance);
        this.attributesByObject.put(attribute, attributeInstance);
        for (Attribute attribute2 = attribute.getParentAttribute(); attribute2 != null; attribute2 = attribute2.getParentAttribute()) {
            this.descendantsByParent.put(attribute2, attribute);
        }
        return attributeInstance;
    }

    protected abstract AttributeInstance createAttributeInstance(Attribute var1);

    public Collection<AttributeInstance> getAttributes() {
        return this.attributesByName.values();
    }

    public void onAttributeModified(AttributeInstance attributeInstance) {
    }

    public void removeAttributeModifiers(Multimap<String, AttributeModifier> multimap) {
        for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
            AttributeInstance attributeInstance = this.getInstance(entry.getKey());
            if (attributeInstance == null) continue;
            attributeInstance.removeModifier(entry.getValue());
        }
    }

    public void addAttributeModifiers(Multimap<String, AttributeModifier> multimap) {
        for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
            AttributeInstance attributeInstance = this.getInstance(entry.getKey());
            if (attributeInstance == null) continue;
            attributeInstance.removeModifier(entry.getValue());
            attributeInstance.addModifier(entry.getValue());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public void assignValues(BaseAttributeMap baseAttributeMap) {
        this.getAttributes().forEach(attributeInstance -> {
            AttributeInstance attributeInstance2 = baseAttributeMap.getInstance(attributeInstance.getAttribute());
            if (attributeInstance2 != null) {
                attributeInstance.copyFrom(attributeInstance2);
            }
        });
    }
}

