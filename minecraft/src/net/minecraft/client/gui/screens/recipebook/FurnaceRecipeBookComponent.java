package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.FuelValues;

@Environment(EnvType.CLIENT)
public class FurnaceRecipeBookComponent extends RecipeBookComponent<AbstractFurnaceMenu> {
	private static final WidgetSprites FILTER_SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_enabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_disabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_enabled_highlighted"),
		ResourceLocation.withDefaultNamespace("recipe_book/furnace_filter_disabled_highlighted")
	);
	private final Component recipeFilterName;
	@Nullable
	private List<ItemStack> fuels;

	public FurnaceRecipeBookComponent(AbstractFurnaceMenu abstractFurnaceMenu, Component component) {
		super(abstractFurnaceMenu);
		this.recipeFilterName = component;
	}

	@Override
	protected void initFilterButtonTextures() {
		this.filterButton.initTextureValues(FILTER_SPRITES);
	}

	@Override
	protected boolean isCraftingSlot(Slot slot) {
		return switch (slot.index) {
			case 0, 1, 2 -> true;
			default -> false;
		};
	}

	@Override
	protected void setupGhostRecipeSlots(GhostSlots ghostSlots, RecipeHolder<?> recipeHolder) {
		ClientLevel clientLevel = this.minecraft.level;
		ItemStack itemStack = recipeHolder.value().getResultItem(clientLevel.registryAccess());
		Slot slot = this.menu.getResultSlot();
		ghostSlots.addResult(itemStack, slot);
		List<Optional<PlacementInfo.SlotInfo>> list = recipeHolder.value().placementInfo().slotInfo();
		if (!list.isEmpty()) {
			((Optional)list.getFirst()).ifPresent(slotInfo -> {
				Slot slotx = this.menu.slots.get(0);
				ghostSlots.addIngredient(slotInfo.possibleItems(), slotx);
			});
		}

		Slot slot2 = this.menu.slots.get(1);
		if (slot2.getItem().isEmpty()) {
			if (list.size() > 1) {
				((Optional)list.get(1)).ifPresent(slotInfo -> ghostSlots.addIngredient(slotInfo.possibleItems(), slot2));
			} else {
				if (this.fuels == null) {
					this.fuels = this.getFuelItems(clientLevel.fuelValues()).stream().map(ItemStack::new).toList();
				}

				ghostSlots.addIngredient(this.fuels, slot2);
			}
		}
	}

	private SequencedSet<Item> getFuelItems(FuelValues fuelValues) {
		return fuelValues.fuelItems();
	}

	@Override
	protected Component getRecipeFilterName() {
		return this.recipeFilterName;
	}

	@Override
	protected void selectMatchingRecipes(RecipeCollection recipeCollection, StackedItemContents stackedItemContents, RecipeBook recipeBook) {
		recipeCollection.selectMatchingRecipes(stackedItemContents, 1, 1, recipeBook);
	}
}
