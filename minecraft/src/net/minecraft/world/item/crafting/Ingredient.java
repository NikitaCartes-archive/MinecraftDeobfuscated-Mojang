package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class Ingredient implements Predicate<ItemStack> {
	public static final Ingredient EMPTY = new Ingredient(Stream.empty());
	private final Ingredient.Value[] values;
	@Nullable
	private ItemStack[] itemStacks;
	@Nullable
	private IntList stackingIds;
	public static final Codec<Ingredient> CODEC = codec(true);
	public static final Codec<Ingredient> CODEC_NONEMPTY = codec(false);

	private Ingredient(Stream<? extends Ingredient.Value> stream) {
		this.values = (Ingredient.Value[])stream.toArray(Ingredient.Value[]::new);
	}

	private Ingredient(Ingredient.Value[] values) {
		this.values = values;
	}

	public ItemStack[] getItems() {
		if (this.itemStacks == null) {
			this.itemStacks = (ItemStack[])Arrays.stream(this.values).flatMap(value -> value.getItems().stream()).distinct().toArray(ItemStack[]::new);
		}

		return this.itemStacks;
	}

	public boolean test(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		} else if (this.isEmpty()) {
			return itemStack.isEmpty();
		} else {
			for (ItemStack itemStack2 : this.getItems()) {
				if (itemStack2.is(itemStack.getItem())) {
					return true;
				}
			}

			return false;
		}
	}

	public IntList getStackingIds() {
		if (this.stackingIds == null) {
			ItemStack[] itemStacks = this.getItems();
			this.stackingIds = new IntArrayList(itemStacks.length);

			for (ItemStack itemStack : itemStacks) {
				this.stackingIds.add(StackedContents.getStackingIndex(itemStack));
			}

			this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
		}

		return this.stackingIds;
	}

	public void toNetwork(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(Arrays.asList(this.getItems()), FriendlyByteBuf::writeItem);
	}

	public JsonElement toJson(boolean bl) {
		Codec<Ingredient> codec = bl ? CODEC : CODEC_NONEMPTY;
		return Util.getOrThrow(codec.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
	}

	public boolean isEmpty() {
		return this.values.length == 0;
	}

	public boolean equals(Object object) {
		return object instanceof Ingredient ingredient ? Arrays.equals(this.values, ingredient.values) : false;
	}

	private static Ingredient fromValues(Stream<? extends Ingredient.Value> stream) {
		Ingredient ingredient = new Ingredient(stream);
		return ingredient.isEmpty() ? EMPTY : ingredient;
	}

	public static Ingredient of() {
		return EMPTY;
	}

	public static Ingredient of(ItemLike... itemLikes) {
		return of(Arrays.stream(itemLikes).map(ItemStack::new));
	}

	public static Ingredient of(ItemStack... itemStacks) {
		return of(Arrays.stream(itemStacks));
	}

	public static Ingredient of(Stream<ItemStack> stream) {
		return fromValues(stream.filter(itemStack -> !itemStack.isEmpty()).map(Ingredient.ItemValue::new));
	}

	public static Ingredient of(TagKey<Item> tagKey) {
		return fromValues(Stream.of(new Ingredient.TagValue(tagKey)));
	}

	public static Ingredient fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		return fromValues(friendlyByteBuf.readList(FriendlyByteBuf::readItem).stream().map(Ingredient.ItemValue::new));
	}

	private static Codec<Ingredient> codec(boolean bl) {
		Codec<Ingredient.Value[]> codec = Codec.list(Ingredient.Value.CODEC)
			.comapFlatMap(
				list -> !bl && list.size() < 1
						? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
						: DataResult.success((Ingredient.Value[])list.toArray(new Ingredient.Value[0])),
				List::of
			);
		return ExtraCodecs.either(codec, Ingredient.Value.CODEC)
			.flatComapMap(
				either -> either.map(Ingredient::new, value -> new Ingredient(new Ingredient.Value[]{value})),
				ingredient -> {
					if (ingredient.values.length == 1) {
						return DataResult.success(Either.right(ingredient.values[0]));
					} else {
						return ingredient.values.length == 0 && !bl
							? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
							: DataResult.success(Either.left(ingredient.values));
					}
				}
			);
	}

	static record ItemValue(ItemStack item) implements Ingredient.Value {
		static final Codec<Ingredient.ItemValue> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(CraftingRecipeCodecs.ITEMSTACK_NONAIR_CODEC.fieldOf("item").forGetter(itemValue -> itemValue.item))
					.apply(instance, Ingredient.ItemValue::new)
		);

		public boolean equals(Object object) {
			return !(object instanceof Ingredient.ItemValue itemValue)
				? false
				: itemValue.item.getItem().equals(this.item.getItem()) && itemValue.item.getCount() == this.item.getCount();
		}

		@Override
		public Collection<ItemStack> getItems() {
			return Collections.singleton(this.item);
		}
	}

	static record TagValue(TagKey<Item> tag) implements Ingredient.Value {
		static final Codec<Ingredient.TagValue> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(tagValue -> tagValue.tag)).apply(instance, Ingredient.TagValue::new)
		);

		public boolean equals(Object object) {
			return object instanceof Ingredient.TagValue tagValue ? tagValue.tag.location().equals(this.tag.location()) : false;
		}

		@Override
		public Collection<ItemStack> getItems() {
			List<ItemStack> list = Lists.<ItemStack>newArrayList();

			for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
				list.add(new ItemStack(holder));
			}

			return list;
		}
	}

	interface Value {
		Codec<Ingredient.Value> CODEC = ExtraCodecs.xor(Ingredient.ItemValue.CODEC, Ingredient.TagValue.CODEC)
			.xmap(either -> either.map(itemValue -> itemValue, tagValue -> tagValue), value -> {
				if (value instanceof Ingredient.TagValue tagValue) {
					return Either.right(tagValue);
				} else if (value instanceof Ingredient.ItemValue itemValue) {
					return Either.left(itemValue);
				} else {
					throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
				}
			});

		Collection<ItemStack> getItems();
	}
}
