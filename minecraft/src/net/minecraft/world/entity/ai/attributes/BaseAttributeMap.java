package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.InsensitiveStringMap;

public abstract class BaseAttributeMap {
	protected final Map<Attribute, AttributeInstance> attributesByObject = Maps.<Attribute, AttributeInstance>newHashMap();
	protected final Map<String, AttributeInstance> attributesByName = new InsensitiveStringMap();
	protected final Multimap<Attribute, Attribute> descendantsByParent = HashMultimap.create();

	@Nullable
	public AttributeInstance getInstance(Attribute attribute) {
		return (AttributeInstance)this.attributesByObject.get(attribute);
	}

	@Nullable
	public AttributeInstance getInstance(String string) {
		return (AttributeInstance)this.attributesByName.get(string);
	}

	public AttributeInstance registerAttribute(Attribute attribute) {
		if (this.attributesByName.containsKey(attribute.getName())) {
			throw new IllegalArgumentException("Attribute is already registered!");
		} else {
			AttributeInstance attributeInstance = this.createAttributeInstance(attribute);
			this.attributesByName.put(attribute.getName(), attributeInstance);
			this.attributesByObject.put(attribute, attributeInstance);

			for (Attribute attribute2 = attribute.getParentAttribute(); attribute2 != null; attribute2 = attribute2.getParentAttribute()) {
				this.descendantsByParent.put(attribute2, attribute);
			}

			return attributeInstance;
		}
	}

	protected abstract AttributeInstance createAttributeInstance(Attribute attribute);

	public Collection<AttributeInstance> getAttributes() {
		return this.attributesByName.values();
	}

	public void onAttributeModified(AttributeInstance attributeInstance) {
	}

	public void removeAttributeModifiers(Multimap<String, AttributeModifier> multimap) {
		for (Entry<String, AttributeModifier> entry : multimap.entries()) {
			AttributeInstance attributeInstance = this.getInstance((String)entry.getKey());
			if (attributeInstance != null) {
				attributeInstance.removeModifier((AttributeModifier)entry.getValue());
			}
		}
	}

	public void addAttributeModifiers(Multimap<String, AttributeModifier> multimap) {
		for (Entry<String, AttributeModifier> entry : multimap.entries()) {
			AttributeInstance attributeInstance = this.getInstance((String)entry.getKey());
			if (attributeInstance != null) {
				attributeInstance.removeModifier((AttributeModifier)entry.getValue());
				attributeInstance.addModifier((AttributeModifier)entry.getValue());
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public void assignValues(BaseAttributeMap baseAttributeMap) {
		this.getAttributes().forEach(attributeInstance -> {
			AttributeInstance attributeInstance2 = baseAttributeMap.getInstance(attributeInstance.getAttribute());
			if (attributeInstance2 != null) {
				attributeInstance.copyFrom(attributeInstance2);
			}
		});
	}
}
