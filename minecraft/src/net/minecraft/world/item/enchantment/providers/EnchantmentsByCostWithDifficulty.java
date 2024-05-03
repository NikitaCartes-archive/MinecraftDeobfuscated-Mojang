package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public record EnchantmentsByCostWithDifficulty(HolderSet<Enchantment> enchantments, int minCost, int maxCostSpan) implements EnchantmentProvider {
	public static final MapCodec<EnchantmentsByCostWithDifficulty> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).fieldOf("enchantments").forGetter(EnchantmentsByCostWithDifficulty::enchantments),
					ExtraCodecs.POSITIVE_INT.fieldOf("min_cost").forGetter(EnchantmentsByCostWithDifficulty::minCost),
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_cost_span").forGetter(EnchantmentsByCostWithDifficulty::maxCostSpan)
				)
				.apply(instance, EnchantmentsByCostWithDifficulty::new)
	);

	@Override
	public void enchant(ItemStack itemStack, ItemEnchantments.Mutable mutable, RandomSource randomSource, Level level, BlockPos blockPos) {
		float f = level.getCurrentDifficultyAt(blockPos).getSpecialMultiplier();
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
