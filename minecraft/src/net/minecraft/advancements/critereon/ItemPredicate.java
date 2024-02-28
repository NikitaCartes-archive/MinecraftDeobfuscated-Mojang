package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

public record ItemPredicate(
	Optional<HolderSet<Item>> items,
	MinMaxBounds.Ints count,
	MinMaxBounds.Ints durability,
	List<EnchantmentPredicate> enchantments,
	List<EnchantmentPredicate> storedEnchantments,
	Optional<HolderSet<Potion>> potions,
	Optional<NbtPredicate> customData,
	DataComponentPredicate components
) {
	public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(RegistryCodecs.homogeneousList(Registries.ITEM), "items").forGetter(ItemPredicate::items),
					ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "count", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::count),
					ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "durability", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::durability),
					ExtraCodecs.strictOptionalField(EnchantmentPredicate.CODEC.listOf(), "enchantments", List.of()).forGetter(ItemPredicate::enchantments),
					ExtraCodecs.strictOptionalField(EnchantmentPredicate.CODEC.listOf(), "stored_enchantments", List.of()).forGetter(ItemPredicate::storedEnchantments),
					ExtraCodecs.strictOptionalField(RegistryCodecs.homogeneousList(Registries.POTION), "potions").forGetter(ItemPredicate::potions),
					ExtraCodecs.strictOptionalField(NbtPredicate.CODEC, "custom_data").forGetter(ItemPredicate::customData),
					ExtraCodecs.strictOptionalField(DataComponentPredicate.CODEC, "components", DataComponentPredicate.EMPTY).forGetter(ItemPredicate::components)
				)
				.apply(instance, ItemPredicate::new)
	);

	public boolean matches(ItemStack itemStack) {
		if (this.items.isPresent() && !itemStack.is((HolderSet<Item>)this.items.get())) {
			return false;
		} else if (!this.count.matches(itemStack.getCount())) {
			return false;
		} else if (!this.durability.isAny() && !itemStack.isDamageableItem()) {
			return false;
		} else if (!this.durability.matches(itemStack.getMaxDamage() - itemStack.getDamageValue())) {
			return false;
		} else if (this.customData.isPresent() && !((NbtPredicate)this.customData.get()).matches(itemStack)) {
			return false;
		} else {
			if (!this.enchantments.isEmpty()) {
				ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

				for (EnchantmentPredicate enchantmentPredicate : this.enchantments) {
					if (!enchantmentPredicate.containedIn(itemEnchantments)) {
						return false;
					}
				}
			}

			if (!this.storedEnchantments.isEmpty()) {
				ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

				for (EnchantmentPredicate enchantmentPredicatex : this.storedEnchantments) {
					if (!enchantmentPredicatex.containedIn(itemEnchantments)) {
						return false;
					}
				}
			}

			if (this.potions.isPresent()) {
				Optional<Holder<Potion>> optional = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
				if (optional.isEmpty() || !((HolderSet)this.potions.get()).contains((Holder)optional.get())) {
					return false;
				}
			}

			return this.components.test((DataComponentHolder)itemStack);
		}
	}

	public static class Builder {
		private final ImmutableList.Builder<EnchantmentPredicate> enchantments = ImmutableList.builder();
		private final ImmutableList.Builder<EnchantmentPredicate> storedEnchantments = ImmutableList.builder();
		private Optional<HolderSet<Item>> items = Optional.empty();
		private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
		private MinMaxBounds.Ints durability = MinMaxBounds.Ints.ANY;
		private Optional<HolderSet<Potion>> potions = Optional.empty();
		private Optional<NbtPredicate> customData = Optional.empty();
		private DataComponentPredicate components = DataComponentPredicate.EMPTY;

		private Builder() {
		}

		public static ItemPredicate.Builder item() {
			return new ItemPredicate.Builder();
		}

		public ItemPredicate.Builder of(ItemLike... itemLikes) {
			this.items = Optional.of(HolderSet.direct(itemLike -> itemLike.asItem().builtInRegistryHolder(), itemLikes));
			return this;
		}

		public ItemPredicate.Builder of(TagKey<Item> tagKey) {
			this.items = Optional.of(BuiltInRegistries.ITEM.getOrCreateTag(tagKey));
			return this;
		}

		public ItemPredicate.Builder withCount(MinMaxBounds.Ints ints) {
			this.count = ints;
			return this;
		}

		public ItemPredicate.Builder hasDurability(MinMaxBounds.Ints ints) {
			this.durability = ints;
			return this;
		}

		public ItemPredicate.Builder isPotion(HolderSet<Potion> holderSet) {
			this.potions = Optional.of(holderSet);
			return this;
		}

		public ItemPredicate.Builder hasCustomData(CompoundTag compoundTag) {
			this.customData = Optional.of(new NbtPredicate(compoundTag));
			return this;
		}

		public ItemPredicate.Builder hasEnchantment(EnchantmentPredicate enchantmentPredicate) {
			this.enchantments.add(enchantmentPredicate);
			return this;
		}

		public ItemPredicate.Builder hasStoredEnchantment(EnchantmentPredicate enchantmentPredicate) {
			this.storedEnchantments.add(enchantmentPredicate);
			return this;
		}

		public ItemPredicate.Builder hasComponents(DataComponentPredicate dataComponentPredicate) {
			this.components = dataComponentPredicate;
			return this;
		}

		public ItemPredicate build() {
			List<EnchantmentPredicate> list = this.enchantments.build();
			List<EnchantmentPredicate> list2 = this.storedEnchantments.build();
			return new ItemPredicate(this.items, this.count, this.durability, list, list2, this.potions, this.customData, this.components);
		}
	}
}
