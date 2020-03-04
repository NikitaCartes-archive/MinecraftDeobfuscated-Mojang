package net.minecraft.world.inventory;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SmithingMenu extends ItemCombinerMenu {
	private static final Map<Item, Item> DIAMOND_TO_NETHERITE = ImmutableMap.<Item, Item>builder()
		.put(Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE)
		.put(Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS)
		.put(Items.DIAMOND_HELMET, Items.NETHERITE_HELMET)
		.put(Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS)
		.put(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD)
		.put(Items.DIAMOND_AXE, Items.NETHERITE_AXE)
		.put(Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE)
		.put(Items.DIAMOND_HOE, Items.NETHERITE_HOE)
		.put(Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL)
		.build();

	public SmithingMenu(int i, Inventory inventory) {
		this(i, inventory, ContainerLevelAccess.NULL);
	}

	public SmithingMenu(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(MenuType.SMITHING, i, inventory, containerLevelAccess);
	}

	@Override
	protected boolean isValidBlock(BlockState blockState) {
		return blockState.getBlock() == Blocks.SMITHING_TABLE;
	}

	@Override
	protected boolean mayPickup(Player player, boolean bl) {
		return DIAMOND_TO_NETHERITE.containsKey(this.inputSlots.getItem(0).getItem()) && this.inputSlots.getItem(1).getItem() == Items.NETHERITE_INGOT;
	}

	@Override
	protected ItemStack onTake(Player player, ItemStack itemStack) {
		this.inputSlots.setItem(0, ItemStack.EMPTY);
		ItemStack itemStack2 = this.inputSlots.getItem(1);
		itemStack2.shrink(1);
		this.inputSlots.setItem(1, itemStack2);
		this.access.execute((level, blockPos) -> level.levelEvent(1044, blockPos, 0));
		return itemStack;
	}

	@Override
	public void createResult() {
		ItemStack itemStack = this.inputSlots.getItem(0);
		ItemStack itemStack2 = this.inputSlots.getItem(1);
		Item item = (Item)DIAMOND_TO_NETHERITE.get(itemStack.getItem());
		if (itemStack2.getItem() == Items.NETHERITE_INGOT && item != null) {
			ItemStack itemStack3 = new ItemStack(item);
			CompoundTag compoundTag = itemStack.getTag();
			itemStack3.setTag(compoundTag != null ? compoundTag.copy() : null);
			this.resultSlots.setItem(0, itemStack3);
		} else {
			this.resultSlots.setItem(0, ItemStack.EMPTY);
		}
	}
}
