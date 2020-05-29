package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class DamageSourceCondition implements LootItemCondition {
	private final DamageSourcePredicate predicate;

	private DamageSourceCondition(DamageSourcePredicate damageSourcePredicate) {
		this.predicate = damageSourcePredicate;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.DAMAGE_SOURCE_PROPERTIES;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.BLOCK_POS, LootContextParams.DAMAGE_SOURCE);
	}

	public boolean test(LootContext lootContext) {
		DamageSource damageSource = lootContext.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
		BlockPos blockPos = lootContext.getParamOrNull(LootContextParams.BLOCK_POS);
		return blockPos != null && damageSource != null && this.predicate.matches(lootContext.getLevel(), Vec3.atLowerCornerOf(blockPos), damageSource);
	}

	public static LootItemCondition.Builder hasDamageSource(DamageSourcePredicate.Builder builder) {
		return () -> new DamageSourceCondition(builder.build());
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<DamageSourceCondition> {
		public void serialize(JsonObject jsonObject, DamageSourceCondition damageSourceCondition, JsonSerializationContext jsonSerializationContext) {
			jsonObject.add("predicate", damageSourceCondition.predicate.serializeToJson());
		}

		public DamageSourceCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			DamageSourcePredicate damageSourcePredicate = DamageSourcePredicate.fromJson(jsonObject.get("predicate"));
			return new DamageSourceCondition(damageSourcePredicate);
		}
	}
}
