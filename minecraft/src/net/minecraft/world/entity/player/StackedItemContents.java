package net.minecraft.world.entity.player;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class StackedItemContents {
	private final StackedContents<Holder<Item>> raw = new StackedContents<>();

	public void accountSimpleStack(ItemStack itemStack) {
		if (Inventory.isUsableForCrafting(itemStack)) {
			this.accountStack(itemStack);
		}
	}

	public void accountStack(ItemStack itemStack) {
		this.accountStack(itemStack, itemStack.getMaxStackSize());
	}

	public void accountStack(ItemStack itemStack, int i) {
		if (!itemStack.isEmpty()) {
			int j = Math.min(i, itemStack.getCount());
			this.raw.account(itemStack.getItemHolder(), j);
		}
	}

	public static StackedContents.IngredientInfo<Holder<Item>> convertIngredientContents(Stream<Holder<Item>> stream) {
		List<Holder<Item>> list = stream.sorted(Comparator.comparingInt(holder -> BuiltInRegistries.ITEM.getId((Item)holder.value()))).toList();
		return new StackedContents.IngredientInfo<>(list);
	}

	public boolean canCraft(Recipe<?> recipe, @Nullable StackedContents.Output<Holder<Item>> output) {
		return this.canCraft(recipe, 1, output);
	}

	public boolean canCraft(Recipe<?> recipe, int i, @Nullable StackedContents.Output<Holder<Item>> output) {
		return this.raw.tryPick(recipe.placementInfo().stackedRecipeContents(), i, output);
	}

	public int getBiggestCraftableStack(Recipe<?> recipe, @Nullable StackedContents.Output<Holder<Item>> output) {
		return this.getBiggestCraftableStack(recipe, Integer.MAX_VALUE, output);
	}

	public int getBiggestCraftableStack(Recipe<?> recipe, int i, @Nullable StackedContents.Output<Holder<Item>> output) {
		return this.raw.tryPickAll(recipe.placementInfo().stackedRecipeContents(), i, output);
	}

	public void clear() {
		this.raw.clear();
	}
}
