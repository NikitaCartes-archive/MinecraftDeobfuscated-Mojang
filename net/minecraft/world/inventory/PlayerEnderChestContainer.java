/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.jetbrains.annotations.Nullable;

public class PlayerEnderChestContainer
extends SimpleContainer {
    @Nullable
    private EnderChestBlockEntity activeChest;

    public PlayerEnderChestContainer() {
        super(27);
    }

    public void setActiveChest(EnderChestBlockEntity enderChestBlockEntity) {
        this.activeChest = enderChestBlockEntity;
    }

    public boolean isActiveChest(EnderChestBlockEntity enderChestBlockEntity) {
        return this.activeChest == enderChestBlockEntity;
    }

    @Override
    public void fromTag(ListTag listTag) {
        int i;
        for (i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, ItemStack.EMPTY);
        }
        for (i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            int j = compoundTag.getByte("Slot") & 0xFF;
            if (j < 0 || j >= this.getContainerSize()) continue;
            this.setItem(j, ItemStack.of(compoundTag));
        }
    }

    @Override
    public ListTag createTag() {
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty()) continue;
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putByte("Slot", (byte)i);
            itemStack.save(compoundTag);
            listTag.add(compoundTag);
        }
        return listTag;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.activeChest != null && !this.activeChest.stillValid(player)) {
            return false;
        }
        return super.stillValid(player);
    }

    @Override
    public void startOpen(Player player) {
        if (this.activeChest != null) {
            this.activeChest.startOpen(player);
        }
        super.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        if (this.activeChest != null) {
            this.activeChest.stopOpen(player);
        }
        super.stopOpen(player);
        this.activeChest = null;
    }
}

