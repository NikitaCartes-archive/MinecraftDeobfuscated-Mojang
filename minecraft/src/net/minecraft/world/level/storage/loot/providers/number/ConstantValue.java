package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.world.level.storage.loot.LootContext;

public record ConstantValue(float value) implements NumberProvider {
	public static final MapCodec<ConstantValue> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Codec.FLOAT.fieldOf("value").forGetter(ConstantValue::value)).apply(instance, ConstantValue::new)
	);
	public static final Codec<ConstantValue> INLINE_CODEC = Codec.FLOAT.xmap(ConstantValue::new, ConstantValue::value);

	@Override
	public LootNumberProviderType getType() {
		return NumberProviders.CONSTANT;
	}

	@Override
	public float getFloat(LootContext lootContext) {
		return this.value;
	}

	public static ConstantValue exactly(float f) {
		return new ConstantValue(f);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			return Float.compare(((ConstantValue)object).value, this.value) == 0;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.value != 0.0F ? Float.floatToIntBits(this.value) : 0;
	}
}
