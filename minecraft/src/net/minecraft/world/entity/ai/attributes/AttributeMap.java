package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AttributeMap {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<Holder<Attribute>, AttributeInstance> attributes = new Object2ObjectOpenHashMap<>();
	private final Set<AttributeInstance> attributesToSync = new ObjectOpenHashSet<>();
	private final Set<AttributeInstance> attributesToUpdate = new ObjectOpenHashSet<>();
	private final AttributeSupplier supplier;

	public AttributeMap(AttributeSupplier attributeSupplier) {
		this.supplier = attributeSupplier;
	}

	private void onAttributeModified(AttributeInstance attributeInstance) {
		this.attributesToUpdate.add(attributeInstance);
		if (attributeInstance.getAttribute().value().isClientSyncable()) {
			this.attributesToSync.add(attributeInstance);
		}
	}

	public Set<AttributeInstance> getAttributesToSync() {
		return this.attributesToSync;
	}

	public Set<AttributeInstance> getAttributesToUpdate() {
		return this.attributesToUpdate;
	}

	public Collection<AttributeInstance> getSyncableAttributes() {
		return (Collection<AttributeInstance>)this.attributes
			.values()
			.stream()
			.filter(attributeInstance -> attributeInstance.getAttribute().value().isClientSyncable())
			.collect(Collectors.toList());
	}

	@Nullable
	public AttributeInstance getInstance(Holder<Attribute> holder) {
		return (AttributeInstance)this.attributes.computeIfAbsent(holder, holderx -> this.supplier.createInstance(this::onAttributeModified, holderx));
	}

	public boolean hasAttribute(Holder<Attribute> holder) {
		return this.attributes.get(holder) != null || this.supplier.hasAttribute(holder);
	}

	public boolean hasModifier(Holder<Attribute> holder, ResourceLocation resourceLocation) {
		AttributeInstance attributeInstance = (AttributeInstance)this.attributes.get(holder);
		return attributeInstance != null ? attributeInstance.getModifier(resourceLocation) != null : this.supplier.hasModifier(holder, resourceLocation);
	}

	public double getValue(Holder<Attribute> holder) {
		AttributeInstance attributeInstance = (AttributeInstance)this.attributes.get(holder);
		return attributeInstance != null ? attributeInstance.getValue() : this.supplier.getValue(holder);
	}

	public double getBaseValue(Holder<Attribute> holder) {
		AttributeInstance attributeInstance = (AttributeInstance)this.attributes.get(holder);
		return attributeInstance != null ? attributeInstance.getBaseValue() : this.supplier.getBaseValue(holder);
	}

	public double getModifierValue(Holder<Attribute> holder, ResourceLocation resourceLocation) {
		AttributeInstance attributeInstance = (AttributeInstance)this.attributes.get(holder);
		return attributeInstance != null ? attributeInstance.getModifier(resourceLocation).amount() : this.supplier.getModifierValue(holder, resourceLocation);
	}

	public void addTransientAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> multimap) {
		multimap.forEach((holder, attributeModifier) -> {
			AttributeInstance attributeInstance = this.getInstance(holder);
			if (attributeInstance != null) {
				attributeInstance.removeModifier(attributeModifier.id());
				attributeInstance.addTransientModifier(attributeModifier);
			}
		});
	}

	public void removeAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> multimap) {
		multimap.asMap().forEach((holder, collection) -> {
			AttributeInstance attributeInstance = (AttributeInstance)this.attributes.get(holder);
			if (attributeInstance != null) {
				collection.forEach(attributeModifier -> attributeInstance.removeModifier(attributeModifier.id()));
			}
		});
	}

	public void assignAllValues(AttributeMap attributeMap) {
		attributeMap.attributes.values().forEach(attributeInstance -> {
			AttributeInstance attributeInstance2 = this.getInstance(attributeInstance.getAttribute());
			if (attributeInstance2 != null) {
				attributeInstance2.replaceFrom(attributeInstance);
			}
		});
	}

	public void assignBaseValues(AttributeMap attributeMap) {
		attributeMap.attributes.values().forEach(attributeInstance -> {
			AttributeInstance attributeInstance2 = this.getInstance(attributeInstance.getAttribute());
			if (attributeInstance2 != null) {
				attributeInstance2.setBaseValue(attributeInstance.getBaseValue());
			}
		});
	}

	public void assignPermanentModifiers(AttributeMap attributeMap) {
		attributeMap.attributes.values().forEach(attributeInstance -> {
			AttributeInstance attributeInstance2 = this.getInstance(attributeInstance.getAttribute());
			if (attributeInstance2 != null) {
				attributeInstance2.addPermanentModifiers(attributeInstance.getPermanentModifiers());
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
			String string = compoundTag.getString("id");
			ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
			if (resourceLocation != null) {
				Util.ifElse(BuiltInRegistries.ATTRIBUTE.getHolder(resourceLocation), reference -> {
					AttributeInstance attributeInstance = this.getInstance(reference);
					if (attributeInstance != null) {
						attributeInstance.load(compoundTag);
					}
				}, () -> LOGGER.warn("Ignoring unknown attribute '{}'", resourceLocation));
			} else {
				LOGGER.warn("Ignoring malformed attribute '{}'", string);
			}
		}
	}
}
