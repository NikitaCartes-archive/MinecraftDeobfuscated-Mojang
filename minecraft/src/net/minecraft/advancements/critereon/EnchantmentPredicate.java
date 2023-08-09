package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;

public record EnchantmentPredicate(Optional<Holder<Enchantment>> enchantment, MinMaxBounds.Ints level) {
	public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(BuiltInRegistries.ENCHANTMENT.holderByNameCodec(), "enchantment").forGetter(EnchantmentPredicate::enchantment),
					ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "levels", MinMaxBounds.Ints.ANY).forGetter(EnchantmentPredicate::level)
				)
				.apply(instance, EnchantmentPredicate::new)
	);

	public EnchantmentPredicate(Enchantment enchantment, MinMaxBounds.Ints ints) {
		this(Optional.of(enchantment.builtInRegistryHolder()), ints);
	}

	public boolean containedIn(Map<Enchantment, Integer> map) {
		if (this.enchantment.isPresent()) {
			Enchantment enchantment = (Enchantment)((Holder)this.enchantment.get()).value();
			if (!map.containsKey(enchantment)) {
				return false;
			}

			int i = (Integer)map.get(enchantment);
			if (this.level != MinMaxBounds.Ints.ANY && !this.level.matches(i)) {
				return false;
			}
		} else if (this.level != MinMaxBounds.Ints.ANY) {
			for (Integer integer : map.values()) {
				if (this.level.matches(integer)) {
					return true;
				}
			}

			return false;
		}

		return true;
	}
}
