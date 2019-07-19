package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomIntGenerators;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EnchantWithLevelsFunction extends LootItemConditionalFunction {
	private final RandomIntGenerator levels;
	private final boolean treasure;

	private EnchantWithLevelsFunction(LootItemCondition[] lootItemConditions, RandomIntGenerator randomIntGenerator, boolean bl) {
		super(lootItemConditions);
		this.levels = randomIntGenerator;
		this.treasure = bl;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Random random = lootContext.getRandom();
		return EnchantmentHelper.enchantItem(random, itemStack, this.levels.getInt(random), this.treasure);
	}

	public static EnchantWithLevelsFunction.Builder enchantWithLevels(RandomIntGenerator randomIntGenerator) {
		return new EnchantWithLevelsFunction.Builder(randomIntGenerator);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<EnchantWithLevelsFunction.Builder> {
		private final RandomIntGenerator levels;
		private boolean treasure;

		public Builder(RandomIntGenerator randomIntGenerator) {
			this.levels = randomIntGenerator;
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
		public Serializer() {
			super(new ResourceLocation("enchant_with_levels"), EnchantWithLevelsFunction.class);
		}

		public void serialize(JsonObject jsonObject, EnchantWithLevelsFunction enchantWithLevelsFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, enchantWithLevelsFunction, jsonSerializationContext);
			jsonObject.add("levels", RandomIntGenerators.serialize(enchantWithLevelsFunction.levels, jsonSerializationContext));
			jsonObject.addProperty("treasure", enchantWithLevelsFunction.treasure);
		}

		public EnchantWithLevelsFunction deserialize(
			JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions
		) {
			RandomIntGenerator randomIntGenerator = RandomIntGenerators.deserialize(jsonObject.get("levels"), jsonDeserializationContext);
			boolean bl = GsonHelper.getAsBoolean(jsonObject, "treasure", false);
			return new EnchantWithLevelsFunction(lootItemConditions, randomIntGenerator, bl);
		}
	}
}
