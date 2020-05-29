package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public abstract class AbstractMinecartContainer extends AbstractMinecart implements Container, MenuProvider {
	private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
	private boolean dropEquipment = true;
	@Nullable
	private ResourceLocation lootTable;
	private long lootTableSeed;

	protected AbstractMinecartContainer(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	protected AbstractMinecartContainer(EntityType<?> entityType, double d, double e, double f, Level level) {
		super(entityType, level, d, e, f);
	}

	@Override
	public void destroy(DamageSource damageSource) {
		super.destroy(damageSource);
		if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			Containers.dropContents(this.level, this, this);
		}
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : this.itemStacks) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getItem(int i) {
		this.unpackLootTable(null);
		return this.itemStacks.get(i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		this.unpackLootTable(null);
		return ContainerHelper.removeItem(this.itemStacks, i, j);
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		this.unpackLootTable(null);
		ItemStack itemStack = this.itemStacks.get(i);
		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			this.itemStacks.set(i, ItemStack.EMPTY);
			return itemStack;
		}
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		this.unpackLootTable(null);
		this.itemStacks.set(i, itemStack);
		if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
			itemStack.setCount(this.getMaxStackSize());
		}
	}

	@Override
	public boolean setSlot(int i, ItemStack itemStack) {
		if (i >= 0 && i < this.getContainerSize()) {
			this.setItem(i, itemStack);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return this.removed ? false : !(player.distanceToSqr(this) > 64.0);
	}

	@Nullable
	@Override
	public Entity changeDimension(ResourceKey<Level> resourceKey) {
		this.dropEquipment = false;
		return super.changeDimension(resourceKey);
	}

	@Override
	public void remove() {
		if (!this.level.isClientSide && this.dropEquipment) {
			Containers.dropContents(this.level, this, this);
		}

		super.remove();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.lootTable != null) {
			compoundTag.putString("LootTable", this.lootTable.toString());
			if (this.lootTableSeed != 0L) {
				compoundTag.putLong("LootTableSeed", this.lootTableSeed);
			}
		} else {
			ContainerHelper.saveAllItems(compoundTag, this.itemStacks);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (compoundTag.contains("LootTable", 8)) {
			this.lootTable = new ResourceLocation(compoundTag.getString("LootTable"));
			this.lootTableSeed = compoundTag.getLong("LootTableSeed");
		} else {
			ContainerHelper.loadAllItems(compoundTag, this.itemStacks);
		}
	}

	@Override
	public boolean interact(Player player, InteractionHand interactionHand) {
		player.openMenu(this);
		return true;
	}

	@Override
	protected void applyNaturalSlowdown() {
		float f = 0.98F;
		if (this.lootTable == null) {
			int i = 15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this);
			f += (float)i * 0.001F;
		}

		this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.0, (double)f));
	}

	public void unpackLootTable(@Nullable Player player) {
		if (this.lootTable != null && this.level.getServer() != null) {
			LootTable lootTable = this.level.getServer().getLootTables().get(this.lootTable);
			if (player instanceof ServerPlayer) {
				CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.lootTable);
			}

			this.lootTable = null;
			LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.level)
				.withParameter(LootContextParams.BLOCK_POS, this.blockPosition())
				.withOptionalRandomSeed(this.lootTableSeed);
			if (player != null) {
				builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
			}

			lootTable.fill(this, builder.create(LootContextParamSets.CHEST));
		}
	}

	@Override
	public void clearContent() {
		this.unpackLootTable(null);
		this.itemStacks.clear();
	}

	public void setLootTable(ResourceLocation resourceLocation, long l) {
		this.lootTable = resourceLocation;
		this.lootTableSeed = l;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		if (this.lootTable != null && player.isSpectator()) {
			return null;
		} else {
			this.unpackLootTable(inventory.player);
			return this.createMenu(i, inventory);
		}
	}

	protected abstract AbstractContainerMenu createMenu(int i, Inventory inventory);
}
