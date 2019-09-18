/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionReference;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.EntityHasScoreCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;

public class LootItemConditions {
    private static final Map<ResourceLocation, LootItemCondition.Serializer<?>> CONDITIONS_BY_NAME = Maps.newHashMap();
    private static final Map<Class<? extends LootItemCondition>, LootItemCondition.Serializer<?>> CONDITIONS_BY_CLASS = Maps.newHashMap();

    public static <T extends LootItemCondition> void register(LootItemCondition.Serializer<? extends T> serializer) {
        ResourceLocation resourceLocation = serializer.getName();
        Class<T> class_ = serializer.getPredicateClass();
        if (CONDITIONS_BY_NAME.containsKey(resourceLocation)) {
            throw new IllegalArgumentException("Can't re-register item condition name " + resourceLocation);
        }
        if (CONDITIONS_BY_CLASS.containsKey(class_)) {
            throw new IllegalArgumentException("Can't re-register item condition class " + class_.getName());
        }
        CONDITIONS_BY_NAME.put(resourceLocation, serializer);
        CONDITIONS_BY_CLASS.put(class_, serializer);
    }

    public static LootItemCondition.Serializer<?> getSerializer(ResourceLocation resourceLocation) {
        LootItemCondition.Serializer<?> serializer = CONDITIONS_BY_NAME.get(resourceLocation);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown loot item condition '" + resourceLocation + "'");
        }
        return serializer;
    }

    public static <T extends LootItemCondition> LootItemCondition.Serializer<T> getSerializer(T lootItemCondition) {
        LootItemCondition.Serializer<?> serializer = CONDITIONS_BY_CLASS.get(lootItemCondition.getClass());
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown loot item condition " + lootItemCondition);
        }
        return serializer;
    }

    public static <T> Predicate<T> andConditions(Predicate<T>[] predicates) {
        switch (predicates.length) {
            case 0: {
                return object -> true;
            }
            case 1: {
                return predicates[0];
            }
            case 2: {
                return predicates[0].and(predicates[1]);
            }
        }
        return object -> {
            for (Predicate predicate : predicates) {
                if (predicate.test(object)) continue;
                return false;
            }
            return true;
        };
    }

    public static <T> Predicate<T> orConditions(Predicate<T>[] predicates) {
        switch (predicates.length) {
            case 0: {
                return object -> false;
            }
            case 1: {
                return predicates[0];
            }
            case 2: {
                return predicates[0].or(predicates[1]);
            }
        }
        return object -> {
            for (Predicate predicate : predicates) {
                if (!predicate.test(object)) continue;
                return true;
            }
            return false;
        };
    }

    static {
        LootItemConditions.register(new InvertedLootItemCondition.Serializer());
        LootItemConditions.register(new AlternativeLootItemCondition.Serializer());
        LootItemConditions.register(new LootItemRandomChanceCondition.Serializer());
        LootItemConditions.register(new LootItemRandomChanceWithLootingCondition.Serializer());
        LootItemConditions.register(new LootItemEntityPropertyCondition.Serializer());
        LootItemConditions.register(new LootItemKilledByPlayerCondition.Serializer());
        LootItemConditions.register(new EntityHasScoreCondition.Serializer());
        LootItemConditions.register(new LootItemBlockStatePropertyCondition.Serializer());
        LootItemConditions.register(new MatchTool.Serializer());
        LootItemConditions.register(new BonusLevelTableCondition.Serializer());
        LootItemConditions.register(new ExplosionCondition.Serializer());
        LootItemConditions.register(new DamageSourceCondition.Serializer());
        LootItemConditions.register(new LocationCheck.Serializer());
        LootItemConditions.register(new WeatherCheck.Serializer());
        LootItemConditions.register(new ConditionReference.Serializer());
        LootItemConditions.register(new TimeCheck.Serializer());
    }

    public static class Serializer
    implements JsonDeserializer<LootItemCondition>,
    JsonSerializer<LootItemCondition> {
        @Override
        public LootItemCondition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            LootItemCondition.Serializer<ResourceLocation> serializer;
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "condition");
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "condition"));
            try {
                serializer = LootItemConditions.getSerializer(resourceLocation);
            } catch (IllegalArgumentException illegalArgumentException) {
                throw new JsonSyntaxException("Unknown condition '" + resourceLocation + "'");
            }
            return serializer.deserialize(jsonObject, jsonDeserializationContext);
        }

        @Override
        public JsonElement serialize(LootItemCondition lootItemCondition, Type type, JsonSerializationContext jsonSerializationContext) {
            LootItemCondition.Serializer<LootItemCondition> serializer = LootItemConditions.getSerializer(lootItemCondition);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("condition", serializer.getName().toString());
            serializer.serialize(jsonObject, lootItemCondition, jsonSerializationContext);
            return jsonObject;
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((LootItemCondition)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

