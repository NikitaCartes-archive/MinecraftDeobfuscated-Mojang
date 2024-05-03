package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class InventoryMenu extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
	public static final int CONTAINER_ID = 0;
	public static final int RESULT_SLOT = 0;
	public static final int CRAFT_SLOT_START = 1;
	public static final int CRAFT_SLOT_COUNT = 4;
	public static final int CRAFT_SLOT_END = 5;
	public static final int ARMOR_SLOT_START = 5;
	public static final int ARMOR_SLOT_COUNT = 4;
	public static final int ARMOR_SLOT_END = 9;
	public static final int INV_SLOT_START = 9;
	public static final int INV_SLOT_END = 36;
	public static final int USE_ROW_SLOT_START = 36;
	public static final int USE_ROW_SLOT_END = 45;
	public static final int SHIELD_SLOT = 45;
	public static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
	public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
	public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
	public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
	public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
	public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");
	private static final Map<EquipmentSlot, ResourceLocation> TEXTURE_EMPTY_SLOTS = Map.of(
		EquipmentSlot.FEET,
		EMPTY_ARMOR_SLOT_BOOTS,
		EquipmentSlot.LEGS,
		EMPTY_ARMOR_SLOT_LEGGINGS,
		EquipmentSlot.CHEST,
		EMPTY_ARMOR_SLOT_CHESTPLATE,
		EquipmentSlot.HEAD,
		EMPTY_ARMOR_SLOT_HELMET
	);
	private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 2, 2);
	private final ResultContainer resultSlots = new ResultContainer();
	public final boolean active;
	private final Player owner;

	public InventoryMenu(Inventory inventory, boolean bl, Player player) {
		super(null, 0);
		this.active = bl;
		this.owner = player;
		this.addSlot(new ResultSlot(inventory.player, this.craftSlots, this.resultSlots, 0, 154, 28));

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				this.addSlot(new Slot(this.craftSlots, j + i * 2, 98 + j * 18, 18 + i * 18));
			}
		}

		for (int i = 0; i < 4; i++) {
			EquipmentSlot equipmentSlot = SLOT_IDS[i];
			ResourceLocation resourceLocation = (ResourceLocation)TEXTURE_EMPTY_SLOTS.get(equipmentSlot);
			this.addSlot(new ArmorSlot(inventory, player, equipmentSlot, 39 - i, 8, 8 + i * 18, resourceLocation));
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inventory, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
		}

		this.addSlot(new Slot(inventory, 40, 77, 62) {
			@Override
			public void setByPlayer(ItemStack itemStack, ItemStack itemStack2) {
				player.onEquipItem(EquipmentSlot.OFFHAND, itemStack2, itemStack);
				super.setByPlayer(itemStack, itemStack2);
			}

			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			}
		});
	}

	public static boolean isHotbarSlot(int i) {
		return i >= 36 && i < 45 || i == 45;
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedContents stackedContents) {
		this.craftSlots.fillStackedContents(stackedContents);
	}

	@Override
	public void clearCraftingContent() {
		this.resultSlots.clearContent();
		this.craftSlots.clearContent();
	}

	@Override
	public boolean recipeMatches(RecipeHolder<CraftingRecipe> recipeHolder) {
		return recipeHolder.value().matches(this.craftSlots.asCraftInput(), this.owner.level());
	}

	@Override
	public void slotsChanged(Container container) {
		CraftingMenu.slotChangedCraftingGrid(this, this.owner.level(), this.owner, this.craftSlots, this.resultSlots, null);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.resultSlots.clearContent();
		if (!player.level().isClientSide) {
			this.clearContainer(player, this.craftSlots);
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
			if (i == 0) {
				if (!this.moveItemStackTo(itemStack2, 9, 45, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			} else if (i >= 1 && i < 5) {
				if (!this.moveItemStackTo(itemStack2, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (i >= 5 && i < 9) {
				if (!this.moveItemStackTo(itemStack2, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(8 - equipmentSlot.getIndex()).hasItem()) {
				int j = 8 - equipmentSlot.getIndex();
				if (!this.moveItemStackTo(itemStack2, j, j + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentSlot == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem()) {
				if (!this.moveItemStackTo(itemStack2, 45, 46, false)) {
					return ItemStack.EMPTY;
				}
			} else if (i >= 9 && i < 36) {
				if (!this.moveItemStackTo(itemStack2, 36, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (i >= 36 && i < 45) {
				if (!this.moveItemStackTo(itemStack2, 9, 36, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 9, 45, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY, itemStack);
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
	public int getResultSlotIndex() {
		return 0;
	}

	@Override
	public int getGridWidth() {
		return this.craftSlots.getWidth();
	}

	@Override
	public int getGridHeight() {
		return this.craftSlots.getHeight();
	}

	@Override
	public int getSize() {
		return 5;
	}

	public CraftingContainer getCraftSlots() {
		return this.craftSlots;
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return RecipeBookType.CRAFTING;
	}

	@Override
	public boolean shouldMoveToInventory(int i) {
		return i != this.getResultSlotIndex();
	}
}
