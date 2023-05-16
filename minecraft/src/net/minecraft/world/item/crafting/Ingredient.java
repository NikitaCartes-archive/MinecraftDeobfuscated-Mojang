package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class Ingredient implements Predicate<ItemStack> {
	public static final Ingredient EMPTY = new Ingredient(Stream.empty());
	private final Ingredient.Value[] values;
	@Nullable
	private ItemStack[] itemStacks;
	@Nullable
	private IntList stackingIds;

	private Ingredient(Stream<? extends Ingredient.Value> stream) {
		this.values = (Ingredient.Value[])stream.toArray(Ingredient.Value[]::new);
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

	public JsonElement toJson() {
		if (this.values.length == 1) {
			return this.values[0].serialize();
		} else {
			JsonArray jsonArray = new JsonArray();

			for (Ingredient.Value value : this.values) {
				jsonArray.add(value.serialize());
			}

			return jsonArray;
		}
	}

	public boolean isEmpty() {
		return this.values.length == 0;
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

	public static Ingredient fromJson(@Nullable JsonElement jsonElement) {
		return fromJson(jsonElement, true);
	}

	public static Ingredient fromJson(@Nullable JsonElement jsonElement, boolean bl) {
		if (jsonElement == null || jsonElement.isJsonNull()) {
			throw new JsonSyntaxException("Item cannot be null");
		} else if (jsonElement.isJsonObject()) {
			return fromValues(Stream.of(valueFromJson(jsonElement.getAsJsonObject(), bl)).filter(Objects::nonNull));
		} else if (jsonElement.isJsonArray()) {
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			if (jsonArray.size() == 0 && !bl) {
				throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
			} else {
				return fromValues(
					StreamSupport.stream(jsonArray.spliterator(), false)
						.map(jsonElementx -> valueFromJson(GsonHelper.convertToJsonObject(jsonElementx, "item"), false))
						.filter(Objects::nonNull)
				);
			}
		} else {
			throw new JsonSyntaxException("Expected item to be object or array of objects");
		}
	}

	@Nullable
	private static Ingredient.Value valueFromJson(JsonObject jsonObject, boolean bl) {
		if (jsonObject.has("item") && jsonObject.has("tag")) {
			throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
		} else if (jsonObject.has("item")) {
			String string = GsonHelper.getAsString(jsonObject, "item");
			Item item = (Item)BuiltInRegistries.ITEM
				.getOptional(new ResourceLocation(string))
				.orElseThrow(() -> new JsonSyntaxException("Unknown item '" + string + "'"));
			if (item == Items.AIR) {
				if (bl) {
					return null;
				} else {
					throw new JsonSyntaxException("Empty ingredient not allowed here");
				}
			} else {
				return new Ingredient.ItemValue(new ItemStack(item));
			}
		} else if (jsonObject.has("tag")) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
			TagKey<Item> tagKey = TagKey.create(Registries.ITEM, resourceLocation);
			return new Ingredient.TagValue(tagKey);
		} else {
			throw new JsonParseException("An ingredient entry needs either a tag or an item");
		}
	}

	static class ItemValue implements Ingredient.Value {
		private final ItemStack item;

		ItemValue(ItemStack itemStack) {
			this.item = itemStack;
		}

		@Override
		public Collection<ItemStack> getItems() {
			return Collections.singleton(this.item);
		}

		@Override
		public JsonObject serialize() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.item.getItem()).toString());
			return jsonObject;
		}
	}

	static class TagValue implements Ingredient.Value {
		private final TagKey<Item> tag;

		TagValue(TagKey<Item> tagKey) {
			this.tag = tagKey;
		}

		@Override
		public Collection<ItemStack> getItems() {
			List<ItemStack> list = Lists.<ItemStack>newArrayList();

			for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
				list.add(new ItemStack(holder));
			}

			return list;
		}

		@Override
		public JsonObject serialize() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("tag", this.tag.location().toString());
			return jsonObject;
		}
	}

	interface Value {
		Collection<ItemStack> getItems();

		JsonObject serialize();
	}
}
