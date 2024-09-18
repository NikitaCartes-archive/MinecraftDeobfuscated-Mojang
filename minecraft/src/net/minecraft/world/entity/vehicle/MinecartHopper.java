package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class MinecartHopper extends AbstractMinecartContainer implements Hopper {
	private boolean enabled = true;
	private boolean consumedItemThisFrame = false;

	public MinecartHopper(EntityType<? extends MinecartHopper> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public BlockState getDefaultDisplayBlockState() {
		return Blocks.HOPPER.defaultBlockState();
	}

	@Override
	public int getDefaultDisplayOffset() {
		return 1;
	}

	@Override
	public int getContainerSize() {
		return 5;
	}

	@Override
	public void activateMinecart(int i, int j, int k, boolean bl) {
		boolean bl2 = !bl;
		if (bl2 != this.isEnabled()) {
			this.setEnabled(bl2);
		}
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean bl) {
		this.enabled = bl;
	}

	@Override
	public double getLevelX() {
		return this.getX();
	}

	@Override
	public double getLevelY() {
		return this.getY() + 0.5;
	}

	@Override
	public double getLevelZ() {
		return this.getZ();
	}

	@Override
	public boolean isGridAligned() {
		return false;
	}

	@Override
	public void tick() {
		this.consumedItemThisFrame = false;
		super.tick();
		this.tryConsumeItems();
	}

	@Override
	protected double makeStepAlongTrack(BlockPos blockPos, RailShape railShape, double d) {
		double e = super.makeStepAlongTrack(blockPos, railShape, d);
		this.tryConsumeItems();
		return e;
	}

	private void tryConsumeItems() {
		if (!this.level().isClientSide && this.isAlive() && this.isEnabled() && !this.consumedItemThisFrame && this.suckInItems()) {
			this.consumedItemThisFrame = true;
			this.setChanged();
		}
	}

	public boolean suckInItems() {
		if (HopperBlockEntity.suckInItems(this.level(), this)) {
			return true;
		} else {
			for (ItemEntity itemEntity : this.level()
				.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.25, 0.0, 0.25), EntitySelector.ENTITY_STILL_ALIVE)) {
				if (HopperBlockEntity.addItem(this, itemEntity)) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	protected Item getDropItem() {
		return Items.HOPPER_MINECART;
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(Items.HOPPER_MINECART);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("Enabled", this.enabled);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.enabled = compoundTag.contains("Enabled") ? compoundTag.getBoolean("Enabled") : true;
	}

	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new HopperMenu(i, inventory, this);
	}
}
