package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;

public class PlacementInfo {
	public static final PlacementInfo NOT_PLACEABLE = new PlacementInfo(List.of(), List.of(), List.of());
	private final List<Ingredient> ingredients;
	private final List<StackedContents.IngredientInfo<Holder<Item>>> unpackedIngredients;
	private final List<Optional<PlacementInfo.SlotInfo>> slotInfo;

	private PlacementInfo(List<Ingredient> list, List<StackedContents.IngredientInfo<Holder<Item>>> list2, List<Optional<PlacementInfo.SlotInfo>> list3) {
		this.ingredients = list;
		this.unpackedIngredients = list2;
		this.slotInfo = list3;
	}

	public static StackedContents.IngredientInfo<Holder<Item>> ingredientToContents(Ingredient ingredient) {
		return StackedItemContents.convertIngredientContents(ingredient.items().stream());
	}

	public static PlacementInfo create(Ingredient ingredient) {
		if (ingredient.items().isEmpty()) {
			return NOT_PLACEABLE;
		} else {
			StackedContents.IngredientInfo<Holder<Item>> ingredientInfo = ingredientToContents(ingredient);
			PlacementInfo.SlotInfo slotInfo = new PlacementInfo.SlotInfo(0);
			return new PlacementInfo(List.of(ingredient), List.of(ingredientInfo), List.of(Optional.of(slotInfo)));
		}
	}

	public static PlacementInfo createFromOptionals(List<Optional<Ingredient>> list) {
		int i = list.size();
		List<Ingredient> list2 = new ArrayList(i);
		List<StackedContents.IngredientInfo<Holder<Item>>> list3 = new ArrayList(i);
		List<Optional<PlacementInfo.SlotInfo>> list4 = new ArrayList(i);
		int j = 0;

		for (Optional<Ingredient> optional : list) {
			if (optional.isPresent()) {
				Ingredient ingredient = (Ingredient)optional.get();
				if (ingredient.items().isEmpty()) {
					return NOT_PLACEABLE;
				}

				list2.add(ingredient);
				list3.add(ingredientToContents(ingredient));
				list4.add(Optional.of(new PlacementInfo.SlotInfo(j++)));
			} else {
				list4.add(Optional.empty());
			}
		}

		return new PlacementInfo(list2, list3, list4);
	}

	public static PlacementInfo create(List<Ingredient> list) {
		int i = list.size();
		List<StackedContents.IngredientInfo<Holder<Item>>> list2 = new ArrayList(i);
		List<Optional<PlacementInfo.SlotInfo>> list3 = new ArrayList(i);

		for (int j = 0; j < i; j++) {
			Ingredient ingredient = (Ingredient)list.get(j);
			if (ingredient.items().isEmpty()) {
				return NOT_PLACEABLE;
			}

			list2.add(ingredientToContents(ingredient));
			list3.add(Optional.of(new PlacementInfo.SlotInfo(j)));
		}

		return new PlacementInfo(list, list2, list3);
	}

	public List<Optional<PlacementInfo.SlotInfo>> slotInfo() {
		return this.slotInfo;
	}

	public List<Ingredient> ingredients() {
		return this.ingredients;
	}

	public List<StackedContents.IngredientInfo<Holder<Item>>> unpackedIngredients() {
		return this.unpackedIngredients;
	}

	public boolean isImpossibleToPlace() {
		return this.slotInfo.isEmpty();
	}

	public static record SlotInfo(int placerOutputPosition) {
	}
}
