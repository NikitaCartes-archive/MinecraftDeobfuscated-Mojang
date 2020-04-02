/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry
extends LootPoolSingletonContainer {
    private final Tag<Item> tag;
    private final boolean expand;

    private TagEntry(Tag<Item> tag, boolean bl, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
        super(i, j, lootItemConditions, lootItemFunctions);
        this.tag = tag;
        this.expand = bl;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        this.tag.getValues().forEach(item -> consumer.accept(new ItemStack((ItemLike)item)));
    }

    private boolean expandTag(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (this.canRun(lootContext)) {
            for (final Item item : this.tag.getValues()) {
                consumer.accept(new LootPoolSingletonContainer.EntryBase(){

                    @Override
                    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
                        consumer.accept(new ItemStack(item));
                    }
                });
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (this.expand) {
            return this.expandTag(lootContext, consumer);
        }
        return super.expand(lootContext, consumer);
    }

    public static LootPoolSingletonContainer.Builder<?> expandTag(Tag<Item> tag) {
        return TagEntry.simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new TagEntry(tag, true, i, j, lootItemConditions, lootItemFunctions));
    }

    public static class Serializer
    extends LootPoolSingletonContainer.Serializer<TagEntry> {
        public Serializer() {
            super(new ResourceLocation("tag"), TagEntry.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, TagEntry tagEntry, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, tagEntry, jsonSerializationContext);
            jsonObject.addProperty("name", ItemTags.getAllTags().getIdOrThrow(tagEntry.tag).toString());
            jsonObject.addProperty("expand", tagEntry.expand);
        }

        @Override
        protected TagEntry deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
            Tag<Item> tag = ItemTags.getAllTags().getTag(resourceLocation);
            if (tag == null) {
                throw new JsonParseException("Can't find tag: " + resourceLocation);
            }
            boolean bl = GsonHelper.getAsBoolean(jsonObject, "expand");
            return new TagEntry(tag, bl, i, j, lootItemConditions, lootItemFunctions);
        }

        @Override
        protected /* synthetic */ LootPoolSingletonContainer deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
            return this.deserialize(jsonObject, jsonDeserializationContext, i, j, lootItemConditions, lootItemFunctions);
        }
    }
}

