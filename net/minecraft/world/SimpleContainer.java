/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SimpleContainer
implements Container,
StackedContentsCompatible {
    private final int size;
    private final NonNullList<ItemStack> items;
    @Nullable
    private List<ContainerListener> listeners;

    public SimpleContainer(int i) {
        this.size = i;
        this.items = NonNullList.withSize(i, ItemStack.EMPTY);
    }

    public SimpleContainer(ItemStack ... itemStacks) {
        this.size = itemStacks.length;
        this.items = NonNullList.of(ItemStack.EMPTY, itemStacks);
    }

    public void addListener(ContainerListener containerListener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }
        this.listeners.add(containerListener);
    }

    public void removeListener(ContainerListener containerListener) {
        if (this.listeners != null) {
            this.listeners.remove(containerListener);
        }
    }

    @Override
    public ItemStack getItem(int i) {
        if (i < 0 || i >= this.items.size()) {
            return ItemStack.EMPTY;
        }
        return this.items.get(i);
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = this.items.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemStack = ContainerHelper.removeItem(this.items, i, j);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }
        return itemStack;
    }

    public ItemStack removeItemType(Item item, int i) {
        ItemStack itemStack = new ItemStack(item, 0);
        for (int j = this.size - 1; j >= 0; --j) {
            ItemStack itemStack2 = this.getItem(j);
            if (!itemStack2.getItem().equals(item)) continue;
            int k = i - itemStack.getCount();
            ItemStack itemStack3 = itemStack2.split(k);
            itemStack.grow(itemStack3.getCount());
            if (itemStack.getCount() == i) break;
        }
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }
        return itemStack;
    }

    public ItemStack addItem(ItemStack itemStack) {
        ItemStack itemStack2 = itemStack.copy();
        this.moveItemToOccupiedSlotsWithSameType(itemStack2);
        if (itemStack2.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.moveItemToEmptySlots(itemStack2);
        if (itemStack2.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return itemStack2;
    }

    public boolean canAddItem(ItemStack itemStack) {
        boolean bl = false;
        for (ItemStack itemStack2 : this.items) {
            if (!itemStack2.isEmpty() && (!ItemStack.isSameItemSameTags(itemStack2, itemStack) || itemStack2.getCount() >= itemStack2.getMaxStackSize())) continue;
            bl = true;
            break;
        }
        return bl;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        ItemStack itemStack = this.items.get(i);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.items.set(i, ItemStack.EMPTY);
        return itemStack;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.items.set(i, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public void setChanged() {
        if (this.listeners != null) {
            for (ContainerListener containerListener : this.listeners) {
                containerListener.containerChanged(this);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public void fillStackedContents(StackedContents stackedContents) {
        for (ItemStack itemStack : this.items) {
            stackedContents.accountStack(itemStack);
        }
    }

    public String toString() {
        return this.items.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList()).toString();
    }

    private void moveItemToEmptySlots(ItemStack itemStack) {
        for (int i = 0; i < this.size; ++i) {
            ItemStack itemStack2 = this.getItem(i);
            if (!itemStack2.isEmpty()) continue;
            this.setItem(i, itemStack.copy());
            itemStack.setCount(0);
            return;
        }
    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack itemStack) {
        for (int i = 0; i < this.size; ++i) {
            ItemStack itemStack2 = this.getItem(i);
            if (!ItemStack.isSameItemSameTags(itemStack2, itemStack)) continue;
            this.moveItemsBetweenStacks(itemStack, itemStack2);
            if (!itemStack.isEmpty()) continue;
            return;
        }
    }

    private void moveItemsBetweenStacks(ItemStack itemStack, ItemStack itemStack2) {
        int i = Math.min(this.getMaxStackSize(), itemStack2.getMaxStackSize());
        int j = Math.min(itemStack.getCount(), i - itemStack2.getCount());
        if (j > 0) {
            itemStack2.grow(j);
            itemStack.shrink(j);
            this.setChanged();
        }
    }

    public void fromTag(ListTag listTag) {
        this.clearContent();
        for (int i = 0; i < listTag.size(); ++i) {
            ItemStack itemStack = ItemStack.of(listTag.getCompound(i));
            if (itemStack.isEmpty()) continue;
            this.addItem(itemStack);
        }
    }

    public ListTag createTag() {
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty()) continue;
            listTag.add(itemStack.save(new CompoundTag()));
        }
        return listTag;
    }
}

