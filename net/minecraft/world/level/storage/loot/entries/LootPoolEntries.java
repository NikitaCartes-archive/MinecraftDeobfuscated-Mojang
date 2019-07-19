/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.entries.SequentialEntry;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

public class LootPoolEntries {
    private static final Map<ResourceLocation, LootPoolEntryContainer.Serializer<?>> ID_TO_SERIALIZER = Maps.newHashMap();
    private static final Map<Class<?>, LootPoolEntryContainer.Serializer<?>> CLASS_TO_SERIALIZER = Maps.newHashMap();

    private static void register(LootPoolEntryContainer.Serializer<?> serializer) {
        ID_TO_SERIALIZER.put(serializer.getName(), serializer);
        CLASS_TO_SERIALIZER.put(serializer.getContainerClass(), serializer);
    }

    static {
        LootPoolEntries.register(CompositeEntryBase.createSerializer(new ResourceLocation("alternatives"), AlternativesEntry.class, AlternativesEntry::new));
        LootPoolEntries.register(CompositeEntryBase.createSerializer(new ResourceLocation("sequence"), SequentialEntry.class, SequentialEntry::new));
        LootPoolEntries.register(CompositeEntryBase.createSerializer(new ResourceLocation("group"), EntryGroup.class, EntryGroup::new));
        LootPoolEntries.register(new EmptyLootItem.Serializer());
        LootPoolEntries.register(new LootItem.Serializer());
        LootPoolEntries.register(new LootTableReference.Serializer());
        LootPoolEntries.register(new DynamicLoot.Serializer());
        LootPoolEntries.register(new TagEntry.Serializer());
    }

    public static class Serializer
    implements JsonDeserializer<LootPoolEntryContainer>,
    JsonSerializer<LootPoolEntryContainer> {
        @Override
        public LootPoolEntryContainer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entry");
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "type"));
            LootPoolEntryContainer.Serializer serializer = (LootPoolEntryContainer.Serializer)ID_TO_SERIALIZER.get(resourceLocation);
            if (serializer == null) {
                throw new JsonParseException("Unknown item type: " + resourceLocation);
            }
            LootItemCondition[] lootItemConditions = GsonHelper.getAsObject(jsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class);
            return serializer.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
        }

        @Override
        public JsonElement serialize(LootPoolEntryContainer lootPoolEntryContainer, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            LootPoolEntryContainer.Serializer<LootPoolEntryContainer> serializer = Serializer.getSerializer(lootPoolEntryContainer.getClass());
            jsonObject.addProperty("type", serializer.getName().toString());
            if (!ArrayUtils.isEmpty(lootPoolEntryContainer.conditions)) {
                jsonObject.add("conditions", jsonSerializationContext.serialize(lootPoolEntryContainer.conditions));
            }
            serializer.serialize(jsonObject, lootPoolEntryContainer, jsonSerializationContext);
            return jsonObject;
        }

        private static LootPoolEntryContainer.Serializer<LootPoolEntryContainer> getSerializer(Class<?> class_) {
            LootPoolEntryContainer.Serializer serializer = (LootPoolEntryContainer.Serializer)CLASS_TO_SERIALIZER.get(class_);
            if (serializer == null) {
                throw new JsonParseException("Unknown item type: " + class_);
            }
            return serializer;
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((LootPoolEntryContainer)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

