package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CraftingMenu extends AbstractCraftingMenu {
	private static final int CRAFTING_GRID_WIDTH = 3;
	private static final int CRAFTING_GRID_HEIGHT = 3;
	public static final int RESULT_SLOT = 0;
	private static final int CRAFT_SLOT_START = 1;
	private static final int CRAFT_SLOT_COUNT = 9;
	private static final int CRAFT_SLOT_END = 10;
	private static final int INV_SLOT_START = 10;
	private static final int INV_SLOT_END = 37;
	private static final int USE_ROW_SLOT_START = 37;
	private static final int USE_ROW_SLOT_END = 46;
	private final ContainerLevelAccess access;
	private final Player player;
	private boolean placingRecipe;

	public CraftingMenu(int i, Inventory inventory) {
		this(i, inventory, ContainerLevelAccess.NULL);
	}

	public CraftingMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.CRAFTING, i, 3, 3);
		this.access = containerLevelAccess;
		this.player = inventory.player;
		this.addResultSlot(this.player, 124, 35);
		this.addCraftingGridSlots(30, 17);
		this.addStandardInventorySlots(inventory, 8, 84);
	}

	protected static void slotChangedCraftingGrid(
		AbstractContainerMenu abstractContainerMenu,
		Level level,
		Player player,
		CraftingContainer craftingContainer,
		ResultContainer resultContainer,
		@Nullable RecipeHolder<CraftingRecipe> recipeHolder
	) {
		if (!level.isClientSide) {
			CraftingInput craftingInput = craftingContainer.asCraftInput();
			ServerPlayer serverPlayer = (ServerPlayer)player;
			ItemStack itemStack = ItemStack.EMPTY;
			Optional<RecipeHolder<CraftingRecipe>> optional = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInput, level, recipeHolder);
			if (optional.isPresent()) {
				RecipeHolder<CraftingRecipe> recipeHolder2 = (RecipeHolder<CraftingRecipe>)optional.get();
				CraftingRecipe craftingRecipe = recipeHolder2.value();
				if (resultContainer.setRecipeUsed(serverPlayer, recipeHolder2)) {
					ItemStack itemStack2 = craftingRecipe.assemble(craftingInput, level.registryAccess());
					if (itemStack2.isItemEnabled(level.enabledFeatures())) {
						itemStack = itemStack2;
					}
				}
			}

			resultContainer.setItem(0, itemStack);
			abstractContainerMenu.setRemoteSlot(0, itemStack);
			serverPlayer.connection
				.send(new ClientboundContainerSetSlotPacket(abstractContainerMenu.containerId, abstractContainerMenu.incrementStateId(), 0, itemStack));
		}
	}

	@Override
	public void slotsChanged(Container container) {
		if (!this.placingRecipe) {
			this.access.execute((level, blockPos) -> slotChangedCraftingGrid(this, level, this.player, this.craftSlots, this.resultSlots, null));
		}
	}

	@Override
	public void beginPlacingRecipe() {
		this.placingRecipe = true;
	}

	@Override
	public void finishPlacingRecipe(RecipeHolder<CraftingRecipe> recipeHolder) {
		this.placingRecipe = false;
		this.access.execute((level, blockPos) -> slotChangedCraftingGrid(this, level, this.player, this.craftSlots, this.resultSlots, recipeHolder));
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.access.execute((level, blockPos) -> this.clearContainer(player, this.craftSlots));
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, Blocks.CRAFTING_TABLE);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i == 0) {
				this.access.execute((level, blockPos) -> itemStack2.getItem().onCraftedBy(itemStack2, level, player));
				if (!this.moveItemStackTo(itemStack2, 10, 46, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			} else if (i >= 10 && i < 46) {
				if (!this.moveItemStackTo(itemStack2, 1, 10, false)) {
					if (i < 37) {
						if (!this.moveItemStackTo(itemStack2, 37, 46, false)) {
							return ItemStack.EMPTY;
						}
					} else if (!this.moveItemStackTo(itemStack2, 10, 37, false)) {
						return ItemStack.EMPTY;
					}
				}
			} else if (!this.moveItemStackTo(itemStack2, 10, 46, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemStack2.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemStack2);
			if (i == 0) {
				player.drop(itemStack2, false);
			}
		}

		return itemStack;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
		return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
	}

	@Override
	public Slot getResultSlot() {
		return this.slots.get(0);
	}

	@Override
	public List<Slot> getInputGridSlots() {
		return this.slots.subList(1, 10);
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return RecipeBookType.CRAFTING;
	}

	@Override
	protected Player owner() {
		return this.player;
	}
}
