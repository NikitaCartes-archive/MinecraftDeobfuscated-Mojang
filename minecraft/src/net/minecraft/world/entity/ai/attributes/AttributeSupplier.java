package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;

public class AttributeSupplier {
	private final Map<Holder<Attribute>, AttributeInstance> instances;

	AttributeSupplier(Map<Holder<Attribute>, AttributeInstance> map) {
		this.instances = map;
	}

	private AttributeInstance getAttributeInstance(Holder<Attribute> holder) {
		AttributeInstance attributeInstance = (AttributeInstance)this.instances.get(holder);
		if (attributeInstance == null) {
			throw new IllegalArgumentException("Can't find attribute " + holder.getRegisteredName());
		} else {
			return attributeInstance;
		}
	}

	public double getValue(Holder<Attribute> holder) {
		return this.getAttributeInstance(holder).getValue();
	}

	public double getBaseValue(Holder<Attribute> holder) {
		return this.getAttributeInstance(holder).getBaseValue();
	}

	public double getModifierValue(Holder<Attribute> holder, UUID uUID) {
		AttributeModifier attributeModifier = this.getAttributeInstance(holder).getModifier(uUID);
		if (attributeModifier == null) {
			throw new IllegalArgumentException("Can't find modifier " + uUID + " on attribute " + holder.getRegisteredName());
		} else {
			return attributeModifier.getAmount();
		}
	}

	@Nullable
	public AttributeInstance createInstance(Consumer<AttributeInstance> consumer, Holder<Attribute> holder) {
		AttributeInstance attributeInstance = (AttributeInstance)this.instances.get(holder);
		if (attributeInstance == null) {
			return null;
		} else {
			AttributeInstance attributeInstance2 = new AttributeInstance(holder, consumer);
			attributeInstance2.replaceFrom(attributeInstance);
			return attributeInstance2;
		}
	}

	public static AttributeSupplier.Builder builder() {
		return new AttributeSupplier.Builder();
	}

	public boolean hasAttribute(Holder<Attribute> holder) {
		return this.instances.containsKey(holder);
	}

	public boolean hasModifier(Holder<Attribute> holder, UUID uUID) {
		AttributeInstance attributeInstance = (AttributeInstance)this.instances.get(holder);
		return attributeInstance != null && attributeInstance.getModifier(uUID) != null;
	}

	public static class Builder {
		private final ImmutableMap.Builder<Holder<Attribute>, AttributeInstance> builder = ImmutableMap.builder();
		private boolean instanceFrozen;

		private AttributeInstance create(Holder<Attribute> holder) {
			AttributeInstance attributeInstance = new AttributeInstance(holder, attributeInstancex -> {
				if (this.instanceFrozen) {
					throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + holder.getRegisteredName());
				}
			});
			this.builder.put(holder, attributeInstance);
			return attributeInstance;
		}

		public AttributeSupplier.Builder add(Holder<Attribute> holder) {
			this.create(holder);
			return this;
		}

		public AttributeSupplier.Builder add(Holder<Attribute> holder, double d) {
			AttributeInstance attributeInstance = this.create(holder);
			attributeInstance.setBaseValue(d);
			return this;
		}

		public AttributeSupplier build() {
			this.instanceFrozen = true;
			return new AttributeSupplier(this.builder.buildKeepingLast());
		}
	}
}
