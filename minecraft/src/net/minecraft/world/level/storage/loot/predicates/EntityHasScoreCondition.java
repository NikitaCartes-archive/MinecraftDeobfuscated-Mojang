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
import java.util.stream.Stream;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class EntityHasScoreCondition implements LootItemCondition {
	final Map<String, IntRange> scores;
	final LootContext.EntityTarget entityTarget;

	EntityHasScoreCondition(Map<String, IntRange> map, LootContext.EntityTarget entityTarget) {
		this.scores = ImmutableMap.copyOf(map);
		this.entityTarget = entityTarget;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.ENTITY_SCORES;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return (Set<LootContextParam<?>>)Stream.concat(
				Stream.of(this.entityTarget.getParam()), this.scores.values().stream().flatMap(intRange -> intRange.getReferencedContextParams().stream())
			)
			.collect(ImmutableSet.toImmutableSet());
	}

	public boolean test(LootContext lootContext) {
		Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());
		if (entity == null) {
			return false;
		} else {
			Scoreboard scoreboard = entity.level.getScoreboard();

			for (Entry<String, IntRange> entry : this.scores.entrySet()) {
				if (!this.hasScore(lootContext, entity, scoreboard, (String)entry.getKey(), (IntRange)entry.getValue())) {
					return false;
				}
			}

			return true;
		}
	}

	protected boolean hasScore(LootContext lootContext, Entity entity, Scoreboard scoreboard, String string, IntRange intRange) {
		Objective objective = scoreboard.getObjective(string);
		if (objective == null) {
			return false;
		} else {
			String string2 = entity.getScoreboardName();
			return !scoreboard.hasPlayerScore(string2, objective) ? false : intRange.test(lootContext, scoreboard.getOrCreatePlayerScore(string2, objective).getScore());
		}
	}

	public static EntityHasScoreCondition.Builder hasScores(LootContext.EntityTarget entityTarget) {
		return new EntityHasScoreCondition.Builder(entityTarget);
	}

	public static class Builder implements LootItemCondition.Builder {
		private final Map<String, IntRange> scores = Maps.<String, IntRange>newHashMap();
		private final LootContext.EntityTarget entityTarget;

		public Builder(LootContext.EntityTarget entityTarget) {
			this.entityTarget = entityTarget;
		}

		public EntityHasScoreCondition.Builder withScore(String string, IntRange intRange) {
			this.scores.put(string, intRange);
			return this;
		}

		@Override
		public LootItemCondition build() {
			return new EntityHasScoreCondition(this.scores, this.entityTarget);
		}
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityHasScoreCondition> {
		public void serialize(JsonObject jsonObject, EntityHasScoreCondition entityHasScoreCondition, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject2 = new JsonObject();

			for (Entry<String, IntRange> entry : entityHasScoreCondition.scores.entrySet()) {
				jsonObject2.add((String)entry.getKey(), jsonSerializationContext.serialize(entry.getValue()));
			}

			jsonObject.add("scores", jsonObject2);
			jsonObject.add("entity", jsonSerializationContext.serialize(entityHasScoreCondition.entityTarget));
		}

		public EntityHasScoreCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			Set<Entry<String, JsonElement>> set = GsonHelper.getAsJsonObject(jsonObject, "scores").entrySet();
			Map<String, IntRange> map = Maps.<String, IntRange>newLinkedHashMap();

			for (Entry<String, JsonElement> entry : set) {
				map.put((String)entry.getKey(), (IntRange)GsonHelper.convertToObject((JsonElement)entry.getValue(), "score", jsonDeserializationContext, IntRange.class));
			}

			return new EntityHasScoreCondition(map, GsonHelper.getAsObject(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class));
		}
	}
}
