package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SmeltItemFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<SmeltItemFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).apply(instance, SmeltItemFunction::new));

	private SmeltItemFunction(List<LootItemCondition> list) {
		super(list);
	}

	@Override
	public LootItemFunctionType<SmeltItemFunction> getType() {
		return LootItemFunctions.FURNACE_SMELT;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isEmpty()) {
			return itemStack;
		} else {
			SingleRecipeInput singleRecipeInput = new SingleRecipeInput(itemStack);
			Optional<RecipeHolder<SmeltingRecipe>> optional = lootContext.getLevel()
				.recipeAccess()
				.getRecipeFor(RecipeType.SMELTING, singleRecipeInput, lootContext.getLevel());
			if (optional.isPresent()) {
				ItemStack itemStack2 = ((SmeltingRecipe)((RecipeHolder)optional.get()).value()).assemble(singleRecipeInput, lootContext.getLevel().registryAccess());
				if (!itemStack2.isEmpty()) {
					return itemStack2.copyWithCount(itemStack.getCount());
				}
			}

			LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", itemStack);
			return itemStack;
		}
	}

	public static LootItemConditionalFunction.Builder<?> smelted() {
		return simpleBuilder(SmeltItemFunction::new);
	}
}
