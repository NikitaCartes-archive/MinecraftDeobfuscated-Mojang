package net.minecraft.world.entity.vehicle;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MinecartHopper extends AbstractMinecartContainer implements Hopper {
	private boolean enabled = true;
	private int cooldownTime = -1;
	private final BlockPos lastPosition = BlockPos.ZERO;

	public MinecartHopper(EntityType<? extends MinecartHopper> entityType, Level level) {
		super(entityType, level);
	}

	public MinecartHopper(Level level, double d, double e, double f) {
		super(EntityType.HOPPER_MINECART, d, e, f, level);
	}

	@Override
	public AbstractMinecart.Type getMinecartType() {
		return AbstractMinecart.Type.HOPPER;
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
	public Level getLevel() {
		return this.level;
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
	public void tick() {
		super.tick();
		if (!this.level.isClientSide && this.isAlive() && this.isEnabled()) {
			BlockPos blockPos = this.blockPosition();
			if (blockPos.equals(this.lastPosition)) {
				this.cooldownTime--;
			} else {
				this.setCooldown(0);
			}

			if (!this.isOnCooldown()) {
				this.setCooldown(0);
				if (this.suckInItems()) {
					this.setCooldown(4);
					this.setChanged();
				}
			}
		}
	}

	public boolean suckInItems() {
		if (HopperBlockEntity.suckInItems(this)) {
			return true;
		} else {
			List<ItemEntity> list = this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.25, 0.0, 0.25), EntitySelector.ENTITY_STILL_ALIVE);
			if (!list.isEmpty()) {
				HopperBlockEntity.addItem(this, (ItemEntity)list.get(0));
			}

			return false;
		}
	}

	@Override
	public void destroy(DamageSource damageSource) {
		super.destroy(damageSource);
		if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			this.spawnAtLocation(Blocks.HOPPER);
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("TransferCooldown", this.cooldownTime);
		compoundTag.putBoolean("Enabled", this.enabled);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.cooldownTime = compoundTag.getInt("TransferCooldown");
		this.enabled = compoundTag.contains("Enabled") ? compoundTag.getBoolean("Enabled") : true;
	}

	public void setCooldown(int i) {
		this.cooldownTime = i;
	}

	public boolean isOnCooldown() {
		return this.cooldownTime > 0;
	}

	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new HopperMenu(i, inventory, this);
	}
}
