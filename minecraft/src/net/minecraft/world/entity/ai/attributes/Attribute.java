package net.minecraft.world.entity.ai.attributes;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;

public class Attribute {
	public static final Codec<Holder<Attribute>> CODEC = BuiltInRegistries.ATTRIBUTE.holderByNameCodec();
	private final double defaultValue;
	private boolean syncable;
	private final String descriptionId;

	protected Attribute(String string, double d) {
		this.defaultValue = d;
		this.descriptionId = string;
	}

	public double getDefaultValue() {
		return this.defaultValue;
	}

	public boolean isClientSyncable() {
		return this.syncable;
	}

	public Attribute setSyncable(boolean bl) {
		this.syncable = bl;
		return this;
	}

	public double sanitizeValue(double d) {
		return d;
	}

	public String getDescriptionId() {
		return this.descriptionId;
	}
}
