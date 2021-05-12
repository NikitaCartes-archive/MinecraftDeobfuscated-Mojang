package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class EnchantWithLevelsFunction extends LootItemConditionalFunction {
	final NumberProvider levels;
	final boolean treasure;

	EnchantWithLevelsFunction(LootItemCondition[] lootItemConditions, NumberProvider numberProvider, boolean bl) {
		super(lootItemConditions);
		this.levels = numberProvider;
		this.treasure = bl;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.ENCHANT_WITH_LEVELS;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.levels.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Random random = lootContext.getRandom();
		return EnchantmentHelper.enchantItem(random, itemStack, this.levels.getInt(lootContext), this.treasure);
	}

	public static EnchantWithLevelsFunction.Builder enchantWithLevels(NumberProvider numberProvider) {
		return new EnchantWithLevelsFunction.Builder(numberProvider);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<EnchantWithLevelsFunction.Builder> {
		private final NumberProvider levels;
		private boolean treasure;

		public Builder(NumberProvider numberProvider) {
			this.levels = numberProvider;
		}

		protected EnchantWithLevelsFunction.Builder getThis() {
			return this;
		}

		public EnchantWithLevelsFunction.Builder allowTreasure() {
			this.treasure = true;
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new EnchantWithLevelsFunction(this.getConditions(), this.levels, this.treasure);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantWithLevelsFunction> {
		public void serialize(JsonObject jsonObject, EnchantWithLevelsFunction enchantWithLevelsFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, enchantWithLevelsFunction, jsonSerializationContext);
			jsonObject.add("levels", jsonSerializationContext.serialize(enchantWithLevelsFunction.levels));
			jsonObject.addProperty("treasure", enchantWithLevelsFunction.treasure);
		}

		public EnchantWithLevelsFunction deserialize(
			JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions
		) {
			NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "levels", jsonDeserializationContext, NumberProvider.class);
			boolean bl = GsonHelper.getAsBoolean(jsonObject, "treasure", false);
			return new EnchantWithLevelsFunction(lootItemConditions, numberProvider, bl);
		}
	}
}
