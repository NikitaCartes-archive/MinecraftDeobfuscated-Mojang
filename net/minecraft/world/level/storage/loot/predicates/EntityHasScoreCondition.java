/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class EntityHasScoreCondition
implements LootItemCondition {
    private final Map<String, RandomValueBounds> scores;
    private final LootContext.EntityTarget entityTarget;

    private EntityHasScoreCondition(Map<String, RandomValueBounds> map, LootContext.EntityTarget entityTarget) {
        this.scores = ImmutableMap.copyOf(map);
        this.entityTarget = entityTarget;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_SCORES;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());
        if (entity == null) {
            return false;
        }
        Scoreboard scoreboard = entity.level.getScoreboard();
        for (Map.Entry<String, RandomValueBounds> entry : this.scores.entrySet()) {
            if (this.hasScore(entity, scoreboard, entry.getKey(), entry.getValue())) continue;
            return false;
        }
        return true;
    }

    protected boolean hasScore(Entity entity, Scoreboard scoreboard, String string, RandomValueBounds randomValueBounds) {
        Objective objective = scoreboard.getObjective(string);
        if (objective == null) {
            return false;
        }
        String string2 = entity.getScoreboardName();
        if (!scoreboard.hasPlayerScore(string2, objective)) {
            return false;
        }
        return randomValueBounds.matchesValue(scoreboard.getOrCreatePlayerScore(string2, objective).getScore());
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<EntityHasScoreCondition> {
        @Override
        public void serialize(JsonObject jsonObject, EntityHasScoreCondition entityHasScoreCondition, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject2 = new JsonObject();
            for (Map.Entry entry : entityHasScoreCondition.scores.entrySet()) {
                jsonObject2.add((String)entry.getKey(), jsonSerializationContext.serialize(entry.getValue()));
            }
            jsonObject.add("scores", jsonObject2);
            jsonObject.add("entity", jsonSerializationContext.serialize((Object)entityHasScoreCondition.entityTarget));
        }

        @Override
        public EntityHasScoreCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            Set<Map.Entry<String, JsonElement>> set = GsonHelper.getAsJsonObject(jsonObject, "scores").entrySet();
            LinkedHashMap<String, RandomValueBounds> map = Maps.newLinkedHashMap();
            for (Map.Entry<String, JsonElement> entry : set) {
                map.put(entry.getKey(), GsonHelper.convertToObject(entry.getValue(), "score", jsonDeserializationContext, RandomValueBounds.class));
            }
            return new EntityHasScoreCondition(map, GsonHelper.getAsObject(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class));
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

