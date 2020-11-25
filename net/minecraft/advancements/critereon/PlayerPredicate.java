/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public class PlayerPredicate {
    public static final PlayerPredicate ANY = new Builder().build();
    private final MinMaxBounds.Ints level;
    @Nullable
    private final GameType gameType;
    private final Map<Stat<?>, MinMaxBounds.Ints> stats;
    private final Object2BooleanMap<ResourceLocation> recipes;
    private final Map<ResourceLocation, AdvancementPredicate> advancements;

    private static AdvancementPredicate advancementPredicateFromJson(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            boolean bl = jsonElement.getAsBoolean();
            return new AdvancementDonePredicate(bl);
        }
        Object2BooleanOpenHashMap<String> object2BooleanMap = new Object2BooleanOpenHashMap<String>();
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "criterion data");
        jsonObject.entrySet().forEach(entry -> {
            boolean bl = GsonHelper.convertToBoolean((JsonElement)entry.getValue(), "criterion test");
            object2BooleanMap.put((String)entry.getKey(), bl);
        });
        return new AdvancementCriterionsPredicate(object2BooleanMap);
    }

    private PlayerPredicate(MinMaxBounds.Ints ints, @Nullable GameType gameType, Map<Stat<?>, MinMaxBounds.Ints> map, Object2BooleanMap<ResourceLocation> object2BooleanMap, Map<ResourceLocation, AdvancementPredicate> map2) {
        this.level = ints;
        this.gameType = gameType;
        this.stats = map;
        this.recipes = object2BooleanMap;
        this.advancements = map2;
    }

    public boolean matches(Entity entity) {
        if (this == ANY) {
            return true;
        }
        if (!(entity instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer serverPlayer = (ServerPlayer)entity;
        if (!this.level.matches(serverPlayer.experienceLevel)) {
            return false;
        }
        if (this.gameType != serverPlayer.gameMode.getGameModeForPlayer()) {
            return false;
        }
        ServerStatsCounter statsCounter = serverPlayer.getStats();
        for (Map.Entry<Stat<?>, MinMaxBounds.Ints> entry : this.stats.entrySet()) {
            int n = statsCounter.getValue(entry.getKey());
            if (entry.getValue().matches(n)) continue;
            return false;
        }
        ServerRecipeBook recipeBook = serverPlayer.getRecipeBook();
        for (Object2BooleanMap.Entry entry : this.recipes.object2BooleanEntrySet()) {
            if (recipeBook.contains((ResourceLocation)entry.getKey()) == entry.getBooleanValue()) continue;
            return false;
        }
        if (!this.advancements.isEmpty()) {
            PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
            ServerAdvancementManager serverAdvancementManager = serverPlayer.getServer().getAdvancements();
            for (Map.Entry<ResourceLocation, AdvancementPredicate> entry3 : this.advancements.entrySet()) {
                Advancement advancement = serverAdvancementManager.getAdvancement(entry3.getKey());
                if (advancement != null && entry3.getValue().test(playerAdvancements.getOrStartProgress(advancement))) continue;
                return false;
            }
        }
        return true;
    }

    public static PlayerPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "player");
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("level"));
        String string = GsonHelper.getAsString(jsonObject, "gamemode", "");
        GameType gameType = GameType.byName(string, null);
        HashMap<Stat<?>, MinMaxBounds.Ints> map = Maps.newHashMap();
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "stats", null);
        if (jsonArray != null) {
            for (JsonElement jsonElement2 : jsonArray) {
                JsonObject jsonObject2 = GsonHelper.convertToJsonObject(jsonElement2, "stats entry");
                ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject2, "type"));
                StatType<?> statType = Registry.STAT_TYPE.get(resourceLocation);
                if (statType == null) {
                    throw new JsonParseException("Invalid stat type: " + resourceLocation);
                }
                ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject2, "stat"));
                Stat<?> stat = PlayerPredicate.getStat(statType, resourceLocation2);
                MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject2.get("value"));
                map.put(stat, ints2);
            }
        }
        Object2BooleanOpenHashMap<ResourceLocation> object2BooleanMap = new Object2BooleanOpenHashMap<ResourceLocation>();
        JsonObject jsonObject3 = GsonHelper.getAsJsonObject(jsonObject, "recipes", new JsonObject());
        for (Map.Entry entry : jsonObject3.entrySet()) {
            ResourceLocation resourceLocation3 = new ResourceLocation((String)entry.getKey());
            boolean bl = GsonHelper.convertToBoolean((JsonElement)entry.getValue(), "recipe present");
            object2BooleanMap.put(resourceLocation3, bl);
        }
        HashMap<ResourceLocation, AdvancementPredicate> map2 = Maps.newHashMap();
        JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "advancements", new JsonObject());
        for (Map.Entry<String, JsonElement> entry2 : jsonObject2.entrySet()) {
            ResourceLocation resourceLocation4 = new ResourceLocation(entry2.getKey());
            AdvancementPredicate advancementPredicate = PlayerPredicate.advancementPredicateFromJson(entry2.getValue());
            map2.put(resourceLocation4, advancementPredicate);
        }
        return new PlayerPredicate(ints, gameType, map, object2BooleanMap, map2);
    }

    private static <T> Stat<T> getStat(StatType<T> statType, ResourceLocation resourceLocation) {
        Registry<T> registry = statType.getRegistry();
        T object = registry.get(resourceLocation);
        if (object == null) {
            throw new JsonParseException("Unknown object " + resourceLocation + " for stat type " + Registry.STAT_TYPE.getKey(statType));
        }
        return statType.get(object);
    }

    private static <T> ResourceLocation getStatValueId(Stat<T> stat) {
        return stat.getType().getRegistry().getKey(stat.getValue());
    }

    public JsonElement serializeToJson() {
        JsonObject jsonObject2;
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("level", this.level.serializeToJson());
        if (this.gameType != null) {
            jsonObject.addProperty("gamemode", this.gameType.getName());
        }
        if (!this.stats.isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            this.stats.forEach((stat, ints) -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", Registry.STAT_TYPE.getKey(stat.getType()).toString());
                jsonObject.addProperty("stat", PlayerPredicate.getStatValueId(stat).toString());
                jsonObject.add("value", ints.serializeToJson());
                jsonArray.add(jsonObject);
            });
            jsonObject.add("stats", jsonArray);
        }
        if (!this.recipes.isEmpty()) {
            jsonObject2 = new JsonObject();
            this.recipes.forEach((resourceLocation, boolean_) -> jsonObject2.addProperty(resourceLocation.toString(), (Boolean)boolean_));
            jsonObject.add("recipes", jsonObject2);
        }
        if (!this.advancements.isEmpty()) {
            jsonObject2 = new JsonObject();
            this.advancements.forEach((resourceLocation, advancementPredicate) -> jsonObject2.add(resourceLocation.toString(), advancementPredicate.toJson()));
            jsonObject.add("advancements", jsonObject2);
        }
        return jsonObject;
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        @Nullable
        private GameType gameType;
        private final Map<Stat<?>, MinMaxBounds.Ints> stats = Maps.newHashMap();
        private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap<ResourceLocation>();
        private final Map<ResourceLocation, AdvancementPredicate> advancements = Maps.newHashMap();

        public PlayerPredicate build() {
            return new PlayerPredicate(this.level, this.gameType, this.stats, this.recipes, this.advancements);
        }
    }

    static class AdvancementCriterionsPredicate
    implements AdvancementPredicate {
        private final Object2BooleanMap<String> criterions;

        public AdvancementCriterionsPredicate(Object2BooleanMap<String> object2BooleanMap) {
            this.criterions = object2BooleanMap;
        }

        @Override
        public JsonElement toJson() {
            JsonObject jsonObject = new JsonObject();
            this.criterions.forEach(jsonObject::addProperty);
            return jsonObject;
        }

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            for (Object2BooleanMap.Entry entry : this.criterions.object2BooleanEntrySet()) {
                CriterionProgress criterionProgress = advancementProgress.getCriterion((String)entry.getKey());
                if (criterionProgress != null && criterionProgress.isDone() == entry.getBooleanValue()) continue;
                return false;
            }
            return true;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((AdvancementProgress)object);
        }
    }

    static class AdvancementDonePredicate
    implements AdvancementPredicate {
        private final boolean state;

        public AdvancementDonePredicate(boolean bl) {
            this.state = bl;
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(this.state);
        }

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            return advancementProgress.isDone() == this.state;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((AdvancementProgress)object);
        }
    }

    static interface AdvancementPredicate
    extends Predicate<AdvancementProgress> {
        public JsonElement toJson();
    }
}

