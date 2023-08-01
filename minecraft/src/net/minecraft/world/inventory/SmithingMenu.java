package net.minecraft.world.inventory;

import java.util.List;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SmithingMenu extends ItemCombinerMenu {
	public static final int TEMPLATE_SLOT = 0;
	public static final int BASE_SLOT = 1;
	public static final int ADDITIONAL_SLOT = 2;
	public static final int RESULT_SLOT = 3;
	public static final int TEMPLATE_SLOT_X_PLACEMENT = 8;
	public static final int BASE_SLOT_X_PLACEMENT = 26;
	public static final int ADDITIONAL_SLOT_X_PLACEMENT = 44;
	private static final int RESULT_SLOT_X_PLACEMENT = 98;
	public static final int SLOT_Y_PLACEMENT = 48;
	private final Level level;
	@Nullable
	private SmithingRecipe selectedRecipe;
	private final List<SmithingRecipe> recipes;

	public SmithingMenu(int i, Inventory inventory) {
		this(i, inventory, ContainerLevelAccess.NULL);
	}

	public SmithingMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.SMITHING, i, inventory, containerLevelAccess);
		this.level = inventory.player.level();
		this.recipes = this.level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
	}

	@Override
	protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
		return ItemCombinerMenuSlotDefinition.create()
			.withSlot(0, 8, 48, itemStack -> this.recipes.stream().anyMatch(smithingRecipe -> smithingRecipe.isTemplateIngredient(itemStack)))
			.withSlot(1, 26, 48, itemStack -> this.recipes.stream().anyMatch(smithingRecipe -> smithingRecipe.isBaseIngredient(itemStack)))
			.withSlot(2, 44, 48, itemStack -> this.recipes.stream().anyMatch(smithingRecipe -> smithingRecipe.isAdditionIngredient(itemStack)))
			.withResultSlot(3, 98, 48)
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
		itemStack.onCraftedBy(player.level(), player, itemStack.getCount());
		this.resultSlots.awardUsedRecipes(player, this.getRelevantItems());
		this.shrinkStackInSlot(0);
		this.shrinkStackInSlot(1);
		this.shrinkStackInSlot(2);
		this.access.execute((level, blockPos) -> level.levelEvent(1044, blockPos, 0));
	}

	private List<ItemStack> getRelevantItems() {
		return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
	}

	private void shrinkStackInSlot(int i) {
		ItemStack itemStack = this.inputSlots.getItem(i);
		if (!itemStack.isEmpty()) {
			itemStack.shrink(1);
			this.inputSlots.setItem(i, itemStack);
		}
	}

	@Override
	public void createResult() {
		List<SmithingRecipe> list = this.level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, this.inputSlots, this.level);
		if (list.isEmpty()) {
			this.resultSlots.setItem(0, ItemStack.EMPTY);
		} else {
			SmithingRecipe smithingRecipe = (SmithingRecipe)list.get(0);
			ItemStack itemStack = smithingRecipe.assemble(this.inputSlots, this.level.registryAccess());
			if (itemStack.isItemEnabled(this.level.enabledFeatures())) {
				this.selectedRecipe = smithingRecipe;
				this.resultSlots.setRecipeUsed(smithingRecipe);
				this.resultSlots.setItem(0, itemStack);
			}
		}
	}

	@Override
	public int getSlotToQuickMoveTo(ItemStack itemStack) {
		return this.findSlotToQuickMoveTo(itemStack).orElse(0);
	}

	private static OptionalInt findSlotMatchingIngredient(SmithingRecipe smithingRecipe, ItemStack itemStack) {
		if (smithingRecipe.isTemplateIngredient(itemStack)) {
			return OptionalInt.of(0);
		} else if (smithingRecipe.isBaseIngredient(itemStack)) {
			return OptionalInt.of(1);
		} else {
			return smithingRecipe.isAdditionIngredient(itemStack) ? OptionalInt.of(2) : OptionalInt.empty();
		}
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
		return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
	}

	@Override
	public boolean canMoveIntoInputSlots(ItemStack itemStack) {
		return this.findSlotToQuickMoveTo(itemStack).isPresent();
	}

	private OptionalInt findSlotToQuickMoveTo(ItemStack itemStack) {
		return this.recipes
			.stream()
			.flatMapToInt(smithingRecipe -> findSlotMatchingIngredient(smithingRecipe, itemStack).stream())
			.filter(i -> !this.getSlot(i).hasItem())
			.findFirst();
	}
}
