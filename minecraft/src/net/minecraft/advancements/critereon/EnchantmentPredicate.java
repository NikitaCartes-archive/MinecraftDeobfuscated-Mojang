package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentPredicate(Optional<HolderSet<Enchantment>> enchantments, MinMaxBounds.Ints level) {
	public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(EnchantmentPredicate::enchantments),
					MinMaxBounds.Ints.CODEC.optionalFieldOf("levels", MinMaxBounds.Ints.ANY).forGetter(EnchantmentPredicate::level)
				)
				.apply(instance, EnchantmentPredicate::new)
	);

	public EnchantmentPredicate(Holder<Enchantment> holder, MinMaxBounds.Ints ints) {
		this(Optional.of(HolderSet.direct(holder)), ints);
	}

	public EnchantmentPredicate(HolderSet<Enchantment> holderSet, MinMaxBounds.Ints ints) {
		this(Optional.of(holderSet), ints);
	}

	public boolean containedIn(ItemEnchantments itemEnchantments) {
		if (this.enchantments.isPresent()) {
			for (Holder<Enchantment> holder : (HolderSet)this.enchantments.get()) {
				if (this.matchesEnchantment(itemEnchantments, holder)) {
					return true;
				}
			}

			return false;
		} else if (this.level != MinMaxBounds.Ints.ANY) {
			for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
				if (this.level.matches(entry.getIntValue())) {
					return true;
				}
			}

			return false;
		} else {
			return !itemEnchantments.isEmpty();
		}
	}

	private boolean matchesEnchantment(ItemEnchantments itemEnchantments, Holder<Enchantment> holder) {
		int i = itemEnchantments.getLevel(holder);
		if (i == 0) {
			return false;
		} else {
			return this.level == MinMaxBounds.Ints.ANY ? true : this.level.matches(i);
		}
	}
}
