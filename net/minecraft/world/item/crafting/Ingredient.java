/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public final class Ingredient
implements Predicate<ItemStack> {
    public static final Ingredient EMPTY = new Ingredient(Stream.empty());
    private final Value[] values;
    @Nullable
    private ItemStack[] itemStacks;
    @Nullable
    private IntList stackingIds;

    private Ingredient(Stream<? extends Value> stream) {
        this.values = (Value[])stream.toArray(Value[]::new);
    }

    public ItemStack[] getItems() {
        if (this.itemStacks == null) {
            this.itemStacks = (ItemStack[])Arrays.stream(this.values).flatMap(value -> value.getItems().stream()).distinct().toArray(ItemStack[]::new);
        }
        return this.itemStacks;
    }

    @Override
    public boolean test(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        if (this.isEmpty()) {
            return itemStack.isEmpty();
        }
        for (ItemStack itemStack2 : this.getItems()) {
            if (!itemStack2.is(itemStack.getItem())) continue;
            return true;
        }
        return false;
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
        }
        JsonArray jsonArray = new JsonArray();
        for (Value value : this.values) {
            jsonArray.add(value.serialize());
        }
        return jsonArray;
    }

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    private static Ingredient fromValues(Stream<? extends Value> stream) {
        Ingredient ingredient = new Ingredient(stream);
        return ingredient.isEmpty() ? EMPTY : ingredient;
    }

    public static Ingredient of() {
        return EMPTY;
    }

    public static Ingredient of(ItemLike ... itemLikes) {
        return Ingredient.of(Arrays.stream(itemLikes).map(ItemStack::new));
    }

    public static Ingredient of(ItemStack ... itemStacks) {
        return Ingredient.of(Arrays.stream(itemStacks));
    }

    public static Ingredient of(Stream<ItemStack> stream) {
        return Ingredient.fromValues(stream.filter(itemStack -> !itemStack.isEmpty()).map(ItemValue::new));
    }

    public static Ingredient of(TagKey<Item> tagKey) {
        return Ingredient.fromValues(Stream.of(new TagValue(tagKey)));
    }

    public static Ingredient fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return Ingredient.fromValues(friendlyByteBuf.readList(FriendlyByteBuf::readItem).stream().map(ItemValue::new));
    }

    public static Ingredient fromJson(@Nullable JsonElement jsonElement2) {
        if (jsonElement2 == null || jsonElement2.isJsonNull()) {
            throw new JsonSyntaxException("Item cannot be null");
        }
        if (jsonElement2.isJsonObject()) {
            return Ingredient.fromValues(Stream.of(Ingredient.valueFromJson(jsonElement2.getAsJsonObject())));
        }
        if (jsonElement2.isJsonArray()) {
            JsonArray jsonArray = jsonElement2.getAsJsonArray();
            if (jsonArray.size() == 0) {
                throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
            }
            return Ingredient.fromValues(StreamSupport.stream(jsonArray.spliterator(), false).map(jsonElement -> Ingredient.valueFromJson(GsonHelper.convertToJsonObject(jsonElement, "item"))));
        }
        throw new JsonSyntaxException("Expected item to be object or array of objects");
    }

    private static Value valueFromJson(JsonObject jsonObject) {
        if (jsonObject.has("item") && jsonObject.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        }
        if (jsonObject.has("item")) {
            Item item = ShapedRecipe.itemFromJson(jsonObject);
            return new ItemValue(new ItemStack(item));
        }
        if (jsonObject.has("tag")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, resourceLocation);
            return new TagValue(tagKey);
        }
        throw new JsonParseException("An ingredient entry needs either a tag or an item");
    }

    @Override
    public /* synthetic */ boolean test(@Nullable Object object) {
        return this.test((ItemStack)object);
    }

    static interface Value {
        public Collection<ItemStack> getItems();

        public JsonObject serialize();
    }

    static class TagValue
    implements Value {
        private final TagKey<Item> tag;

        TagValue(TagKey<Item> tagKey) {
            this.tag = tagKey;
        }

        @Override
        public Collection<ItemStack> getItems() {
            ArrayList<ItemStack> list = Lists.newArrayList();
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

    static class ItemValue
    implements Value {
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
}

