package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public interface ContainerEntity extends Container, MenuProvider {
	Vec3 position();

	@Nullable
	ResourceLocation getLootTable();

	void setLootTable(@Nullable ResourceLocation resourceLocation);

	long getLootTableSeed();

	void setLootTableSeed(long l);

	NonNullList<ItemStack> getItemStacks();

	void clearItemStacks();

	Level getLevel();

	boolean isRemoved();

	@Override
	default boolean isEmpty() {
		return this.isChestVehicleEmpty();
	}

	default void addChestVehicleSaveData(CompoundTag compoundTag) {
		if (this.getLootTable() != null) {
			compoundTag.putString("LootTable", this.getLootTable().toString());
			if (this.getLootTableSeed() != 0L) {
				compoundTag.putLong("LootTableSeed", this.getLootTableSeed());
			}
		} else {
			ContainerHelper.saveAllItems(compoundTag, this.getItemStacks());
		}
	}

	default void readChestVehicleSaveData(CompoundTag compoundTag) {
		this.clearItemStacks();
		if (compoundTag.contains("LootTable", 8)) {
			this.setLootTable(new ResourceLocation(compoundTag.getString("LootTable")));
			this.setLootTableSeed(compoundTag.getLong("LootTableSeed"));
		} else {
			ContainerHelper.loadAllItems(compoundTag, this.getItemStacks());
		}
	}

	default void chestVehicleDestroyed(DamageSource damageSource, Level level, Entity entity) {
		if (level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			Containers.dropContents(level, entity, this);
			if (!level.isClientSide) {
				Entity entity2 = damageSource.getDirectEntity();
				if (entity2 != null && entity2.getType() == EntityType.PLAYER) {
					PiglinAi.angerNearbyPiglins((Player)entity2, true);
				}
			}
		}
	}

	default InteractionResult interactWithContainerVehicle(Player player) {
		player.openMenu(this);
		return !player.level.isClientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
	}

	default void unpackChestVehicleLootTable(@Nullable Player player) {
		MinecraftServer minecraftServer = this.getLevel().getServer();
		if (this.getLootTable() != null && minecraftServer != null) {
			LootTable lootTable = minecraftServer.getLootData().getLootTable(this.getLootTable());
			if (player != null) {
				CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.getLootTable());
			}

			this.setLootTable(null);
			LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.getLevel())
				.withParameter(LootContextParams.ORIGIN, this.position())
				.withOptionalRandomSeed(this.getLootTableSeed());
			if (player != null) {
				builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
			}

			lootTable.fill(this, builder.create(LootContextParamSets.CHEST));
		}
	}

	default void clearChestVehicleContent() {
		this.unpackChestVehicleLootTable(null);
		this.getItemStacks().clear();
	}

	default boolean isChestVehicleEmpty() {
		for (ItemStack itemStack : this.getItemStacks()) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	default ItemStack removeChestVehicleItemNoUpdate(int i) {
		this.unpackChestVehicleLootTable(null);
		ItemStack itemStack = this.getItemStacks().get(i);
		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			this.getItemStacks().set(i, ItemStack.EMPTY);
			return itemStack;
		}
	}

	default ItemStack getChestVehicleItem(int i) {
		this.unpackChestVehicleLootTable(null);
		return this.getItemStacks().get(i);
	}

	default ItemStack removeChestVehicleItem(int i, int j) {
		this.unpackChestVehicleLootTable(null);
		return ContainerHelper.removeItem(this.getItemStacks(), i, j);
	}

	default void setChestVehicleItem(int i, ItemStack itemStack) {
		this.unpackChestVehicleLootTable(null);
		this.getItemStacks().set(i, itemStack);
		if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
			itemStack.setCount(this.getMaxStackSize());
		}
	}

	default SlotAccess getChestVehicleSlot(int i) {
		return i >= 0 && i < this.getContainerSize() ? new SlotAccess() {
			@Override
			public ItemStack get() {
				return ContainerEntity.this.getChestVehicleItem(i);
			}

			@Override
			public boolean set(ItemStack itemStack) {
				ContainerEntity.this.setChestVehicleItem(i, itemStack);
				return true;
			}
		} : SlotAccess.NULL;
	}

	default boolean isChestVehicleStillValid(Player player) {
		return !this.isRemoved() && this.position().closerThan(player.position(), 8.0);
	}
}
