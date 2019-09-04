package net.minecraft.world.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StonecutterMenu extends AbstractContainerMenu {
	private final ContainerLevelAccess access;
	private final DataSlot selectedRecipeIndex = DataSlot.standalone();
	private final Level level;
	private List<StonecutterRecipe> recipes = Lists.<StonecutterRecipe>newArrayList();
	private ItemStack input = ItemStack.EMPTY;
	private long lastSoundTime;
	final Slot inputSlot;
	final Slot resultSlot;
	private Runnable slotUpdateListener = () -> {
	};
	public final Container container = new SimpleContainer(1) {
		@Override
		public void setChanged() {
			super.setChanged();
			StonecutterMenu.this.slotsChanged(this);
			StonecutterMenu.this.slotUpdateListener.run();
		}
	};
	private final ResultContainer resultContainer = new ResultContainer();

	public StonecutterMenu(int i, Inventory inventory) {
		this(i, inventory, ContainerLevelAccess.NULL);
	}

	public StonecutterMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.STONECUTTER, i);
		this.access = containerLevelAccess;
		this.level = inventory.player.level;
		this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
		this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return false;
			}

			@Override
			public ItemStack onTake(Player player, ItemStack itemStack) {
				ItemStack itemStack2 = StonecutterMenu.this.inputSlot.remove(1);
				if (!itemStack2.isEmpty()) {
					StonecutterMenu.this.setupResultSlot();
				}

				itemStack.getItem().onCraftedBy(itemStack, player.level, player);
				containerLevelAccess.execute((level, blockPos) -> {
					long l = level.getGameTime();
					if (StonecutterMenu.this.lastSoundTime != l) {
						level.playSound(null, blockPos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
						StonecutterMenu.this.lastSoundTime = l;
					}
				});
				return super.onTake(player, itemStack);
			}
		});

		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
			}
		}

		for (int j = 0; j < 9; j++) {
			this.addSlot(new Slot(inventory, j, 8 + j * 18, 142));
		}

		this.addDataSlot(this.selectedRecipeIndex);
	}

	@Environment(EnvType.CLIENT)
	public int getSelectedRecipeIndex() {
		return this.selectedRecipeIndex.get();
	}

	@Environment(EnvType.CLIENT)
	public List<StonecutterRecipe> getRecipes() {
		return this.recipes;
	}

	@Environment(EnvType.CLIENT)
	public int getNumRecipes() {
		return this.recipes.size();
	}

	@Environment(EnvType.CLIENT)
	public boolean hasInputItem() {
		return this.inputSlot.hasItem() && !this.recipes.isEmpty();
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, Blocks.STONECUTTER);
	}

	@Override
	public boolean clickMenuButton(Player player, int i) {
		if (i >= 0 && i < this.recipes.size()) {
			this.selectedRecipeIndex.set(i);
			this.setupResultSlot();
		}

		return true;
	}

	@Override
	public void slotsChanged(Container container) {
		ItemStack itemStack = this.inputSlot.getItem();
		if (itemStack.getItem() != this.input.getItem()) {
			this.input = itemStack.copy();
			this.setupRecipeList(container, itemStack);
		}
	}

	private void setupRecipeList(Container container, ItemStack itemStack) {
		this.recipes.clear();
		this.selectedRecipeIndex.set(-1);
		this.resultSlot.set(ItemStack.EMPTY);
		if (!itemStack.isEmpty()) {
			this.recipes = this.level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, container, this.level);
		}
	}

	private void setupResultSlot() {
		if (!this.recipes.isEmpty()) {
			StonecutterRecipe stonecutterRecipe = (StonecutterRecipe)this.recipes.get(this.selectedRecipeIndex.get());
			this.resultSlot.set(stonecutterRecipe.assemble(this.container));
		} else {
			this.resultSlot.set(ItemStack.EMPTY);
		}

		this.broadcastChanges();
	}

	@Override
	public MenuType<?> getType() {
		return MenuType.STONECUTTER;
	}

	@Environment(EnvType.CLIENT)
	public void registerUpdateListener(Runnable runnable) {
		this.slotUpdateListener = runnable;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
		return slot.container != this.resultContainer && super.canTakeItemForPickAll(itemStack, slot);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = (Slot)this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			Item item = itemStack2.getItem();
			itemStack = itemStack2.copy();
			if (i == 1) {
				item.onCraftedBy(itemStack2, player.level, player);
				if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			} else if (i == 0) {
				if (!this.moveItemStackTo(itemStack2, 2, 38, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, new SimpleContainer(itemStack2), this.level).isPresent()) {
				if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (i >= 2 && i < 29) {
				if (!this.moveItemStackTo(itemStack2, 29, 38, false)) {
					return ItemStack.EMPTY;
				}
			} else if (i >= 29 && i < 38 && !this.moveItemStackTo(itemStack2, 2, 29, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			}

			slot.setChanged();
			if (itemStack2.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemStack2);
			this.broadcastChanges();
		}

		return itemStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.resultContainer.removeItemNoUpdate(1);
		this.access.execute((level, blockPos) -> this.clearContainer(player, player.level, this.container));
	}
}
