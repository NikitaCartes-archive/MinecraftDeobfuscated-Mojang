package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ResultSlot extends Slot {
	private final CraftingContainer craftSlots;
	private final Player player;
	private int removeCount;

	public ResultSlot(Player player, CraftingContainer craftingContainer, Container container, int i, int j, int k) {
		super(container, i, j, k);
		this.player = player;
		this.craftSlots = craftingContainer;
	}

	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return false;
	}

	@Override
	public ItemStack remove(int i) {
		if (this.hasItem()) {
			this.removeCount = this.removeCount + Math.min(i, this.getItem().getCount());
		}

		return super.remove(i);
	}

	@Override
	protected void onQuickCraft(ItemStack itemStack, int i) {
		this.removeCount += i;
		this.checkTakeAchievements(itemStack);
	}

	@Override
	protected void onSwapCraft(int i) {
		this.removeCount += i;
	}

	@Override
	protected void checkTakeAchievements(ItemStack itemStack) {
		if (this.removeCount > 0) {
			itemStack.onCraftedBy(this.player.level(), this.player, this.removeCount);
		}

		if (this.container instanceof RecipeCraftingHolder recipeCraftingHolder) {
			recipeCraftingHolder.awardUsedRecipes(this.player, this.craftSlots.getItems());
		}

		this.removeCount = 0;
	}

	private static NonNullList<ItemStack> copyAllInputItems(CraftingInput craftingInput) {
		NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);

		for (int i = 0; i < nonNullList.size(); i++) {
			nonNullList.set(i, craftingInput.getItem(i));
		}

		return nonNullList;
	}

	private NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput, Level level) {
		return level instanceof ServerLevel serverLevel
			? (NonNullList)serverLevel.recipeAccess()
				.getRecipeFor(RecipeType.CRAFTING, craftingInput, serverLevel)
				.map(recipeHolder -> ((CraftingRecipe)recipeHolder.value()).getRemainingItems(craftingInput))
				.orElseGet(() -> copyAllInputItems(craftingInput))
			: CraftingRecipe.defaultCraftingReminder(craftingInput);
	}

	@Override
	public void onTake(Player player, ItemStack itemStack) {
		this.checkTakeAchievements(itemStack);
		CraftingInput.Positioned positioned = this.craftSlots.asPositionedCraftInput();
		CraftingInput craftingInput = positioned.input();
		int i = positioned.left();
		int j = positioned.top();
		NonNullList<ItemStack> nonNullList = this.getRemainingItems(craftingInput, player.level());

		for (int k = 0; k < craftingInput.height(); k++) {
			for (int l = 0; l < craftingInput.width(); l++) {
				int m = l + i + (k + j) * this.craftSlots.getWidth();
				ItemStack itemStack2 = this.craftSlots.getItem(m);
				ItemStack itemStack3 = nonNullList.get(l + k * craftingInput.width());
				if (!itemStack2.isEmpty()) {
					this.craftSlots.removeItem(m, 1);
					itemStack2 = this.craftSlots.getItem(m);
				}

				if (!itemStack3.isEmpty()) {
					if (itemStack2.isEmpty()) {
						this.craftSlots.setItem(m, itemStack3);
					} else if (ItemStack.isSameItemSameComponents(itemStack2, itemStack3)) {
						itemStack3.grow(itemStack2.getCount());
						this.craftSlots.setItem(m, itemStack3);
					} else if (!this.player.getInventory().add(itemStack3)) {
						this.player.drop(itemStack3, false);
					}
				}
			}
		}
	}

	@Override
	public boolean isFake() {
		return true;
	}
}
