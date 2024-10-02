package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;

public final class Ingredient implements Predicate<ItemStack> {
	public static final StreamCodec<RegistryFriendlyByteBuf, Ingredient> CONTENTS_STREAM_CODEC = ByteBufCodecs.holderSet(Registries.ITEM)
		.map(Ingredient::new, ingredient -> ingredient.values);
	public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> OPTIONAL_CONTENTS_STREAM_CODEC = ByteBufCodecs.holderSet(Registries.ITEM)
		.map(
			holderSet -> holderSet.size() == 0 ? Optional.empty() : Optional.of(new Ingredient(holderSet)),
			optional -> (HolderSet)optional.map(ingredient -> ingredient.values).orElse(HolderSet.direct())
		);
	public static final Codec<HolderSet<Item>> NON_AIR_HOLDER_SET_CODEC = HolderSetCodec.create(Registries.ITEM, ItemStack.ITEM_NON_AIR_CODEC, false);
	public static final Codec<Ingredient> CODEC = ExtraCodecs.nonEmptyHolderSet(NON_AIR_HOLDER_SET_CODEC).xmap(Ingredient::new, ingredient -> ingredient.values);
	private final HolderSet<Item> values;
	@Nullable
	private List<Holder<Item>> items;

	private Ingredient(HolderSet<Item> holderSet) {
		holderSet.unwrap().ifRight(list -> {
			if (list.isEmpty()) {
				throw new UnsupportedOperationException("Ingredients can't be empty");
			} else if (list.contains(Items.AIR.builtInRegistryHolder())) {
				throw new UnsupportedOperationException("Ingredient can't contain air");
			}
		});
		this.values = holderSet;
	}

	public static boolean testOptionalIngredient(Optional<Ingredient> optional, ItemStack itemStack) {
		return (Boolean)optional.map(ingredient -> ingredient.test(itemStack)).orElseGet(itemStack::isEmpty);
	}

	public List<Holder<Item>> items() {
		if (this.items == null) {
			this.items = ImmutableList.copyOf(this.values);
		}

		return this.items;
	}

	public boolean test(ItemStack itemStack) {
		List<Holder<Item>> list = this.items();

		for (int i = 0; i < list.size(); i++) {
			if (itemStack.is((Holder<Item>)list.get(i))) {
				return true;
			}
		}

		return false;
	}

	public boolean equals(Object object) {
		return object instanceof Ingredient ingredient ? Objects.equals(this.values, ingredient.values) : false;
	}

	public static Ingredient of(ItemLike itemLike) {
		return new Ingredient(HolderSet.direct(itemLike.asItem().builtInRegistryHolder()));
	}

	public static Ingredient of(ItemLike... itemLikes) {
		return of(Arrays.stream(itemLikes));
	}

	public static Ingredient of(Stream<? extends ItemLike> stream) {
		return new Ingredient(HolderSet.direct(stream.map(itemLike -> itemLike.asItem().builtInRegistryHolder()).toList()));
	}

	public static Ingredient of(HolderSet<Item> holderSet) {
		return new Ingredient(holderSet);
	}

	public SlotDisplay display() {
		return this.values
			.unwrap()
			.map(
				SlotDisplay.TagSlotDisplay::new,
				list -> new SlotDisplay.Composite((List<SlotDisplay>)list.stream().map(SlotDisplay.ItemSlotDisplay::new).collect(Collectors.toUnmodifiableList()))
			);
	}
}
