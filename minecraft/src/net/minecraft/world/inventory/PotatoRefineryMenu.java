package net.minecraft.world.inventory;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.PotatoRefinementRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.PotatoRefineryBlockEntity;
import net.minecraft.world.phys.Vec3;

public class PotatoRefineryMenu extends RecipeBookMenu<Container> {
	public static final int INGREDIENT_SLOT = 0;
	public static final int FUEL_SLOT = 1;
	public static final int BOTTLE_SLOT = 2;
	public static final int RESULT_SLOT = 3;
	public static final int SLOT_COUNT = 4;
	public static final int DATA_COUNT = 4;
	private static final int INV_SLOT_START = 4;
	private static final int INV_SLOT_END = 31;
	private static final int USE_ROW_SLOT_START = 31;
	private static final int USE_ROW_SLOT_END = 40;
	private final Container container;
	private final ContainerData data;
	protected final Level level;
	private final RecipeType<PotatoRefinementRecipe> recipeType;
	private final RecipeBookType recipeBookType;

	public PotatoRefineryMenu(int i, Inventory inventory) {
		this(i, inventory, new SimpleContainer(4), new SimpleContainerData(4));
	}

	public PotatoRefineryMenu(int i, Inventory inventory, Container container, ContainerData containerData) {
		this(MenuType.POTATO_REFINERY, RecipeType.POTATO_REFINEMENT, RecipeBookType.FURNACE, i, inventory, container, containerData);
	}

	private PotatoRefineryMenu(
		MenuType<?> menuType,
		RecipeType<PotatoRefinementRecipe> recipeType,
		RecipeBookType recipeBookType,
		int i,
		Inventory inventory,
		Container container,
		ContainerData containerData
	) {
		super(menuType, i);
		this.recipeType = recipeType;
		this.recipeBookType = recipeBookType;
		checkContainerSize(container, 4);
		checkContainerDataCount(containerData, 4);
		this.container = container;
		this.data = containerData;
		this.level = inventory.player.level();
		this.addSlot(new Slot(container, 0, 52, 33));
		this.addSlot(new Slot(container, 2, 107, 36));
		this.addSlot(new PotatoRefineryMenu.FuelSlot(container, 1, 52, 71));
		this.addSlot(new PotatoRefineryMenu.ResultSlot(inventory.player, container, 3, 107, 62));

		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 104 + j * 18));
			}
		}

		for (int j = 0; j < 9; j++) {
			this.addSlot(new Slot(inventory, j, 8 + j * 18, 162));
		}

		this.addDataSlots(containerData);
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedContents stackedContents) {
		if (this.container instanceof StackedContentsCompatible) {
			((StackedContentsCompatible)this.container).fillStackedContents(stackedContents);
		}
	}

	@Override
	public void clearCraftingContent() {
		this.getSlot(0).set(ItemStack.EMPTY);
		this.getSlot(2).set(ItemStack.EMPTY);
		this.getSlot(3).set(ItemStack.EMPTY);
	}

	@Override
	public boolean recipeMatches(RecipeHolder<? extends Recipe<Container>> recipeHolder) {
		return recipeHolder.value().matches(this.container, this.level);
	}

	@Override
	public int getResultSlotIndex() {
		return 3;
	}

	@Override
	public int getGridWidth() {
		return 1;
	}

	@Override
	public int getGridHeight() {
		return 1;
	}

	@Override
	public int getSize() {
		return 3;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i == 3) {
				if (!this.moveItemStackTo(itemStack2, 4, 40, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			} else if (i != 1 && i != 0) {
				if (this.canRefine(itemStack2)) {
					if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if (this.isFuel(itemStack2)) {
					if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
						return ItemStack.EMPTY;
					}
				} else if (i >= 4 && i < 31) {
					if (!this.moveItemStackTo(itemStack2, 31, 40, false)) {
						return ItemStack.EMPTY;
					}
				} else if (i >= 31 && i < 40 && !this.moveItemStackTo(itemStack2, 4, 31, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 4, 40, false)) {
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
		}

		return itemStack;
	}

	protected boolean canRefine(ItemStack itemStack) {
		return this.level.getRecipeManager().getRecipeFor(this.recipeType, new SimpleContainer(itemStack), this.level).isPresent();
	}

	protected boolean isFuel(ItemStack itemStack) {
		return PotatoRefineryBlockEntity.isFuel(itemStack);
	}

	public float getBurnProgress() {
		int i = this.data.get(2);
		int j = this.data.get(3);
		return j != 0 && i != 0 ? Mth.clamp((float)i / (float)j, 0.0F, 1.0F) : 0.0F;
	}

	public float getLitProgress() {
		int i = this.data.get(1);
		if (i == 0) {
			i = 200;
		}

		return Mth.clamp((float)this.data.get(0) / (float)i, 0.0F, 1.0F);
	}

	public boolean isLit() {
		return this.data.get(0) > 0;
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return this.recipeBookType;
	}

	@Override
	public boolean shouldMoveToInventory(int i) {
		return i != 1;
	}

	class FuelSlot extends Slot {
		public FuelSlot(Container container, int i, int j, int k) {
			super(container, i, j, k);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack) {
			return PotatoRefineryMenu.this.isFuel(itemStack) || isBucket(itemStack);
		}

		@Override
		public int getMaxStackSize(ItemStack itemStack) {
			return isBucket(itemStack) ? 1 : super.getMaxStackSize(itemStack);
		}

		public static boolean isBucket(ItemStack itemStack) {
			return itemStack.is(Items.BUCKET);
		}
	}

	static class ResultSlot extends Slot {
		private final Player player;
		private int removeCount;

		public ResultSlot(Player player, Container container, int i, int j, int k) {
			super(container, i, j, k);
			this.player = player;
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
		public void onTake(Player player, ItemStack itemStack) {
			this.checkTakeAchievements(itemStack);
			super.onTake(player, itemStack);
		}

		@Override
		protected void onQuickCraft(ItemStack itemStack, int i) {
			this.removeCount += i;
			this.checkTakeAchievements(itemStack);
		}

		protected void checkTakeAchievements(ItemStack itemStack) {
			itemStack.onCraftedBy(this.player.level(), this.player, this.removeCount);
			this.removeCount = 0;
			if (this.player instanceof ServerPlayer serverPlayer && this.container instanceof PotatoRefineryBlockEntity potatoRefineryBlockEntity) {
				CriteriaTriggers.POTATO_REFINED.trigger(serverPlayer, itemStack);
				float f = potatoRefineryBlockEntity.harvestExperience();
				if (f > 0.0F) {
					createExperience(serverPlayer.serverLevel(), serverPlayer.position(), 1, f);
				}
			}
		}

		private static void createExperience(ServerLevel serverLevel, Vec3 vec3, int i, float f) {
			int j = Mth.floor((float)i * f);
			float g = Mth.frac((float)i * f);
			if (g != 0.0F && Math.random() < (double)g) {
				j++;
			}

			ExperienceOrb.award(serverLevel, vec3, j);
		}
	}
}
