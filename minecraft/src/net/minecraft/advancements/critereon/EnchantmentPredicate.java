package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentPredicate(Optional<Holder<Enchantment>> enchantment, MinMaxBounds.Ints level) {
	public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BuiltInRegistries.ENCHANTMENT.holderByNameCodec().optionalFieldOf("enchantment").forGetter(EnchantmentPredicate::enchantment),
					MinMaxBounds.Ints.CODEC.optionalFieldOf("levels", MinMaxBounds.Ints.ANY).forGetter(EnchantmentPredicate::level)
				)
				.apply(instance, EnchantmentPredicate::new)
	);

	public EnchantmentPredicate(Enchantment enchantment, MinMaxBounds.Ints ints) {
		this(Optional.of(enchantment.builtInRegistryHolder()), ints);
	}

	public boolean containedIn(ItemEnchantments itemEnchantments) {
		if (this.enchantment.isPresent()) {
			Enchantment enchantment = (Enchantment)((Holder)this.enchantment.get()).value();
			int i = itemEnchantments.getLevel(enchantment);
			if (i == 0) {
				return false;
			}

			if (this.level != MinMaxBounds.Ints.ANY && !this.level.matches(i)) {
				return false;
			}
		} else if (this.level != MinMaxBounds.Ints.ANY) {
			for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
				if (this.level.matches(entry.getIntValue())) {
					return true;
				}
			}

			return false;
		}

		return true;
	}
}
