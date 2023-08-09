package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;

public record ItemPredicate(
	Optional<TagKey<Item>> tag,
	Optional<HolderSet<Item>> items,
	MinMaxBounds.Ints count,
	MinMaxBounds.Ints durability,
	List<EnchantmentPredicate> enchantments,
	List<EnchantmentPredicate> storedEnchantments,
	Optional<Holder<Potion>> potion,
	Optional<NbtPredicate> nbt
) {
	private static final Codec<HolderSet<Item>> ITEMS_CODEC = BuiltInRegistries.ITEM
		.holderByNameCodec()
		.listOf()
		.xmap(HolderSet::direct, holderSet -> holderSet.stream().toList());
	public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(TagKey.codec(Registries.ITEM), "tag").forGetter(ItemPredicate::tag),
					ExtraCodecs.strictOptionalField(ITEMS_CODEC, "items").forGetter(ItemPredicate::items),
					ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "count", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::count),
					ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "durability", MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::durability),
					ExtraCodecs.strictOptionalField(EnchantmentPredicate.CODEC.listOf(), "enchantments", List.of()).forGetter(ItemPredicate::enchantments),
					ExtraCodecs.strictOptionalField(EnchantmentPredicate.CODEC.listOf(), "stored_enchantments", List.of()).forGetter(ItemPredicate::storedEnchantments),
					ExtraCodecs.strictOptionalField(BuiltInRegistries.POTION.holderByNameCodec(), "potion").forGetter(ItemPredicate::potion),
					ExtraCodecs.strictOptionalField(NbtPredicate.CODEC, "nbt").forGetter(ItemPredicate::nbt)
				)
				.apply(instance, ItemPredicate::new)
	);

	static Optional<ItemPredicate> of(
		Optional<TagKey<Item>> optional,
		Optional<HolderSet<Item>> optional2,
		MinMaxBounds.Ints ints,
		MinMaxBounds.Ints ints2,
		List<EnchantmentPredicate> list,
		List<EnchantmentPredicate> list2,
		Optional<Holder<Potion>> optional3,
		Optional<NbtPredicate> optional4
	) {
		return optional.isEmpty()
				&& optional2.isEmpty()
				&& ints.isAny()
				&& ints2.isAny()
				&& list.isEmpty()
				&& list2.isEmpty()
				&& optional3.isEmpty()
				&& optional4.isEmpty()
			? Optional.empty()
			: Optional.of(new ItemPredicate(optional, optional2, ints, ints2, list, list2, optional3, optional4));
	}

	public boolean matches(ItemStack itemStack) {
		if (this.tag.isPresent() && !itemStack.is((TagKey<Item>)this.tag.get())) {
			return false;
		} else if (this.items.isPresent() && !itemStack.is((HolderSet<Item>)this.items.get())) {
			return false;
		} else if (!this.count.matches(itemStack.getCount())) {
			return false;
		} else if (!this.durability.isAny() && !itemStack.isDamageableItem()) {
			return false;
		} else if (!this.durability.matches(itemStack.getMaxDamage() - itemStack.getDamageValue())) {
			return false;
		} else if (this.nbt.isPresent() && !((NbtPredicate)this.nbt.get()).matches(itemStack)) {
			return false;
		} else {
			if (!this.enchantments.isEmpty()) {
				Map<Enchantment, Integer> map = EnchantmentHelper.deserializeEnchantments(itemStack.getEnchantmentTags());

				for (EnchantmentPredicate enchantmentPredicate : this.enchantments) {
					if (!enchantmentPredicate.containedIn(map)) {
						return false;
					}
				}
			}

			if (!this.storedEnchantments.isEmpty()) {
				Map<Enchantment, Integer> map = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(itemStack));

				for (EnchantmentPredicate enchantmentPredicatex : this.storedEnchantments) {
					if (!enchantmentPredicatex.containedIn(map)) {
						return false;
					}
				}
			}

			return !this.potion.isPresent() || ((Holder)this.potion.get()).value() == PotionUtils.getPotion(itemStack);
		}
	}

	public static Optional<ItemPredicate> fromJson(@Nullable JsonElement jsonElement) {
		return jsonElement != null && !jsonElement.isJsonNull()
			? Optional.of(Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, jsonElement), JsonParseException::new))
			: Optional.empty();
	}

	public JsonElement serializeToJson() {
		return Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
	}

	public static JsonElement serializeToJsonArray(List<ItemPredicate> list) {
		return Util.getOrThrow(CODEC.listOf().encodeStart(JsonOps.INSTANCE, list), IllegalStateException::new);
	}

	public static List<ItemPredicate> fromJsonArray(@Nullable JsonElement jsonElement) {
		return jsonElement != null && !jsonElement.isJsonNull()
			? Util.getOrThrow(CODEC.listOf().parse(JsonOps.INSTANCE, jsonElement), JsonParseException::new)
			: List.of();
	}

	public static class Builder {
		private final ImmutableList.Builder<EnchantmentPredicate> enchantments = ImmutableList.builder();
		private final ImmutableList.Builder<EnchantmentPredicate> storedEnchantments = ImmutableList.builder();
		private Optional<HolderSet<Item>> items = Optional.empty();
		private Optional<TagKey<Item>> tag = Optional.empty();
		private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
		private MinMaxBounds.Ints durability = MinMaxBounds.Ints.ANY;
		private Optional<Holder<Potion>> potion = Optional.empty();
		private Optional<NbtPredicate> nbt = Optional.empty();

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
			this.tag = Optional.of(tagKey);
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

		public ItemPredicate.Builder isPotion(Potion potion) {
			this.potion = Optional.of(potion.builtInRegistryHolder());
			return this;
		}

		public ItemPredicate.Builder hasNbt(CompoundTag compoundTag) {
			this.nbt = Optional.of(new NbtPredicate(compoundTag));
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

		public Optional<ItemPredicate> build() {
			return ItemPredicate.of(this.tag, this.items, this.count, this.durability, this.enchantments.build(), this.storedEnchantments.build(), this.potion, this.nbt);
		}
	}
}
