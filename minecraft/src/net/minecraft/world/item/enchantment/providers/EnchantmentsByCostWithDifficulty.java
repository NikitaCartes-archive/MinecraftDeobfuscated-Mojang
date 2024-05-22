package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentsByCostWithDifficulty(HolderSet<Enchantment> enchantments, int minCost, int maxCostSpan) implements EnchantmentProvider {
	public static final int MAX_ALLOWED_VALUE_PART = 10000;
	public static final MapCodec<EnchantmentsByCostWithDifficulty> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).fieldOf("enchantments").forGetter(EnchantmentsByCostWithDifficulty::enchantments),
					ExtraCodecs.intRange(1, 10000).fieldOf("min_cost").forGetter(EnchantmentsByCostWithDifficulty::minCost),
					ExtraCodecs.intRange(0, 10000).fieldOf("max_cost_span").forGetter(EnchantmentsByCostWithDifficulty::maxCostSpan)
				)
				.apply(instance, EnchantmentsByCostWithDifficulty::new)
	);

	@Override
	public void enchant(ItemStack itemStack, ItemEnchantments.Mutable mutable, RandomSource randomSource, DifficultyInstance difficultyInstance) {
		float f = difficultyInstance.getSpecialMultiplier();
		int i = Mth.randomBetweenInclusive(randomSource, this.minCost, this.minCost + (int)(f * (float)this.maxCostSpan));

		for (EnchantmentInstance enchantmentInstance : EnchantmentHelper.selectEnchantment(randomSource, itemStack, i, this.enchantments.stream())) {
			mutable.upgrade(enchantmentInstance.enchantment, enchantmentInstance.level);
		}
	}

	@Override
	public MapCodec<EnchantmentsByCostWithDifficulty> codec() {
		return CODEC;
	}
}
