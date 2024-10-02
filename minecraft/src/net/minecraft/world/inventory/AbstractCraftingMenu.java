package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class AbstractCraftingMenu extends RecipeBookMenu {
	private final int width;
	private final int height;
	protected final CraftingContainer craftSlots;
	protected final ResultContainer resultSlots = new ResultContainer();

	public AbstractCraftingMenu(MenuType<?> menuType, int i, int j, int k) {
		super(menuType, i);
		this.width = j;
		this.height = k;
		this.craftSlots = new TransientCraftingContainer(this, j, k);
	}

	protected Slot addResultSlot(Player player, int i, int j) {
		return this.addSlot(new ResultSlot(player, this.craftSlots, this.resultSlots, 0, i, j));
	}

	protected void addCraftingGridSlots(int i, int j) {
		for (int k = 0; k < this.width; k++) {
			for (int l = 0; l < this.height; l++) {
				this.addSlot(new Slot(this.craftSlots, l + k * this.width, i + l * 18, j + k * 18));
			}
		}
	}

	@Override
	public RecipeBookMenu.PostPlaceAction handlePlacement(boolean bl, boolean bl2, RecipeHolder<?> recipeHolder, ServerLevel serverLevel, Inventory inventory) {
		RecipeHolder<CraftingRecipe> recipeHolder2 = (RecipeHolder<CraftingRecipe>)recipeHolder;
		this.beginPlacingRecipe();

		RecipeBookMenu.PostPlaceAction var8;
		try {
			List<Slot> list = this.getInputGridSlots();
			var8 = ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<CraftingRecipe>() {
				@Override
				public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
					AbstractCraftingMenu.this.fillCraftSlotsStackedContents(stackedItemContents);
				}

				@Override
				public void clearCraftingContent() {
					AbstractCraftingMenu.this.resultSlots.clearContent();
					AbstractCraftingMenu.this.craftSlots.clearContent();
				}

				@Override
				public boolean recipeMatches(RecipeHolder<CraftingRecipe> recipeHolder) {
					return recipeHolder.value().matches(AbstractCraftingMenu.this.craftSlots.asCraftInput(), AbstractCraftingMenu.this.owner().level());
				}
			}, this.width, this.height, list, list, inventory, recipeHolder2, bl, bl2);
		} finally {
			this.finishPlacingRecipe(serverLevel, (RecipeHolder<CraftingRecipe>)recipeHolder);
		}

		return var8;
	}

	protected void beginPlacingRecipe() {
	}

	protected void finishPlacingRecipe(ServerLevel serverLevel, RecipeHolder<CraftingRecipe> recipeHolder) {
	}

	public abstract Slot getResultSlot();

	public abstract List<Slot> getInputGridSlots();

	public int getGridWidth() {
		return this.width;
	}

	public int getGridHeight() {
		return this.height;
	}

	protected abstract Player owner();

	@Override
	public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
		this.craftSlots.fillStackedContents(stackedItemContents);
	}
}
