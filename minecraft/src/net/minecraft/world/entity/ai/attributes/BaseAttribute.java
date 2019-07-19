package net.minecraft.world.entity.ai.attributes;

import javax.annotation.Nullable;

public abstract class BaseAttribute implements Attribute {
	private final Attribute parent;
	private final String name;
	private final double defaultValue;
	private boolean syncable;

	protected BaseAttribute(@Nullable Attribute attribute, String string, double d) {
		this.parent = attribute;
		this.name = string;
		this.defaultValue = d;
		if (string == null) {
			throw new IllegalArgumentException("Name cannot be null!");
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public double getDefaultValue() {
		return this.defaultValue;
	}

	@Override
	public boolean isClientSyncable() {
		return this.syncable;
	}

	public BaseAttribute setSyncable(boolean bl) {
		this.syncable = bl;
		return this;
	}

	@Nullable
	@Override
	public Attribute getParentAttribute() {
		return this.parent;
	}

	public int hashCode() {
		return this.name.hashCode();
	}

	public boolean equals(Object object) {
		return object instanceof Attribute && this.name.equals(((Attribute)object).getName());
	}
}
