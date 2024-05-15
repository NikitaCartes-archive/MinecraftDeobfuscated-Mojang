package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public interface EnchantmentProvider {
	Codec<EnchantmentProvider> DIRECT_CODEC = BuiltInRegistries.ENCHANTMENT_PROVIDER_TYPE.byNameCodec().dispatch(EnchantmentProvider::codec, Function.identity());

	void enchant(ItemStack itemStack, ItemEnchantments.Mutable mutable, RandomSource randomSource, DifficultyInstance difficultyInstance);

	MapCodec<? extends EnchantmentProvider> codec();
}
