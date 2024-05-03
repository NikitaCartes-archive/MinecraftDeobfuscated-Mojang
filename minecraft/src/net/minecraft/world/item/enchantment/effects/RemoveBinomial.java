package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record RemoveBinomial(LevelBasedValue chance) implements EnchantmentValueEffect {
	public static final MapCodec<RemoveBinomial> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(LevelBasedValue.CODEC.fieldOf("chance").forGetter(RemoveBinomial::chance)).apply(instance, RemoveBinomial::new)
	);

	@Override
	public float process(ItemStack itemStack, int i, RandomSource randomSource, float f) {
		float g = this.chance.calculate(i);
		int j = 0;

		for (int k = 0; (float)k < f; k++) {
			if (randomSource.nextFloat() < g) {
				j++;
			}
		}

		return f - (float)j;
	}

	@Override
	public MapCodec<RemoveBinomial> codec() {
		return CODEC;
	}
}
