package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ModifiableAttributeInstance implements AttributeInstance {
	private final BaseAttributeMap attributeMap;
	private final Attribute attribute;
	private final Map<AttributeModifier.Operation, Set<AttributeModifier>> modifiers = Maps.newEnumMap(AttributeModifier.Operation.class);
	private final Map<String, Set<AttributeModifier>> modifiersByName = Maps.<String, Set<AttributeModifier>>newHashMap();
	private final Map<UUID, AttributeModifier> modifierById = Maps.<UUID, AttributeModifier>newHashMap();
	private double baseValue;
	private boolean dirty = true;
	private double cachedValue;

	public ModifiableAttributeInstance(BaseAttributeMap baseAttributeMap, Attribute attribute) {
		this.attributeMap = baseAttributeMap;
		this.attribute = attribute;
		this.baseValue = attribute.getDefaultValue();

		for (AttributeModifier.Operation operation : AttributeModifier.Operation.values()) {
			this.modifiers.put(operation, Sets.newHashSet());
		}
	}

	@Override
	public Attribute getAttribute() {
		return this.attribute;
	}

	@Override
	public double getBaseValue() {
		return this.baseValue;
	}

	@Override
	public void setBaseValue(double d) {
		if (d != this.getBaseValue()) {
			this.baseValue = d;
			this.setDirty();
		}
	}

	@Override
	public Set<AttributeModifier> getModifiers(AttributeModifier.Operation operation) {
		return (Set<AttributeModifier>)this.modifiers.get(operation);
	}

	@Override
	public Set<AttributeModifier> getModifiers() {
		Set<AttributeModifier> set = Sets.<AttributeModifier>newHashSet();

		for (AttributeModifier.Operation operation : AttributeModifier.Operation.values()) {
			set.addAll(this.getModifiers(operation));
		}

		return set;
	}

	@Nullable
	@Override
	public AttributeModifier getModifier(UUID uUID) {
		return (AttributeModifier)this.modifierById.get(uUID);
	}

	@Override
	public boolean hasModifier(AttributeModifier attributeModifier) {
		return this.modifierById.get(attributeModifier.getId()) != null;
	}

	@Override
	public void addModifier(AttributeModifier attributeModifier) {
		if (this.getModifier(attributeModifier.getId()) != null) {
			throw new IllegalArgumentException("Modifier is already applied on this attribute!");
		} else {
			Set<AttributeModifier> set = (Set<AttributeModifier>)this.modifiersByName.computeIfAbsent(attributeModifier.getName(), string -> Sets.newHashSet());
			((Set)this.modifiers.get(attributeModifier.getOperation())).add(attributeModifier);
			set.add(attributeModifier);
			this.modifierById.put(attributeModifier.getId(), attributeModifier);
			this.setDirty();
		}
	}

	protected void setDirty() {
		this.dirty = true;
		this.attributeMap.onAttributeModified(this);
	}

	@Override
	public void removeModifier(AttributeModifier attributeModifier) {
		for (AttributeModifier.Operation operation : AttributeModifier.Operation.values()) {
			((Set)this.modifiers.get(operation)).remove(attributeModifier);
		}

		Set<AttributeModifier> set = (Set<AttributeModifier>)this.modifiersByName.get(attributeModifier.getName());
		if (set != null) {
			set.remove(attributeModifier);
			if (set.isEmpty()) {
				this.modifiersByName.remove(attributeModifier.getName());
			}
		}

		this.modifierById.remove(attributeModifier.getId());
		this.setDirty();
	}

	@Override
	public void removeModifier(UUID uUID) {
		AttributeModifier attributeModifier = this.getModifier(uUID);
		if (attributeModifier != null) {
			this.removeModifier(attributeModifier);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void removeModifiers() {
		Collection<AttributeModifier> collection = this.getModifiers();
		if (collection != null) {
			for (AttributeModifier attributeModifier : Lists.newArrayList(collection)) {
				this.removeModifier(attributeModifier);
			}
		}
	}

	@Override
	public double getValue() {
		if (this.dirty) {
			this.cachedValue = this.calculateValue();
			this.dirty = false;
		}

		return this.cachedValue;
	}

	private double calculateValue() {
		double d = this.getBaseValue();

		for (AttributeModifier attributeModifier : this.getAppliedModifiers(AttributeModifier.Operation.ADDITION)) {
			d += attributeModifier.getAmount();
		}

		double e = d;

		for (AttributeModifier attributeModifier2 : this.getAppliedModifiers(AttributeModifier.Operation.MULTIPLY_BASE)) {
			e += d * attributeModifier2.getAmount();
		}

		for (AttributeModifier attributeModifier2 : this.getAppliedModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
			e *= 1.0 + attributeModifier2.getAmount();
		}

		return this.attribute.sanitizeValue(e);
	}

	private Collection<AttributeModifier> getAppliedModifiers(AttributeModifier.Operation operation) {
		Set<AttributeModifier> set = Sets.<AttributeModifier>newHashSet(this.getModifiers(operation));

		for (Attribute attribute = this.attribute.getParentAttribute(); attribute != null; attribute = attribute.getParentAttribute()) {
			AttributeInstance attributeInstance = this.attributeMap.getInstance(attribute);
			if (attributeInstance != null) {
				set.addAll(attributeInstance.getModifiers(operation));
			}
		}

		return set;
	}
}
