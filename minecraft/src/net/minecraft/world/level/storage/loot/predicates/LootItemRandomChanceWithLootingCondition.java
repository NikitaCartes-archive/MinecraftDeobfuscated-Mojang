package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootItemRandomChanceWithLootingCondition implements LootItemCondition {
	private final float percent;
	private final float lootingMultiplier;

	private LootItemRandomChanceWithLootingCondition(float f, float g) {
		this.percent = f;
		this.lootingMultiplier = g;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.RANDOM_CHANCE_WITH_LOOTING;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
	}

	public boolean test(LootContext lootContext) {
		Entity entity = lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
		int i = 0;
		if (entity instanceof LivingEntity) {
			i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
		}

		return lootContext.getRandom().nextFloat() < this.percent + (float)i * this.lootingMultiplier;
	}

	public static LootItemCondition.Builder randomChanceAndLootingBoost(float f, float g) {
		return () -> new LootItemRandomChanceWithLootingCondition(f, g);
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemRandomChanceWithLootingCondition> {
		public void serialize(
			JsonObject jsonObject, LootItemRandomChanceWithLootingCondition lootItemRandomChanceWithLootingCondition, JsonSerializationContext jsonSerializationContext
		) {
			jsonObject.addProperty("chance", lootItemRandomChanceWithLootingCondition.percent);
			jsonObject.addProperty("looting_multiplier", lootItemRandomChanceWithLootingCondition.lootingMultiplier);
		}

		public LootItemRandomChanceWithLootingCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			return new LootItemRandomChanceWithLootingCondition(GsonHelper.getAsFloat(jsonObject, "chance"), GsonHelper.getAsFloat(jsonObject, "looting_multiplier"));
		}
	}
}
