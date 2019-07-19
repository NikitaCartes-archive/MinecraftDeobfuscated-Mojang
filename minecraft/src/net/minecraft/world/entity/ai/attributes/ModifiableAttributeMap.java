package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.InsensitiveStringMap;

public class ModifiableAttributeMap extends BaseAttributeMap {
	private final Set<AttributeInstance> dirtyAttributes = Sets.<AttributeInstance>newHashSet();
	protected final Map<String, AttributeInstance> attributesByLegacy = new InsensitiveStringMap();

	public ModifiableAttributeInstance getInstance(Attribute attribute) {
		return (ModifiableAttributeInstance)super.getInstance(attribute);
	}

	public ModifiableAttributeInstance getInstance(String string) {
		AttributeInstance attributeInstance = super.getInstance(string);
		if (attributeInstance == null) {
			attributeInstance = (AttributeInstance)this.attributesByLegacy.get(string);
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
			if (modifiableAttributeInstance != null) {
				modifiableAttributeInstance.setDirty();
			}
		}
	}

	public Set<AttributeInstance> getDirtyAttributes() {
		return this.dirtyAttributes;
	}

	public Collection<AttributeInstance> getSyncableAttributes() {
		Set<AttributeInstance> set = Sets.<AttributeInstance>newHashSet();

		for (AttributeInstance attributeInstance : this.getAttributes()) {
			if (attributeInstance.getAttribute().isClientSyncable()) {
				set.add(attributeInstance);
			}
		}

		return set;
	}
}
