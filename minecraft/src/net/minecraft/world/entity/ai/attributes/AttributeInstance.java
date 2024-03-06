package net.minecraft.world.entity.ai.attributes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;

public class AttributeInstance {
	private final Holder<Attribute> attribute;
	private final Map<AttributeModifier.Operation, Map<UUID, AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
	private final Map<UUID, AttributeModifier> modifierById = new Object2ObjectArrayMap<>();
	private final Map<UUID, AttributeModifier> permanentModifiers = new Object2ObjectArrayMap<>();
	private double baseValue;
	private boolean dirty = true;
	private double cachedValue;
	private final Consumer<AttributeInstance> onDirty;

	public AttributeInstance(Holder<Attribute> holder, Consumer<AttributeInstance> consumer) {
		this.attribute = holder;
		this.onDirty = consumer;
		this.baseValue = holder.value().getDefaultValue();
	}

	public Holder<Attribute> getAttribute() {
		return this.attribute;
	}

	public double getBaseValue() {
		return this.baseValue;
	}

	public void setBaseValue(double d) {
		if (d != this.baseValue) {
			this.baseValue = d;
			this.setDirty();
		}
	}

	@VisibleForTesting
	Map<UUID, AttributeModifier> getModifiers(AttributeModifier.Operation operation) {
		return (Map<UUID, AttributeModifier>)this.modifiersByOperation.computeIfAbsent(operation, operationx -> new Object2ObjectOpenHashMap());
	}

	public Set<AttributeModifier> getModifiers() {
		return ImmutableSet.copyOf(this.modifierById.values());
	}

	@Nullable
	public AttributeModifier getModifier(UUID uUID) {
		return (AttributeModifier)this.modifierById.get(uUID);
	}

	public boolean hasModifier(AttributeModifier attributeModifier) {
		return this.modifierById.get(attributeModifier.id()) != null;
	}

	private void addModifier(AttributeModifier attributeModifier) {
		AttributeModifier attributeModifier2 = (AttributeModifier)this.modifierById.putIfAbsent(attributeModifier.id(), attributeModifier);
		if (attributeModifier2 != null) {
			throw new IllegalArgumentException("Modifier is already applied on this attribute!");
		} else {
			this.getModifiers(attributeModifier.operation()).put(attributeModifier.id(), attributeModifier);
			this.setDirty();
		}
	}

	public void addOrUpdateTransientModifier(AttributeModifier attributeModifier) {
		AttributeModifier attributeModifier2 = (AttributeModifier)this.modifierById.put(attributeModifier.id(), attributeModifier);
		if (attributeModifier != attributeModifier2) {
			this.getModifiers(attributeModifier.operation()).put(attributeModifier.id(), attributeModifier);
			this.setDirty();
		}
	}

	public void addTransientModifier(AttributeModifier attributeModifier) {
		this.addModifier(attributeModifier);
	}

	public void addPermanentModifier(AttributeModifier attributeModifier) {
		this.addModifier(attributeModifier);
		this.permanentModifiers.put(attributeModifier.id(), attributeModifier);
	}

	protected void setDirty() {
		this.dirty = true;
		this.onDirty.accept(this);
	}

	public void removeModifier(AttributeModifier attributeModifier) {
		this.removeModifier(attributeModifier.id());
	}

	public void removeModifier(UUID uUID) {
		AttributeModifier attributeModifier = (AttributeModifier)this.modifierById.remove(uUID);
		if (attributeModifier != null) {
			this.getModifiers(attributeModifier.operation()).remove(uUID);
			this.permanentModifiers.remove(uUID);
			this.setDirty();
		}
	}

	public boolean removePermanentModifier(UUID uUID) {
		AttributeModifier attributeModifier = (AttributeModifier)this.permanentModifiers.remove(uUID);
		if (attributeModifier == null) {
			return false;
		} else {
			this.getModifiers(attributeModifier.operation()).remove(attributeModifier.id());
			this.modifierById.remove(uUID);
			this.setDirty();
			return true;
		}
	}

	public void removeModifiers() {
		for (AttributeModifier attributeModifier : this.getModifiers()) {
			this.removeModifier(attributeModifier);
		}
	}

	public double getValue() {
		if (this.dirty) {
			this.cachedValue = this.calculateValue();
			this.dirty = false;
		}

		return this.cachedValue;
	}

	private double calculateValue() {
		double d = this.getBaseValue();

		for (AttributeModifier attributeModifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
			d += attributeModifier.amount();
		}

		double e = d;

		for (AttributeModifier attributeModifier2 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
			e += d * attributeModifier2.amount();
		}

		for (AttributeModifier attributeModifier2 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
			e *= 1.0 + attributeModifier2.amount();
		}

		return this.attribute.value().sanitizeValue(e);
	}

	private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation operation) {
		return ((Map)this.modifiersByOperation.getOrDefault(operation, Map.of())).values();
	}

	public void replaceFrom(AttributeInstance attributeInstance) {
		this.baseValue = attributeInstance.baseValue;
		this.modifierById.clear();
		this.modifierById.putAll(attributeInstance.modifierById);
		this.permanentModifiers.clear();
		this.permanentModifiers.putAll(attributeInstance.permanentModifiers);
		this.modifiersByOperation.clear();
		attributeInstance.modifiersByOperation.forEach((operation, map) -> this.getModifiers(operation).putAll(map));
		this.setDirty();
	}

	public CompoundTag save() {
		CompoundTag compoundTag = new CompoundTag();
		ResourceKey<Attribute> resourceKey = (ResourceKey<Attribute>)this.attribute
			.unwrapKey()
			.orElseThrow(() -> new IllegalStateException("Tried to serialize unregistered attribute"));
		compoundTag.putString("Name", resourceKey.location().toString());
		compoundTag.putDouble("Base", this.baseValue);
		if (!this.permanentModifiers.isEmpty()) {
			ListTag listTag = new ListTag();

			for (AttributeModifier attributeModifier : this.permanentModifiers.values()) {
				listTag.add(attributeModifier.save());
			}

			compoundTag.put("Modifiers", listTag);
		}

		return compoundTag;
	}

	public void load(CompoundTag compoundTag) {
		this.baseValue = compoundTag.getDouble("Base");
		if (compoundTag.contains("Modifiers", 9)) {
			ListTag listTag = compoundTag.getList("Modifiers", 10);

			for (int i = 0; i < listTag.size(); i++) {
				AttributeModifier attributeModifier = AttributeModifier.load(listTag.getCompound(i));
				if (attributeModifier != null) {
					this.modifierById.put(attributeModifier.id(), attributeModifier);
					this.getModifiers(attributeModifier.operation()).put(attributeModifier.id(), attributeModifier);
					this.permanentModifiers.put(attributeModifier.id(), attributeModifier);
				}
			}
		}

		this.setDirty();
	}
}
