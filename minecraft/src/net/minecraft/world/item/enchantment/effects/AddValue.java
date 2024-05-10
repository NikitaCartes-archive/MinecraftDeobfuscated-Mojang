package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record AddValue(LevelBasedValue value) implements EnchantmentValueEffect {
	public static final MapCodec<AddValue> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(LevelBasedValue.CODEC.fieldOf("value").forGetter(AddValue::value)).apply(instance, AddValue::new)
	);

	@Override
	public float process(int i, RandomSource randomSource, float f) {
		return f + this.value.calculate(i);
	}

	@Override
	public MapCodec<AddValue> codec() {
		return CODEC;
	}
}
