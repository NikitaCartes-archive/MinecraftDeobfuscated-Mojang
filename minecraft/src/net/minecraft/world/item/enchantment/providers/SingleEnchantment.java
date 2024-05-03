package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public record SingleEnchantment(Holder<Enchantment> enchantment, IntProvider level) implements EnchantmentProvider {
	public static final MapCodec<SingleEnchantment> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Enchantment.CODEC.fieldOf("enchantment").forGetter(SingleEnchantment::enchantment), IntProvider.CODEC.fieldOf("level").forGetter(SingleEnchantment::level)
				)
				.apply(instance, SingleEnchantment::new)
	);

	@Override
	public void enchant(ItemStack itemStack, ItemEnchantments.Mutable mutable, RandomSource randomSource, Level level, BlockPos blockPos) {
		mutable.upgrade(this.enchantment, Mth.clamp(this.level.sample(randomSource), this.enchantment.value().getMinLevel(), this.enchantment.value().getMaxLevel()));
	}

	@Override
	public MapCodec<SingleEnchantment> codec() {
		return CODEC;
	}
}
