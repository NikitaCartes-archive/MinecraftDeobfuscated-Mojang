package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;

public class AttributeSupplier {
	private final Map<Attribute, AttributeInstance> instances;

	public AttributeSupplier(Map<Attribute, AttributeInstance> map) {
		this.instances = ImmutableMap.copyOf(map);
	}

	private AttributeInstance getAttributeInstance(Attribute attribute) {
		AttributeInstance attributeInstance = (AttributeInstance)this.instances.get(attribute);
		if (attributeInstance == null) {
			throw new IllegalArgumentException("Can't find attribute " + Registry.ATTRIBUTES.getKey(attribute));
		} else {
			return attributeInstance;
		}
	}

	public double getValue(Attribute attribute) {
		return this.getAttributeInstance(attribute).getValue();
	}

	public double getBaseValue(Attribute attribute) {
		return this.getAttributeInstance(attribute).getBaseValue();
	}

	@Nullable
	public AttributeInstance createInstance(Consumer<AttributeInstance> consumer, Attribute attribute) {
		AttributeInstance attributeInstance = (AttributeInstance)this.instances.get(attribute);
		if (attributeInstance == null) {
			return null;
		} else {
			AttributeInstance attributeInstance2 = new AttributeInstance(attribute, consumer);
			attributeInstance2.replaceFrom(attributeInstance);
			return attributeInstance2;
		}
	}

	public static AttributeSupplier.Builder builder() {
		return new AttributeSupplier.Builder();
	}

	public static class Builder {
		private final Map<Attribute, AttributeInstance> builder = Maps.<Attribute, AttributeInstance>newHashMap();
		private boolean instanceFrozen;

		private AttributeInstance create(Attribute attribute) {
			AttributeInstance attributeInstance = new AttributeInstance(attribute, attributeInstancex -> {
				if (this.instanceFrozen) {
					throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + Registry.ATTRIBUTES.getKey(attribute));
				}
			});
			this.builder.put(attribute, attributeInstance);
			return attributeInstance;
		}

		public AttributeSupplier.Builder add(Attribute attribute) {
			this.create(attribute);
			return this;
		}

		public AttributeSupplier.Builder add(Attribute attribute, double d) {
			AttributeInstance attributeInstance = this.create(attribute);
			attributeInstance.setBaseValue(d);
			return this;
		}

		public AttributeSupplier build() {
			this.instanceFrozen = true;
			return new AttributeSupplier(this.builder);
		}
	}
}
