package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AttributeMap {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<Attribute, AttributeInstance> attributes = Maps.<Attribute, AttributeInstance>newHashMap();
	private final Set<AttributeInstance> dirtyAttributes = Sets.<AttributeInstance>newHashSet();
	private final AttributeSupplier supplier;

	public AttributeMap(AttributeSupplier attributeSupplier) {
		this.supplier = attributeSupplier;
	}

	private void onAttributeModified(AttributeInstance attributeInstance) {
		if (attributeInstance.getAttribute().isClientSyncable()) {
			this.dirtyAttributes.add(attributeInstance);
		}
	}

	public Set<AttributeInstance> getDirtyAttributes() {
		return this.dirtyAttributes;
	}

	public Collection<AttributeInstance> getSyncableAttributes() {
		return (Collection<AttributeInstance>)this.attributes
			.values()
			.stream()
			.filter(attributeInstance -> attributeInstance.getAttribute().isClientSyncable())
			.collect(Collectors.toList());
	}

	@Nullable
	public AttributeInstance getInstance(Attribute attribute) {
		return (AttributeInstance)this.attributes.computeIfAbsent(attribute, attributex -> this.supplier.createInstance(this::onAttributeModified, attributex));
	}

	@Nullable
	public AttributeInstance getInstance(Holder<Attribute> holder) {
		return this.getInstance(holder.value());
	}

	public boolean hasAttribute(Attribute attribute) {
		return this.attributes.get(attribute) != null || this.supplier.hasAttribute(attribute);
	}

	public boolean hasAttribute(Holder<Attribute> holder) {
		return this.hasAttribute(holder.value());
	}

	public boolean hasModifier(Attribute attribute, UUID uUID) {
		AttributeInstance attributeInstance = (AttributeInstance)this.attributes.get(attribute);
		return attributeInstance != null ? attributeInstance.getModifier(uUID) != null : this.supplier.hasModifier(attribute, uUID);
	}

	public boolean hasModifier(Holder<Attribute> holder, UUID uUID) {
		return this.hasModifier(holder.value(), uUID);
	}

	public double getValue(Attribute attribute) {
		AttributeInstance attributeInstance = (AttributeInstance)this.attributes.get(attribute);
		return attributeInstance != null ? attributeInstance.getValue() : this.supplier.getValue(attribute);
	}

	public double getBaseValue(Attribute attribute) {
		AttributeInstance attributeInstance = (AttributeInstance)this.attributes.get(attribute);
		return attributeInstance != null ? attributeInstance.getBaseValue() : this.supplier.getBaseValue(attribute);
	}

	public double getModifierValue(Attribute attribute, UUID uUID) {
		AttributeInstance attributeInstance = (AttributeInstance)this.attributes.get(attribute);
		return attributeInstance != null ? attributeInstance.getModifier(uUID).getAmount() : this.supplier.getModifierValue(attribute, uUID);
	}

	public double getModifierValue(Holder<Attribute> holder, UUID uUID) {
		return this.getModifierValue(holder.value(), uUID);
	}

	public void removeAttributeModifiers(Multimap<Attribute, AttributeModifier> multimap) {
		multimap.asMap().forEach((attribute, collection) -> {
			AttributeInstance attributeInstance = (AttributeInstance)this.attributes.get(attribute);
			if (attributeInstance != null) {
				collection.forEach(attributeInstance::removeModifier);
			}
		});
	}

	public void addTransientAttributeModifiers(Multimap<Attribute, AttributeModifier> multimap) {
		multimap.forEach((attribute, attributeModifier) -> {
			AttributeInstance attributeInstance = this.getInstance(attribute);
			if (attributeInstance != null) {
				attributeInstance.removeModifier(attributeModifier);
				attributeInstance.addTransientModifier(attributeModifier);
			}
		});
	}

	public void assignValues(AttributeMap attributeMap) {
		attributeMap.attributes.values().forEach(attributeInstance -> {
			AttributeInstance attributeInstance2 = this.getInstance(attributeInstance.getAttribute());
			if (attributeInstance2 != null) {
				attributeInstance2.replaceFrom(attributeInstance);
			}
		});
	}

	public ListTag save() {
		ListTag listTag = new ListTag();

		for (AttributeInstance attributeInstance : this.attributes.values()) {
			listTag.add(attributeInstance.save());
		}

		return listTag;
	}

	public void load(ListTag listTag) {
		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			String string = compoundTag.getString("Name");
			Util.ifElse(Registry.ATTRIBUTE.getOptional(ResourceLocation.tryParse(string)), attribute -> {
				AttributeInstance attributeInstance = this.getInstance(attribute);
				if (attributeInstance != null) {
					attributeInstance.load(compoundTag);
				}
			}, () -> LOGGER.warn("Ignoring unknown attribute '{}'", string));
		}
	}
}
