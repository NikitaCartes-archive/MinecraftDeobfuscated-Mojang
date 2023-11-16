package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

public record EntityHasScoreCondition(Map<String, IntRange> scores, LootContext.EntityTarget entityTarget) implements LootItemCondition {
	public static final Codec<EntityHasScoreCondition> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.unboundedMap(Codec.STRING, IntRange.CODEC).fieldOf("scores").forGetter(EntityHasScoreCondition::scores),
					LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(EntityHasScoreCondition::entityTarget)
				)
				.apply(instance, EntityHasScoreCondition::new)
	);

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
			Scoreboard scoreboard = entity.level().getScoreboard();

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
			ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(entity, objective);
			return readOnlyScoreInfo == null ? false : intRange.test(lootContext, readOnlyScoreInfo.value());
		}
	}

	public static EntityHasScoreCondition.Builder hasScores(LootContext.EntityTarget entityTarget) {
		return new EntityHasScoreCondition.Builder(entityTarget);
	}

	public static class Builder implements LootItemCondition.Builder {
		private final ImmutableMap.Builder<String, IntRange> scores = ImmutableMap.builder();
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
			return new EntityHasScoreCondition(this.scores.build(), this.entityTarget);
		}
	}
}
