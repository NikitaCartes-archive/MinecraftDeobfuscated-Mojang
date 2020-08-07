package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class EntityHasScoreCondition implements LootItemCondition {
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

	public boolean test(LootContext lootContext) {
		Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());
		if (entity == null) {
			return false;
		} else {
			Scoreboard scoreboard = entity.level.getScoreboard();

			for (Entry<String, RandomValueBounds> entry : this.scores.entrySet()) {
				if (!this.hasScore(entity, scoreboard, (String)entry.getKey(), (RandomValueBounds)entry.getValue())) {
					return false;
				}
			}

			return true;
		}
	}

	protected boolean hasScore(Entity entity, Scoreboard scoreboard, String string, RandomValueBounds randomValueBounds) {
		Objective objective = scoreboard.getObjective(string);
		if (objective == null) {
			return false;
		} else {
			String string2 = entity.getScoreboardName();
			return !scoreboard.hasPlayerScore(string2, objective)
				? false
				: randomValueBounds.matchesValue(scoreboard.getOrCreatePlayerScore(string2, objective).getScore());
		}
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityHasScoreCondition> {
		public void serialize(JsonObject jsonObject, EntityHasScoreCondition entityHasScoreCondition, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject2 = new JsonObject();

			for (Entry<String, RandomValueBounds> entry : entityHasScoreCondition.scores.entrySet()) {
				jsonObject2.add((String)entry.getKey(), jsonSerializationContext.serialize(entry.getValue()));
			}

			jsonObject.add("scores", jsonObject2);
			jsonObject.add("entity", jsonSerializationContext.serialize(entityHasScoreCondition.entityTarget));
		}

		public EntityHasScoreCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			Set<Entry<String, JsonElement>> set = GsonHelper.getAsJsonObject(jsonObject, "scores").entrySet();
			Map<String, RandomValueBounds> map = Maps.<String, RandomValueBounds>newLinkedHashMap();

			for (Entry<String, JsonElement> entry : set) {
				map.put(entry.getKey(), GsonHelper.convertToObject((JsonElement)entry.getValue(), "score", jsonDeserializationContext, RandomValueBounds.class));
			}

			return new EntityHasScoreCondition(map, GsonHelper.getAsObject(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class));
		}
	}
}
