/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractContainerMenu {
    private final NonNullList<ItemStack> lastSlots = NonNullList.create();
    public final List<Slot> slots = Lists.newArrayList();
    private final List<DataSlot> dataSlots = Lists.newArrayList();
    @Nullable
    private final MenuType<?> menuType;
    public final int containerId;
    @Environment(value=EnvType.CLIENT)
    private short changeUid;
    private int quickcraftType = -1;
    private int quickcraftStatus;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();
    private final List<ContainerListener> containerListeners = Lists.newArrayList();
    private final Set<Player> unSynchedPlayers = Sets.newHashSet();

    protected AbstractContainerMenu(@Nullable MenuType<?> menuType, int i) {
        this.menuType = menuType;
        this.containerId = i;
    }

    protected static boolean stillValid(ContainerLevelAccess containerLevelAccess, Player player, Block block) {
        return containerLevelAccess.evaluate((level, blockPos) -> {
            if (!level.getBlockState((BlockPos)blockPos).is(block)) {
                return false;
            }
            return player.distanceToSqr((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) <= 64.0;
        }, true);
    }

    public MenuType<?> getType() {
        if (this.menuType == null) {
            throw new UnsupportedOperationException("Unable to construct this menu by type");
        }
        return this.menuType;
    }

    protected static void checkContainerSize(Container container, int i) {
        int j = container.getContainerSize();
        if (j < i) {
            throw new IllegalArgumentException("Container size " + j + " is smaller than expected " + i);
        }
    }

    protected static void checkContainerDataCount(ContainerData containerData, int i) {
        int j = containerData.getCount();
        if (j < i) {
            throw new IllegalArgumentException("Container data count " + j + " is smaller than expected " + i);
        }
    }

    protected Slot addSlot(Slot slot) {
        slot.index = this.slots.size();
        this.slots.add(slot);
        this.lastSlots.add(ItemStack.EMPTY);
        return slot;
    }

    protected DataSlot addDataSlot(DataSlot dataSlot) {
        this.dataSlots.add(dataSlot);
        return dataSlot;
    }

    protected void addDataSlots(ContainerData containerData) {
        for (int i = 0; i < containerData.getCount(); ++i) {
            this.addDataSlot(DataSlot.forContainer(containerData, i));
        }
    }

    public void addSlotListener(ContainerListener containerListener) {
        if (this.containerListeners.contains(containerListener)) {
            return;
        }
        this.containerListeners.add(containerListener);
        containerListener.refreshContainer(this, this.getItems());
        this.broadcastChanges();
    }

    @Environment(value=EnvType.CLIENT)
    public void removeSlotListener(ContainerListener containerListener) {
        this.containerListeners.remove(containerListener);
    }

    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        for (int i = 0; i < this.slots.size(); ++i) {
            nonNullList.add(this.slots.get(i).getItem());
        }
        return nonNullList;
    }

    public void broadcastChanges() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack itemStack = this.slots.get(i).getItem();
            ItemStack itemStack2 = this.lastSlots.get(i);
            if (ItemStack.matches(itemStack2, itemStack)) continue;
            ItemStack itemStack3 = itemStack.copy();
            this.lastSlots.set(i, itemStack3);
            for (ContainerListener containerListener : this.containerListeners) {
                containerListener.slotChanged(this, i, itemStack3);
            }
        }
        for (i = 0; i < this.dataSlots.size(); ++i) {
            DataSlot dataSlot = this.dataSlots.get(i);
            if (!dataSlot.checkAndClearUpdateFlag()) continue;
            for (ContainerListener containerListener2 : this.containerListeners) {
                containerListener2.setContainerData(this, i, dataSlot.get());
            }
        }
    }

    public boolean clickMenuButton(Player player, int i) {
        return false;
    }

    public Slot getSlot(int i) {
        return this.slots.get(i);
    }

    public ItemStack quickMoveStack(Player player, int i) {
        Slot slot = this.slots.get(i);
        if (slot != null) {
            return slot.getItem();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack clicked(int i, int j, ClickType clickType, Player player) {
        try {
            return this.doClick(i, j, clickType, player);
        } catch (Exception exception) {
            CrashReport crashReport = CrashReport.forThrowable(exception, "Container click");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Click info");
            crashReportCategory.setDetail("Menu", () -> this.menuType != null ? Registry.MENU.getKey(this.menuType).toString() : "<no type>");
            crashReportCategory.setDetail("Slot", i);
            crashReportCategory.setDetail("Button", j);
            crashReportCategory.setDetail("Type", (Object)clickType);
            throw new ReportedException(crashReport);
        }
    }

    private ItemStack doClick(int i, int j, ClickType clickType, Player player) {
        ItemStack itemStack = ItemStack.EMPTY;
        Inventory inventory = player.inventory;
        if (clickType == ClickType.QUICK_CRAFT) {
            int k = this.quickcraftStatus;
            this.quickcraftStatus = AbstractContainerMenu.getQuickcraftHeader(j);
            if ((k != 1 || this.quickcraftStatus != 2) && k != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (inventory.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = AbstractContainerMenu.getQuickcraftType(j);
                if (AbstractContainerMenu.isValidQuickcraftType(this.quickcraftType, player)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                Slot slot = this.slots.get(i);
                ItemStack itemStack2 = inventory.getCarried();
                if (slot != null && AbstractContainerMenu.canItemQuickReplace(slot, itemStack2, true) && slot.mayPlace(itemStack2) && (this.quickcraftType == 2 || itemStack2.getCount() > this.quickcraftSlots.size()) && this.canDragTo(slot)) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    ItemStack itemStack3 = inventory.getCarried().copy();
                    int l = inventory.getCarried().getCount();
                    for (Slot slot2 : this.quickcraftSlots) {
                        ItemStack itemStack4 = inventory.getCarried();
                        if (slot2 == null || !AbstractContainerMenu.canItemQuickReplace(slot2, itemStack4, true) || !slot2.mayPlace(itemStack4) || this.quickcraftType != 2 && itemStack4.getCount() < this.quickcraftSlots.size() || !this.canDragTo(slot2)) continue;
                        ItemStack itemStack5 = itemStack3.copy();
                        int m = slot2.hasItem() ? slot2.getItem().getCount() : 0;
                        AbstractContainerMenu.getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, itemStack5, m);
                        int n = Math.min(itemStack5.getMaxStackSize(), slot2.getMaxStackSize(itemStack5));
                        if (itemStack5.getCount() > n) {
                            itemStack5.setCount(n);
                        }
                        l -= itemStack5.getCount() - m;
                        slot2.set(itemStack5);
                    }
                    itemStack3.setCount(l);
                    inventory.setCarried(itemStack3);
                }
                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else if (!(clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE || j != 0 && j != 1)) {
            if (i == -999) {
                if (!inventory.getCarried().isEmpty()) {
                    if (j == 0) {
                        player.drop(inventory.getCarried(), true);
                        inventory.setCarried(ItemStack.EMPTY);
                    }
                    if (j == 1) {
                        player.drop(inventory.getCarried().split(1), true);
                    }
                }
            } else if (clickType == ClickType.QUICK_MOVE) {
                if (i < 0) {
                    return ItemStack.EMPTY;
                }
                Slot slot3 = this.slots.get(i);
                if (slot3 == null || !slot3.mayPickup(player)) {
                    return ItemStack.EMPTY;
                }
                ItemStack itemStack3 = this.quickMoveStack(player, i);
                while (!itemStack3.isEmpty() && ItemStack.isSame(slot3.getItem(), itemStack3)) {
                    itemStack = itemStack3.copy();
                    itemStack3 = this.quickMoveStack(player, i);
                }
            } else {
                if (i < 0) {
                    return ItemStack.EMPTY;
                }
                Slot slot3 = this.slots.get(i);
                if (slot3 != null) {
                    ItemStack itemStack3 = slot3.getItem();
                    ItemStack itemStack2 = inventory.getCarried();
                    if (!itemStack3.isEmpty()) {
                        itemStack = itemStack3.copy();
                    }
                    if (itemStack3.isEmpty()) {
                        if (!itemStack2.isEmpty() && slot3.mayPlace(itemStack2)) {
                            int o;
                            int n = o = j == 0 ? itemStack2.getCount() : 1;
                            if (o > slot3.getMaxStackSize(itemStack2)) {
                                o = slot3.getMaxStackSize(itemStack2);
                            }
                            slot3.set(itemStack2.split(o));
                        }
                    } else if (slot3.mayPickup(player)) {
                        int o;
                        if (itemStack2.isEmpty()) {
                            if (itemStack3.isEmpty()) {
                                slot3.set(ItemStack.EMPTY);
                                inventory.setCarried(ItemStack.EMPTY);
                            } else {
                                int o2 = j == 0 ? itemStack3.getCount() : (itemStack3.getCount() + 1) / 2;
                                inventory.setCarried(slot3.remove(o2));
                                if (itemStack3.isEmpty()) {
                                    slot3.set(ItemStack.EMPTY);
                                }
                                slot3.onTake(player, inventory.getCarried());
                            }
                        } else if (slot3.mayPlace(itemStack2)) {
                            if (AbstractContainerMenu.consideredTheSameItem(itemStack3, itemStack2)) {
                                int o3;
                                int n = o3 = j == 0 ? itemStack2.getCount() : 1;
                                if (o3 > slot3.getMaxStackSize(itemStack2) - itemStack3.getCount()) {
                                    o3 = slot3.getMaxStackSize(itemStack2) - itemStack3.getCount();
                                }
                                if (o3 > itemStack2.getMaxStackSize() - itemStack3.getCount()) {
                                    o3 = itemStack2.getMaxStackSize() - itemStack3.getCount();
                                }
                                itemStack2.shrink(o3);
                                itemStack3.grow(o3);
                            } else if (itemStack2.getCount() <= slot3.getMaxStackSize(itemStack2)) {
                                slot3.set(itemStack2);
                                inventory.setCarried(itemStack3);
                            }
                        } else if (itemStack2.getMaxStackSize() > 1 && AbstractContainerMenu.consideredTheSameItem(itemStack3, itemStack2) && !itemStack3.isEmpty() && (o = itemStack3.getCount()) + itemStack2.getCount() <= itemStack2.getMaxStackSize()) {
                            itemStack2.grow(o);
                            itemStack3 = slot3.remove(o);
                            if (itemStack3.isEmpty()) {
                                slot3.set(ItemStack.EMPTY);
                            }
                            slot3.onTake(player, inventory.getCarried());
                        }
                    }
                    slot3.setChanged();
                }
            }
        } else if (clickType == ClickType.SWAP) {
            Slot slot3 = this.slots.get(i);
            ItemStack itemStack3 = inventory.getItem(j);
            ItemStack itemStack2 = slot3.getItem();
            if (!itemStack3.isEmpty() || !itemStack2.isEmpty()) {
                if (itemStack3.isEmpty()) {
                    if (slot3.mayPickup(player)) {
                        inventory.setItem(j, itemStack2);
                        slot3.onSwapCraft(itemStack2.getCount());
                        slot3.set(ItemStack.EMPTY);
                        slot3.onTake(player, itemStack2);
                    }
                } else if (itemStack2.isEmpty()) {
                    if (slot3.mayPlace(itemStack3)) {
                        int o = slot3.getMaxStackSize(itemStack3);
                        if (itemStack3.getCount() > o) {
                            slot3.set(itemStack3.split(o));
                        } else {
                            slot3.set(itemStack3);
                            inventory.setItem(j, ItemStack.EMPTY);
                        }
                    }
                } else if (slot3.mayPickup(player) && slot3.mayPlace(itemStack3)) {
                    int o = slot3.getMaxStackSize(itemStack3);
                    if (itemStack3.getCount() > o) {
                        slot3.set(itemStack3.split(o));
                        slot3.onTake(player, itemStack2);
                        if (!inventory.add(itemStack2)) {
                            player.drop(itemStack2, true);
                        }
                    } else {
                        slot3.set(itemStack3);
                        inventory.setItem(j, itemStack2);
                        slot3.onTake(player, itemStack2);
                    }
                }
            }
        } else if (clickType == ClickType.CLONE && player.abilities.instabuild && inventory.getCarried().isEmpty() && i >= 0) {
            Slot slot3 = this.slots.get(i);
            if (slot3 != null && slot3.hasItem()) {
                ItemStack itemStack3 = slot3.getItem().copy();
                itemStack3.setCount(itemStack3.getMaxStackSize());
                inventory.setCarried(itemStack3);
            }
        } else if (clickType == ClickType.THROW && inventory.getCarried().isEmpty() && i >= 0) {
            Slot slot3 = this.slots.get(i);
            if (slot3 != null && slot3.hasItem() && slot3.mayPickup(player)) {
                ItemStack itemStack3 = slot3.remove(j == 0 ? 1 : slot3.getItem().getCount());
                slot3.onTake(player, itemStack3);
                player.drop(itemStack3, true);
            }
        } else if (clickType == ClickType.PICKUP_ALL && i >= 0) {
            Slot slot3 = this.slots.get(i);
            ItemStack itemStack3 = inventory.getCarried();
            if (!(itemStack3.isEmpty() || slot3 != null && slot3.hasItem() && slot3.mayPickup(player))) {
                int l = j == 0 ? 0 : this.slots.size() - 1;
                int o = j == 0 ? 1 : -1;
                for (int p = 0; p < 2; ++p) {
                    for (int q = l; q >= 0 && q < this.slots.size() && itemStack3.getCount() < itemStack3.getMaxStackSize(); q += o) {
                        Slot slot4 = this.slots.get(q);
                        if (!slot4.hasItem() || !AbstractContainerMenu.canItemQuickReplace(slot4, itemStack3, true) || !slot4.mayPickup(player) || !this.canTakeItemForPickAll(itemStack3, slot4)) continue;
                        ItemStack itemStack6 = slot4.getItem();
                        if (p == 0 && itemStack6.getCount() == itemStack6.getMaxStackSize()) continue;
                        int n = Math.min(itemStack3.getMaxStackSize() - itemStack3.getCount(), itemStack6.getCount());
                        ItemStack itemStack7 = slot4.remove(n);
                        itemStack3.grow(n);
                        if (itemStack7.isEmpty()) {
                            slot4.set(ItemStack.EMPTY);
                        }
                        slot4.onTake(player, itemStack7);
                    }
                }
            }
            this.broadcastChanges();
        }
        return itemStack;
    }

    public static boolean consideredTheSameItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.getItem() == itemStack2.getItem() && ItemStack.tagMatches(itemStack, itemStack2);
    }

    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return true;
    }

    public void removed(Player player) {
        Inventory inventory = player.inventory;
        if (!inventory.getCarried().isEmpty()) {
            player.drop(inventory.getCarried(), false);
            inventory.setCarried(ItemStack.EMPTY);
        }
    }

    protected void clearContainer(Player player, Level level, Container container) {
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
            for (int i = 0; i < container.getContainerSize(); ++i) {
                player.drop(container.removeItemNoUpdate(i), false);
            }
            return;
        }
        for (int i = 0; i < container.getContainerSize(); ++i) {
            player.inventory.placeItemBackInInventory(level, container.removeItemNoUpdate(i));
        }
    }

    public void slotsChanged(Container container) {
        this.broadcastChanges();
    }

    public void setItem(int i, ItemStack itemStack) {
        this.getSlot(i).set(itemStack);
    }

    @Environment(value=EnvType.CLIENT)
    public void setAll(List<ItemStack> list) {
        for (int i = 0; i < list.size(); ++i) {
            this.getSlot(i).set(list.get(i));
        }
    }

    public void setData(int i, int j) {
        this.dataSlots.get(i).set(j);
    }

    @Environment(value=EnvType.CLIENT)
    public short backup(Inventory inventory) {
        this.changeUid = (short)(this.changeUid + 1);
        return this.changeUid;
    }

    public boolean isSynched(Player player) {
        return !this.unSynchedPlayers.contains(player);
    }

    public void setSynched(Player player, boolean bl) {
        if (bl) {
            this.unSynchedPlayers.remove(player);
        } else {
            this.unSynchedPlayers.add(player);
        }
    }

    public abstract boolean stillValid(Player var1);

    protected boolean moveItemStackTo(ItemStack itemStack, int i, int j, boolean bl) {
        ItemStack itemStack2;
        Slot slot;
        boolean bl2 = false;
        int k = i;
        if (bl) {
            k = j - 1;
        }
        if (itemStack.isStackable()) {
            while (!itemStack.isEmpty() && (bl ? k >= i : k < j)) {
                slot = this.slots.get(k);
                itemStack2 = slot.getItem();
                if (!itemStack2.isEmpty() && AbstractContainerMenu.consideredTheSameItem(itemStack, itemStack2)) {
                    int l = itemStack2.getCount() + itemStack.getCount();
                    if (l <= itemStack.getMaxStackSize()) {
                        itemStack.setCount(0);
                        itemStack2.setCount(l);
                        slot.setChanged();
                        bl2 = true;
                    } else if (itemStack2.getCount() < itemStack.getMaxStackSize()) {
                        itemStack.shrink(itemStack.getMaxStackSize() - itemStack2.getCount());
                        itemStack2.setCount(itemStack.getMaxStackSize());
                        slot.setChanged();
                        bl2 = true;
                    }
                }
                if (bl) {
                    --k;
                    continue;
                }
                ++k;
            }
        }
        if (!itemStack.isEmpty()) {
            k = bl ? j - 1 : i;
            while (bl ? k >= i : k < j) {
                slot = this.slots.get(k);
                itemStack2 = slot.getItem();
                if (itemStack2.isEmpty() && slot.mayPlace(itemStack)) {
                    if (itemStack.getCount() > slot.getMaxStackSize()) {
                        slot.set(itemStack.split(slot.getMaxStackSize()));
                    } else {
                        slot.set(itemStack.split(itemStack.getCount()));
                    }
                    slot.setChanged();
                    bl2 = true;
                    break;
                }
                if (bl) {
                    --k;
                    continue;
                }
                ++k;
            }
        }
        return bl2;
    }

    public static int getQuickcraftType(int i) {
        return i >> 2 & 3;
    }

    public static int getQuickcraftHeader(int i) {
        return i & 3;
    }

    @Environment(value=EnvType.CLIENT)
    public static int getQuickcraftMask(int i, int j) {
        return i & 3 | (j & 3) << 2;
    }

    public static boolean isValidQuickcraftType(int i, Player player) {
        if (i == 0) {
            return true;
        }
        if (i == 1) {
            return true;
        }
        return i == 2 && player.abilities.instabuild;
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack itemStack, boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = slot == null || !slot.hasItem();
        if (!bl2 && itemStack.sameItem(slot.getItem()) && ItemStack.tagMatches(slot.getItem(), itemStack)) {
            return slot.getItem().getCount() + (bl ? 0 : itemStack.getCount()) <= itemStack.getMaxStackSize();
        }
        return bl2;
    }

    public static void getQuickCraftSlotCount(Set<Slot> set, int i, ItemStack itemStack, int j) {
        switch (i) {
            case 0: {
                itemStack.setCount(Mth.floor((float)itemStack.getCount() / (float)set.size()));
                break;
            }
            case 1: {
                itemStack.setCount(1);
                break;
            }
            case 2: {
                itemStack.setCount(itemStack.getItem().getMaxStackSize());
            }
        }
        itemStack.grow(j);
    }

    public boolean canDragTo(Slot slot) {
        return true;
    }

    public static int getRedstoneSignalFromBlockEntity(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof Container) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)((Object)blockEntity));
        }
        return 0;
    }

    public static int getRedstoneSignalFromContainer(@Nullable Container container) {
        if (container == null) {
            return 0;
        }
        int i = 0;
        float f = 0.0f;
        for (int j = 0; j < container.getContainerSize(); ++j) {
            ItemStack itemStack = container.getItem(j);
            if (itemStack.isEmpty()) continue;
            f += (float)itemStack.getCount() / (float)Math.min(container.getMaxStackSize(), itemStack.getMaxStackSize());
            ++i;
        }
        return Mth.floor((f /= (float)container.getContainerSize()) * 14.0f) + (i > 0 ? 1 : 0);
    }
}

