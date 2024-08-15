package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlacementInfo {
	public static final PlacementInfo NOT_PLACEABLE = new PlacementInfo(List.of(), List.of());
	private final List<StackedContents.IngredientInfo<Holder<Item>>> stackedIngredients;
	private final List<Optional<PlacementInfo.SlotInfo>> slotInfo;

	private PlacementInfo(List<StackedContents.IngredientInfo<Holder<Item>>> list, List<Optional<PlacementInfo.SlotInfo>> list2) {
		this.stackedIngredients = list;
		this.slotInfo = list2;
	}

	private static StackedContents.IngredientInfo<Holder<Item>> createStackedContents(List<ItemStack> list) {
		return StackedItemContents.convertIngredientContents(list.stream().map(ItemStack::getItemHolder));
	}

	private static List<ItemStack> createPossibleItems(Ingredient ingredient) {
		return ingredient.items().stream().map(ItemStack::new).toList();
	}

	public static PlacementInfo create(Ingredient ingredient) {
		List<ItemStack> list = createPossibleItems(ingredient);
		if (list.isEmpty()) {
			return NOT_PLACEABLE;
		} else {
			StackedContents.IngredientInfo<Holder<Item>> ingredientInfo = createStackedContents(list);
			PlacementInfo.SlotInfo slotInfo = new PlacementInfo.SlotInfo(list, 0);
			return new PlacementInfo(List.of(ingredientInfo), List.of(Optional.of(slotInfo)));
		}
	}

	public static PlacementInfo createFromOptionals(List<Optional<Ingredient>> list) {
		int i = list.size();
		List<StackedContents.IngredientInfo<Holder<Item>>> list2 = new ArrayList(i);
		List<Optional<PlacementInfo.SlotInfo>> list3 = new ArrayList(i);
		int j = 0;

		for (Optional<Ingredient> optional : list) {
			if (optional.isPresent()) {
				List<ItemStack> list4 = createPossibleItems((Ingredient)optional.get());
				if (list4.isEmpty()) {
					return NOT_PLACEABLE;
				}

				list2.add(createStackedContents(list4));
				list3.add(Optional.of(new PlacementInfo.SlotInfo(list4, j++)));
			} else {
				list3.add(Optional.empty());
			}
		}

		return new PlacementInfo(list2, list3);
	}

	public static PlacementInfo create(List<Ingredient> list) {
		int i = list.size();
		List<StackedContents.IngredientInfo<Holder<Item>>> list2 = new ArrayList(i);
		List<Optional<PlacementInfo.SlotInfo>> list3 = new ArrayList(i);

		for (int j = 0; j < i; j++) {
			Ingredient ingredient = (Ingredient)list.get(j);
			List<ItemStack> list4 = createPossibleItems(ingredient);
			if (list4.isEmpty()) {
				return NOT_PLACEABLE;
			}

			list2.add(createStackedContents(list4));
			list3.add(Optional.of(new PlacementInfo.SlotInfo(list4, j)));
		}

		return new PlacementInfo(list2, list3);
	}

	public List<Optional<PlacementInfo.SlotInfo>> slotInfo() {
		return this.slotInfo;
	}

	public List<StackedContents.IngredientInfo<Holder<Item>>> stackedRecipeContents() {
		return this.stackedIngredients;
	}

	public boolean isImpossibleToPlace() {
		return this.slotInfo.isEmpty();
	}

	public static record SlotInfo(List<ItemStack> possibleItems, int placerOutputPosition) {
		public SlotInfo(List<ItemStack> possibleItems, int placerOutputPosition) {
			if (possibleItems.isEmpty()) {
				throw new IllegalArgumentException("Possible items list must be not empty");
			} else {
				this.possibleItems = possibleItems;
				this.placerOutputPosition = placerOutputPosition;
			}
		}
	}
}
