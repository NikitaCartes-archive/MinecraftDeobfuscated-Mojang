/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.InsensitiveStringMap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.world.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ModifiableAttributeMap
extends BaseAttributeMap {
    private final Set<AttributeInstance> dirtyAttributes = Sets.newHashSet();
    protected final Map<String, AttributeInstance> attributesByLegacy = new InsensitiveStringMap<AttributeInstance>();

    @Override
    public ModifiableAttributeInstance getInstance(Attribute attribute) {
        return (ModifiableAttributeInstance)super.getInstance(attribute);
    }

    @Override
    public ModifiableAttributeInstance getInstance(String string) {
        AttributeInstance attributeInstance = super.getInstance(string);
        if (attributeInstance == null) {
            attributeInstance = this.attributesByLegacy.get(string);
        }
        return (ModifiableAttributeInstance)attributeInstance;
    }

    @Override
    public AttributeInstance registerAttribute(Attribute attribute) {
        AttributeInstance attributeInstance = super.registerAttribute(attribute);
        if (attribute instanceof RangedAttribute && ((RangedAttribute)attribute).getImportLegacyName() != null) {
            this.attributesByLegacy.put(((RangedAttribute)attribute).getImportLegacyName(), attributeInstance);
        }
        return attributeInstance;
    }

    @Override
    protected AttributeInstance createAttributeInstance(Attribute attribute) {
        return new ModifiableAttributeInstance(this, attribute);
    }

    @Override
    public void onAttributeModified(AttributeInstance attributeInstance) {
        if (attributeInstance.getAttribute().isClientSyncable()) {
            this.dirtyAttributes.add(attributeInstance);
        }
        for (Attribute attribute : this.descendantsByParent.get(attributeInstance.getAttribute())) {
            ModifiableAttributeInstance modifiableAttributeInstance = this.getInstance(attribute);
            if (modifiableAttributeInstance == null) continue;
            modifiableAttributeInstance.setDirty();
        }
    }

    public Set<AttributeInstance> getDirtyAttributes() {
        return this.dirtyAttributes;
    }

    public Collection<AttributeInstance> getSyncableAttributes() {
        HashSet<AttributeInstance> set = Sets.newHashSet();
        for (AttributeInstance attributeInstance : this.getAttributes()) {
            if (!attributeInstance.getAttribute().isClientSyncable()) continue;
            set.add(attributeInstance);
        }
        return set;
    }

    @Override
    public /* synthetic */ AttributeInstance getInstance(String string) {
        return this.getInstance(string);
    }

    @Override
    public /* synthetic */ AttributeInstance getInstance(Attribute attribute) {
        return this.getInstance(attribute);
    }
}

