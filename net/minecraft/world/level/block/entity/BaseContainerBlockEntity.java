/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public abstract class BaseContainerBlockEntity
extends BlockEntity
implements Container,
MenuProvider,
Nameable {
    private LockCode lockKey = LockCode.NO_LOCK;
    private Component name;

    protected BaseContainerBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.lockKey = LockCode.fromTag(compoundTag);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        this.lockKey.addToTag(compoundTag);
        if (this.name != null) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
        return compoundTag;
    }

    public void setCustomName(Component component) {
        this.name = component;
    }

    @Override
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return this.getDefaultName();
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    protected abstract Component getDefaultName();

    public boolean canOpen(Player player) {
        return BaseContainerBlockEntity.canUnlock(player, this.lockKey, this.getDisplayName());
    }

    public static boolean canUnlock(Player player, LockCode lockCode, Component component) {
        if (player.isSpectator() || lockCode.unlocksWith(player.getMainHandItem())) {
            return true;
        }
        player.displayClientMessage(new TranslatableComponent("container.isLocked", component), true);
        player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0f, 1.0f);
        return false;
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (this.canOpen(player)) {
            return this.createMenu(i, inventory);
        }
        return null;
    }

    protected abstract AbstractContainerMenu createMenu(int var1, Inventory var2);
}

