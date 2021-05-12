package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SmeltItemFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogManager.getLogger();

	SmeltItemFunction(LootItemCondition[] lootItemConditions) {
		super(lootItemConditions);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.FURNACE_SMELT;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isEmpty()) {
			return itemStack;
		} else {
			Optional<SmeltingRecipe> optional = lootContext.getLevel()
				.getRecipeManager()
				.getRecipeFor(RecipeType.SMELTING, new SimpleContainer(itemStack), lootContext.getLevel());
			if (optional.isPresent()) {
				ItemStack itemStack2 = ((SmeltingRecipe)optional.get()).getResultItem();
				if (!itemStack2.isEmpty()) {
					ItemStack itemStack3 = itemStack2.copy();
					itemStack3.setCount(itemStack.getCount());
					return itemStack3;
				}
			}

			LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", itemStack);
			return itemStack;
		}
	}

	public static LootItemConditionalFunction.Builder<?> smelted() {
		return simpleBuilder(SmeltItemFunction::new);
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SmeltItemFunction> {
		public SmeltItemFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			return new SmeltItemFunction(lootItemConditions);
		}
	}
}
