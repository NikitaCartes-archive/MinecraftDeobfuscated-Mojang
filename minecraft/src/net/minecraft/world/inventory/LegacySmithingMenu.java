package net.minecraft.world.inventory;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.LegacyUpgradeRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated(
	forRemoval = true
)
public class LegacySmithingMenu extends ItemCombinerMenu {
	private final Level level;
	public static final int INPUT_SLOT = 0;
	public static final int ADDITIONAL_SLOT = 1;
	public static final int RESULT_SLOT = 2;
	private static final int INPUT_SLOT_X_PLACEMENT = 27;
	private static final int ADDITIONAL_SLOT_X_PLACEMENT = 76;
	private static final int RESULT_SLOT_X_PLACEMENT = 134;
	private static final int SLOT_Y_PLACEMENT = 47;
	@Nullable
	private LegacyUpgradeRecipe selectedRecipe;
	private final List<LegacyUpgradeRecipe> recipes;

	public LegacySmithingMenu(int i, Inventory inventory) {
		this(i, inventory, ContainerLevelAccess.NULL);
	}

	public LegacySmithingMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.LEGACY_SMITHING, i, inventory, containerLevelAccess);
		this.level = inventory.player.level;
		this.recipes = this.level
			.getRecipeManager()
			.<Container, SmithingRecipe>getAllRecipesFor(RecipeType.SMITHING)
			.stream()
			.filter(smithingRecipe -> smithingRecipe instanceof LegacyUpgradeRecipe)
			.map(smithingRecipe -> (LegacyUpgradeRecipe)smithingRecipe)
			.toList();
	}

	@Override
	protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
		return ItemCombinerMenuSlotDefinition.create()
			.withSlot(0, 27, 47, itemStack -> true)
			.withSlot(1, 76, 47, itemStack -> true)
			.withResultSlot(2, 134, 47)
			.build();
	}

	@Override
	protected boolean isValidBlock(BlockState blockState) {
		return blockState.is(Blocks.SMITHING_TABLE);
	}

	@Override
	protected boolean mayPickup(Player player, boolean bl) {
		return this.selectedRecipe != null && this.selectedRecipe.matches(this.inputSlots, this.level);
	}

	@Override
	protected void onTake(Player player, ItemStack itemStack) {
		itemStack.onCraftedBy(player.level, player, itemStack.getCount());
		this.resultSlots.awardUsedRecipes(player);
		this.shrinkStackInSlot(0);
		this.shrinkStackInSlot(1);
		this.access.execute((level, blockPos) -> level.levelEvent(1044, blockPos, 0));
	}

	private void shrinkStackInSlot(int i) {
		ItemStack itemStack = this.inputSlots.getItem(i);
		itemStack.shrink(1);
		this.inputSlots.setItem(i, itemStack);
	}

	@Override
	public void createResult() {
		List<LegacyUpgradeRecipe> list = this.level
			.getRecipeManager()
			.getRecipesFor(RecipeType.SMITHING, this.inputSlots, this.level)
			.stream()
			.filter(smithingRecipe -> smithingRecipe instanceof LegacyUpgradeRecipe)
			.map(smithingRecipe -> (LegacyUpgradeRecipe)smithingRecipe)
			.toList();
		if (list.isEmpty()) {
			this.resultSlots.setItem(0, ItemStack.EMPTY);
		} else {
			LegacyUpgradeRecipe legacyUpgradeRecipe = (LegacyUpgradeRecipe)list.get(0);
			ItemStack itemStack = legacyUpgradeRecipe.assemble(this.inputSlots, this.level.registryAccess());
			if (itemStack.isItemEnabled(this.level.enabledFeatures())) {
				this.selectedRecipe = legacyUpgradeRecipe;
				this.resultSlots.setRecipeUsed(legacyUpgradeRecipe);
				this.resultSlots.setItem(0, itemStack);
			}
		}
	}

	@Override
	public int getSlotToQuickMoveTo(ItemStack itemStack) {
		return this.shouldQuickMoveToAdditionalSlot(itemStack) ? 1 : 0;
	}

	protected boolean shouldQuickMoveToAdditionalSlot(ItemStack itemStack) {
		return this.recipes.stream().anyMatch(legacyUpgradeRecipe -> legacyUpgradeRecipe.isAdditionIngredient(itemStack));
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
		return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
	}
}
