package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootingEnchantFunction extends LootItemConditionalFunction {
	private final RandomValueBounds value;
	private final int limit;

	private LootingEnchantFunction(LootItemCondition[] lootItemConditions, RandomValueBounds randomValueBounds, int i) {
		super(lootItemConditions);
		this.value = randomValueBounds;
		this.limit = i;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.LOOTING_ENCHANT;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.KILLER_ENTITY);
	}

	private boolean hasLimit() {
		return this.limit > 0;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Entity entity = lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
		if (entity instanceof LivingEntity) {
			int i = EnchantmentHelper.getMobLooting((LivingEntity)entity);
			if (i == 0) {
				return itemStack;
			}

			float f = (float)i * this.value.getFloat(lootContext.getRandom());
			itemStack.grow(Math.round(f));
			if (this.hasLimit() && itemStack.getCount() > this.limit) {
				itemStack.setCount(this.limit);
			}
		}

		return itemStack;
	}

	public static LootingEnchantFunction.Builder lootingMultiplier(RandomValueBounds randomValueBounds) {
		return new LootingEnchantFunction.Builder(randomValueBounds);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<LootingEnchantFunction.Builder> {
		private final RandomValueBounds count;
		private int limit = 0;

		public Builder(RandomValueBounds randomValueBounds) {
			this.count = randomValueBounds;
		}

		protected LootingEnchantFunction.Builder getThis() {
			return this;
		}

		public LootingEnchantFunction.Builder setLimit(int i) {
			this.limit = i;
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new LootingEnchantFunction(this.getConditions(), this.count, this.limit);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<LootingEnchantFunction> {
		public void serialize(JsonObject jsonObject, LootingEnchantFunction lootingEnchantFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, lootingEnchantFunction, jsonSerializationContext);
			jsonObject.add("count", jsonSerializationContext.serialize(lootingEnchantFunction.value));
			if (lootingEnchantFunction.hasLimit()) {
				jsonObject.add("limit", jsonSerializationContext.serialize(lootingEnchantFunction.limit));
			}
		}

		public LootingEnchantFunction deserialize(
			JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions
		) {
			int i = GsonHelper.getAsInt(jsonObject, "limit", 0);
			return new LootingEnchantFunction(lootItemConditions, GsonHelper.getAsObject(jsonObject, "count", jsonDeserializationContext, RandomValueBounds.class), i);
		}
	}
}
