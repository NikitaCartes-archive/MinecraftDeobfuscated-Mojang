package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ExplosionCondition implements LootItemCondition {
	static final ExplosionCondition INSTANCE = new ExplosionCondition();

	private ExplosionCondition() {
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.SURVIVES_EXPLOSION;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.EXPLOSION_RADIUS);
	}

	public boolean test(LootContext lootContext) {
		Float float_ = lootContext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
		if (float_ != null) {
			RandomSource randomSource = lootContext.getRandom();
			float f = 1.0F / float_;
			return randomSource.nextFloat() <= f;
		} else {
			return true;
		}
	}

	public static LootItemCondition.Builder survivesExplosion() {
		return () -> INSTANCE;
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ExplosionCondition> {
		public void serialize(JsonObject jsonObject, ExplosionCondition explosionCondition, JsonSerializationContext jsonSerializationContext) {
		}

		public ExplosionCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			return ExplosionCondition.INSTANCE;
		}
	}
}
