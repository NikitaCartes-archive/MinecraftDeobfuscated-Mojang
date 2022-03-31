package net.minecraft.world.entity.vehicle;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MinecartChest extends AbstractMinecartContainer {
	public MinecartChest(EntityType<? extends MinecartChest> entityType, Level level) {
		super(entityType, level);
	}

	public MinecartChest(Level level, double d, double e, double f) {
		super(EntityType.CHEST_MINECART, d, e, f, level);
	}

	@Override
	protected Item getDropItem() {
		return Items.CHEST_MINECART;
	}

	@Override
	public int getContainerSize() {
		return 27;
	}

	@Override
	public AbstractMinecart.Type getMinecartType() {
		return AbstractMinecart.Type.CHEST;
	}

	@Override
	public BlockState getDefaultDisplayBlockState() {
		return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
	}

	@Override
	public int getDefaultDisplayOffset() {
		return 8;
	}

	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return ChestMenu.threeRows(i, inventory, this);
	}
}
