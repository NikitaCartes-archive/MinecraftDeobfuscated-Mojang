/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.vehicle;

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
import org.jetbrains.annotations.Nullable;

public interface ContainerEntity
extends Container,
MenuProvider {
    public Vec3 position();

    @Nullable
    public ResourceLocation getLootTable();

    public void setLootTable(@Nullable ResourceLocation var1);

    public long getLootTableSeed();

    public void setLootTableSeed(long var1);

    public NonNullList<ItemStack> getItemStacks();

    public void clearItemStacks();

    public Level getLevel();

    public boolean isRemoved();

    @Override
    default public boolean isEmpty() {
        return this.isChestVehicleEmpty();
    }

    default public void addChestVehicleSaveData(CompoundTag compoundTag) {
        if (this.getLootTable() != null) {
            compoundTag.putString("LootTable", this.getLootTable().toString());
            if (this.getLootTableSeed() != 0L) {
                compoundTag.putLong("LootTableSeed", this.getLootTableSeed());
            }
        } else {
            ContainerHelper.saveAllItems(compoundTag, this.getItemStacks());
        }
    }

    default public void readChestVehicleSaveData(CompoundTag compoundTag) {
        this.clearItemStacks();
        if (compoundTag.contains("LootTable", 8)) {
            this.setLootTable(new ResourceLocation(compoundTag.getString("LootTable")));
            this.setLootTableSeed(compoundTag.getLong("LootTableSeed"));
        } else {
            ContainerHelper.loadAllItems(compoundTag, this.getItemStacks());
        }
    }

    default public void chestVehicleDestroyed(DamageSource damageSource, Level level, Entity entity) {
        Entity entity2;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        Containers.dropContents(level, entity, (Container)this);
        if (!level.isClientSide && (entity2 = damageSource.getDirectEntity()) != null && entity2.getType() == EntityType.PLAYER) {
            PiglinAi.angerNearbyPiglins((Player)entity2, true);
        }
    }

    default public InteractionResult interactWithContainerVehicle(Player player) {
        player.openMenu(this);
        if (!player.level.isClientSide) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    default public void unpackChestVehicleLootTable(@Nullable Player player) {
        MinecraftServer minecraftServer = this.getLevel().getServer();
        if (this.getLootTable() != null && minecraftServer != null) {
            LootTable lootTable = minecraftServer.getLootTables().get(this.getLootTable());
            if (player != null) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.getLootTable());
            }
            this.setLootTable(null);
            LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.getLevel()).withParameter(LootContextParams.ORIGIN, this.position()).withOptionalRandomSeed(this.getLootTableSeed());
            if (player != null) {
                builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }
            lootTable.fill(this, builder.create(LootContextParamSets.CHEST));
        }
    }

    default public void clearChestVehicleContent() {
        this.unpackChestVehicleLootTable(null);
        this.getItemStacks().clear();
    }

    default public boolean isChestVehicleEmpty() {
        for (ItemStack itemStack : this.getItemStacks()) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    default public ItemStack removeChestVehicleItemNoUpdate(int i) {
        this.unpackChestVehicleLootTable(null);
        ItemStack itemStack = this.getItemStacks().get(i);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.getItemStacks().set(i, ItemStack.EMPTY);
        return itemStack;
    }

    default public ItemStack getChestVehicleItem(int i) {
        this.unpackChestVehicleLootTable(null);
        return this.getItemStacks().get(i);
    }

    default public ItemStack removeChestVehicleItem(int i, int j) {
        this.unpackChestVehicleLootTable(null);
        return ContainerHelper.removeItem(this.getItemStacks(), i, j);
    }

    default public void setChestVehicleItem(int i, ItemStack itemStack) {
        this.unpackChestVehicleLootTable(null);
        this.getItemStacks().set(i, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }
    }

    default public SlotAccess getChestVehicleSlot(final int i) {
        if (i >= 0 && i < this.getContainerSize()) {
            return new SlotAccess(){

                @Override
                public ItemStack get() {
                    return ContainerEntity.this.getChestVehicleItem(i);
                }

                @Override
                public boolean set(ItemStack itemStack) {
                    ContainerEntity.this.setChestVehicleItem(i, itemStack);
                    return true;
                }
            };
        }
        return SlotAccess.NULL;
    }

    default public boolean isChestVehicleStillValid(Player player) {
        return !this.isRemoved() && this.position().closerThan(player.position(), 8.0);
    }
}

