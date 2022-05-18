/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractContainerMenu {
    private static Logger LOGGER = LogUtils.getLogger();
    public static final int SLOT_CLICKED_OUTSIDE = -999;
    public static final int QUICKCRAFT_TYPE_CHARITABLE = 0;
    public static final int QUICKCRAFT_TYPE_GREEDY = 1;
    public static final int QUICKCRAFT_TYPE_CLONE = 2;
    public static final int QUICKCRAFT_HEADER_START = 0;
    public static final int QUICKCRAFT_HEADER_CONTINUE = 1;
    public static final int QUICKCRAFT_HEADER_END = 2;
    public static final int CARRIED_SLOT_SIZE = Integer.MAX_VALUE;
    private final NonNullList<ItemStack> lastSlots = NonNullList.create();
    public final NonNullList<Slot> slots = NonNullList.create();
    private final List<DataSlot> dataSlots = Lists.newArrayList();
    private ItemStack carried = ItemStack.EMPTY;
    private final NonNullList<ItemStack> remoteSlots = NonNullList.create();
    private final IntList remoteDataSlots = new IntArrayList();
    private ItemStack remoteCarried = ItemStack.EMPTY;
    private int stateId;
    @Nullable
    private final MenuType<?> menuType;
    public final int containerId;
    private int quickcraftType = -1;
    private int quickcraftStatus;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();
    private final List<ContainerListener> containerListeners = Lists.newArrayList();
    @Nullable
    private ContainerSynchronizer synchronizer;
    private boolean suppressRemoteUpdates;

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

    public boolean isValidSlotIndex(int i) {
        return i == -1 || i == -999 || i < this.slots.size();
    }

    protected Slot addSlot(Slot slot) {
        slot.index = this.slots.size();
        this.slots.add(slot);
        this.lastSlots.add(ItemStack.EMPTY);
        this.remoteSlots.add(ItemStack.EMPTY);
        return slot;
    }

    protected DataSlot addDataSlot(DataSlot dataSlot) {
        this.dataSlots.add(dataSlot);
        this.remoteDataSlots.add(0);
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
        this.broadcastChanges();
    }

    public void setSynchronizer(ContainerSynchronizer containerSynchronizer) {
        this.synchronizer = containerSynchronizer;
        this.sendAllDataToRemote();
    }

    public void sendAllDataToRemote() {
        int i;
        int j = this.slots.size();
        for (i = 0; i < j; ++i) {
            this.remoteSlots.set(i, this.slots.get(i).getItem().copy());
        }
        this.remoteCarried = this.getCarried().copy();
        j = this.dataSlots.size();
        for (i = 0; i < j; ++i) {
            this.remoteDataSlots.set(i, this.dataSlots.get(i).get());
        }
        if (this.synchronizer != null) {
            this.synchronizer.sendInitialData(this, this.remoteSlots, this.remoteCarried, this.remoteDataSlots.toIntArray());
        }
    }

    public void removeSlotListener(ContainerListener containerListener) {
        this.containerListeners.remove(containerListener);
    }

    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        for (Slot slot : this.slots) {
            nonNullList.add(slot.getItem());
        }
        return nonNullList;
    }

    public void broadcastChanges() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack itemStack = this.slots.get(i).getItem();
            Supplier<ItemStack> supplier = Suppliers.memoize(itemStack::copy);
            this.triggerSlotListeners(i, itemStack, supplier);
            this.synchronizeSlotToRemote(i, itemStack, supplier);
        }
        this.synchronizeCarriedToRemote();
        for (i = 0; i < this.dataSlots.size(); ++i) {
            DataSlot dataSlot = this.dataSlots.get(i);
            int j = dataSlot.get();
            if (dataSlot.checkAndClearUpdateFlag()) {
                this.updateDataSlotListeners(i, j);
            }
            this.synchronizeDataSlotToRemote(i, j);
        }
    }

    public void broadcastFullState() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack itemStack = this.slots.get(i).getItem();
            this.triggerSlotListeners(i, itemStack, itemStack::copy);
        }
        for (i = 0; i < this.dataSlots.size(); ++i) {
            DataSlot dataSlot = this.dataSlots.get(i);
            if (!dataSlot.checkAndClearUpdateFlag()) continue;
            this.updateDataSlotListeners(i, dataSlot.get());
        }
        this.sendAllDataToRemote();
    }

    private void updateDataSlotListeners(int i, int j) {
        for (ContainerListener containerListener : this.containerListeners) {
            containerListener.dataChanged(this, i, j);
        }
    }

    private void triggerSlotListeners(int i, ItemStack itemStack, java.util.function.Supplier<ItemStack> supplier) {
        ItemStack itemStack2 = this.lastSlots.get(i);
        if (!ItemStack.matches(itemStack2, itemStack)) {
            ItemStack itemStack3 = supplier.get();
            this.lastSlots.set(i, itemStack3);
            for (ContainerListener containerListener : this.containerListeners) {
                containerListener.slotChanged(this, i, itemStack3);
            }
        }
    }

    private void synchronizeSlotToRemote(int i, ItemStack itemStack, java.util.function.Supplier<ItemStack> supplier) {
        if (this.suppressRemoteUpdates) {
            return;
        }
        ItemStack itemStack2 = this.remoteSlots.get(i);
        if (!ItemStack.matches(itemStack2, itemStack)) {
            ItemStack itemStack3 = supplier.get();
            this.remoteSlots.set(i, itemStack3);
            if (this.synchronizer != null) {
                this.synchronizer.sendSlotChange(this, i, itemStack3);
            }
        }
    }

    private void synchronizeDataSlotToRemote(int i, int j) {
        if (this.suppressRemoteUpdates) {
            return;
        }
        int k = this.remoteDataSlots.getInt(i);
        if (k != j) {
            this.remoteDataSlots.set(i, j);
            if (this.synchronizer != null) {
                this.synchronizer.sendDataChange(this, i, j);
            }
        }
    }

    private void synchronizeCarriedToRemote() {
        if (this.suppressRemoteUpdates) {
            return;
        }
        if (!ItemStack.matches(this.getCarried(), this.remoteCarried)) {
            this.remoteCarried = this.getCarried().copy();
            if (this.synchronizer != null) {
                this.synchronizer.sendCarriedChange(this, this.remoteCarried);
            }
        }
    }

    public void setRemoteSlot(int i, ItemStack itemStack) {
        this.remoteSlots.set(i, itemStack.copy());
    }

    public void setRemoteSlotNoCopy(int i, ItemStack itemStack) {
        if (i < 0 || i >= this.remoteSlots.size()) {
            LOGGER.debug("Incorrect slot index: {} available slots: {}", (Object)i, (Object)this.remoteSlots.size());
            return;
        }
        this.remoteSlots.set(i, itemStack);
    }

    public void setRemoteCarried(ItemStack itemStack) {
        this.remoteCarried = itemStack.copy();
    }

    public boolean clickMenuButton(Player player, int i) {
        return false;
    }

    public Slot getSlot(int i) {
        return this.slots.get(i);
    }

    public abstract ItemStack quickMoveStack(Player var1, int var2);

    public void clicked(int i, int j, ClickType clickType, Player player) {
        try {
            this.doClick(i, j, clickType, player);
        } catch (Exception exception) {
            CrashReport crashReport = CrashReport.forThrowable(exception, "Container click");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Click info");
            crashReportCategory.setDetail("Menu Type", () -> this.menuType != null ? Registry.MENU.getKey(this.menuType).toString() : "<no type>");
            crashReportCategory.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
            crashReportCategory.setDetail("Slot Count", this.slots.size());
            crashReportCategory.setDetail("Slot", i);
            crashReportCategory.setDetail("Button", j);
            crashReportCategory.setDetail("Type", (Object)clickType);
            throw new ReportedException(crashReport);
        }
    }

    private void doClick(int i, int j, ClickType clickType, Player player) {
        block39: {
            block50: {
                block46: {
                    ItemStack itemStack3;
                    ItemStack itemStack22;
                    Slot slot3;
                    Inventory inventory;
                    block49: {
                        block48: {
                            block47: {
                                block44: {
                                    ClickAction clickAction;
                                    block45: {
                                        block43: {
                                            block37: {
                                                block42: {
                                                    ItemStack itemStack4;
                                                    block41: {
                                                        block40: {
                                                            block38: {
                                                                inventory = player.getInventory();
                                                                if (clickType != ClickType.QUICK_CRAFT) break block37;
                                                                int k = this.quickcraftStatus;
                                                                this.quickcraftStatus = AbstractContainerMenu.getQuickcraftHeader(j);
                                                                if (k == 1 && this.quickcraftStatus == 2 || k == this.quickcraftStatus) break block38;
                                                                this.resetQuickCraft();
                                                                break block39;
                                                            }
                                                            if (!this.getCarried().isEmpty()) break block40;
                                                            this.resetQuickCraft();
                                                            break block39;
                                                        }
                                                        if (this.quickcraftStatus != 0) break block41;
                                                        this.quickcraftType = AbstractContainerMenu.getQuickcraftType(j);
                                                        if (AbstractContainerMenu.isValidQuickcraftType(this.quickcraftType, player)) {
                                                            this.quickcraftStatus = 1;
                                                            this.quickcraftSlots.clear();
                                                        } else {
                                                            this.resetQuickCraft();
                                                        }
                                                        break block39;
                                                    }
                                                    if (this.quickcraftStatus != 1) break block42;
                                                    Slot slot = this.slots.get(i);
                                                    if (!AbstractContainerMenu.canItemQuickReplace(slot, itemStack4 = this.getCarried(), true) || !slot.mayPlace(itemStack4) || this.quickcraftType != 2 && itemStack4.getCount() <= this.quickcraftSlots.size() || !this.canDragTo(slot)) break block39;
                                                    this.quickcraftSlots.add(slot);
                                                    break block39;
                                                }
                                                if (this.quickcraftStatus == 2) {
                                                    if (!this.quickcraftSlots.isEmpty()) {
                                                        if (this.quickcraftSlots.size() == 1) {
                                                            int l = this.quickcraftSlots.iterator().next().index;
                                                            this.resetQuickCraft();
                                                            this.doClick(l, this.quickcraftType, ClickType.PICKUP, player);
                                                            return;
                                                        }
                                                        ItemStack itemStack23 = this.getCarried().copy();
                                                        int m = this.getCarried().getCount();
                                                        for (Slot slot2 : this.quickcraftSlots) {
                                                            ItemStack itemStack32 = this.getCarried();
                                                            if (slot2 == null || !AbstractContainerMenu.canItemQuickReplace(slot2, itemStack32, true) || !slot2.mayPlace(itemStack32) || this.quickcraftType != 2 && itemStack32.getCount() < this.quickcraftSlots.size() || !this.canDragTo(slot2)) continue;
                                                            ItemStack itemStack4 = itemStack23.copy();
                                                            int n = slot2.hasItem() ? slot2.getItem().getCount() : 0;
                                                            AbstractContainerMenu.getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, itemStack4, n);
                                                            int o = Math.min(itemStack4.getMaxStackSize(), slot2.getMaxStackSize(itemStack4));
                                                            if (itemStack4.getCount() > o) {
                                                                itemStack4.setCount(o);
                                                            }
                                                            m -= itemStack4.getCount() - n;
                                                            slot2.set(itemStack4);
                                                        }
                                                        itemStack23.setCount(m);
                                                        this.setCarried(itemStack23);
                                                    }
                                                    this.resetQuickCraft();
                                                } else {
                                                    this.resetQuickCraft();
                                                }
                                                break block39;
                                            }
                                            if (this.quickcraftStatus == 0) break block43;
                                            this.resetQuickCraft();
                                            break block39;
                                        }
                                        if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE || j != 0 && j != 1) break block44;
                                        ClickAction clickAction2 = clickAction = j == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
                                        if (i != -999) break block45;
                                        if (this.getCarried().isEmpty()) break block39;
                                        if (clickAction == ClickAction.PRIMARY) {
                                            player.drop(this.getCarried(), true);
                                            this.setCarried(ItemStack.EMPTY);
                                        } else {
                                            player.drop(this.getCarried().split(1), true);
                                        }
                                        break block39;
                                    }
                                    if (clickType == ClickType.QUICK_MOVE) {
                                        if (i < 0) {
                                            return;
                                        }
                                        Slot slot = this.slots.get(i);
                                        if (!slot.mayPickup(player)) {
                                            return;
                                        }
                                        ItemStack itemStack5 = this.quickMoveStack(player, i);
                                        while (!itemStack5.isEmpty() && ItemStack.isSame(slot.getItem(), itemStack5)) {
                                            itemStack5 = this.quickMoveStack(player, i);
                                        }
                                    } else {
                                        if (i < 0) {
                                            return;
                                        }
                                        Slot slot = this.slots.get(i);
                                        ItemStack itemStack6 = slot.getItem();
                                        ItemStack itemStack5 = this.getCarried();
                                        player.updateTutorialInventoryAction(itemStack5, slot.getItem(), clickAction);
                                        if (!itemStack5.overrideStackedOnOther(slot, clickAction, player) && !itemStack6.overrideOtherStackedOnMe(itemStack5, slot, clickAction, player, this.createCarriedSlotAccess())) {
                                            if (itemStack6.isEmpty()) {
                                                if (!itemStack5.isEmpty()) {
                                                    int p = clickAction == ClickAction.PRIMARY ? itemStack5.getCount() : 1;
                                                    this.setCarried(slot.safeInsert(itemStack5, p));
                                                }
                                            } else if (slot.mayPickup(player)) {
                                                if (itemStack5.isEmpty()) {
                                                    int p = clickAction == ClickAction.PRIMARY ? itemStack6.getCount() : (itemStack6.getCount() + 1) / 2;
                                                    Optional<ItemStack> optional = slot.tryRemove(p, Integer.MAX_VALUE, player);
                                                    optional.ifPresent(itemStack -> {
                                                        this.setCarried((ItemStack)itemStack);
                                                        slot.onTake(player, (ItemStack)itemStack);
                                                    });
                                                } else if (slot.mayPlace(itemStack5)) {
                                                    if (ItemStack.isSameItemSameTags(itemStack6, itemStack5)) {
                                                        int p = clickAction == ClickAction.PRIMARY ? itemStack5.getCount() : 1;
                                                        this.setCarried(slot.safeInsert(itemStack5, p));
                                                    } else if (itemStack5.getCount() <= slot.getMaxStackSize(itemStack5)) {
                                                        this.setCarried(itemStack6);
                                                        slot.set(itemStack5);
                                                    }
                                                } else if (ItemStack.isSameItemSameTags(itemStack6, itemStack5)) {
                                                    Optional<ItemStack> optional2 = slot.tryRemove(itemStack6.getCount(), itemStack5.getMaxStackSize() - itemStack5.getCount(), player);
                                                    optional2.ifPresent(itemStack2 -> {
                                                        itemStack5.grow(itemStack2.getCount());
                                                        slot.onTake(player, (ItemStack)itemStack2);
                                                    });
                                                }
                                            }
                                        }
                                        slot.setChanged();
                                    }
                                    break block39;
                                }
                                if (clickType != ClickType.SWAP) break block46;
                                slot3 = this.slots.get(i);
                                itemStack22 = inventory.getItem(j);
                                itemStack3 = slot3.getItem();
                                if (itemStack22.isEmpty() && itemStack3.isEmpty()) break block39;
                                if (!itemStack22.isEmpty()) break block47;
                                if (!slot3.mayPickup(player)) break block39;
                                inventory.setItem(j, itemStack3);
                                slot3.onSwapCraft(itemStack3.getCount());
                                slot3.set(ItemStack.EMPTY);
                                slot3.onTake(player, itemStack3);
                                break block39;
                            }
                            if (!itemStack3.isEmpty()) break block48;
                            if (!slot3.mayPlace(itemStack22)) break block39;
                            int q = slot3.getMaxStackSize(itemStack22);
                            if (itemStack22.getCount() > q) {
                                slot3.set(itemStack22.split(q));
                            } else {
                                inventory.setItem(j, ItemStack.EMPTY);
                                slot3.set(itemStack22);
                            }
                            break block39;
                        }
                        if (!slot3.mayPickup(player) || !slot3.mayPlace(itemStack22)) break block39;
                        int q = slot3.getMaxStackSize(itemStack22);
                        if (itemStack22.getCount() <= q) break block49;
                        slot3.set(itemStack22.split(q));
                        slot3.onTake(player, itemStack3);
                        if (inventory.add(itemStack3)) break block39;
                        player.drop(itemStack3, true);
                        break block39;
                    }
                    inventory.setItem(j, itemStack3);
                    slot3.set(itemStack22);
                    slot3.onTake(player, itemStack3);
                    break block39;
                }
                if (clickType != ClickType.CLONE || !player.getAbilities().instabuild || !this.getCarried().isEmpty() || i < 0) break block50;
                Slot slot3 = this.slots.get(i);
                if (!slot3.hasItem()) break block39;
                ItemStack itemStack24 = slot3.getItem().copy();
                itemStack24.setCount(itemStack24.getMaxStackSize());
                this.setCarried(itemStack24);
                break block39;
            }
            if (clickType == ClickType.THROW && this.getCarried().isEmpty() && i >= 0) {
                Slot slot3 = this.slots.get(i);
                int l = j == 0 ? 1 : slot3.getItem().getCount();
                ItemStack itemStack7 = slot3.safeTake(l, Integer.MAX_VALUE, player);
                player.drop(itemStack7, true);
            } else if (clickType == ClickType.PICKUP_ALL && i >= 0) {
                Slot slot3 = this.slots.get(i);
                ItemStack itemStack25 = this.getCarried();
                if (!(itemStack25.isEmpty() || slot3.hasItem() && slot3.mayPickup(player))) {
                    int m = j == 0 ? 0 : this.slots.size() - 1;
                    int q = j == 0 ? 1 : -1;
                    for (int p = 0; p < 2; ++p) {
                        for (int r = m; r >= 0 && r < this.slots.size() && itemStack25.getCount() < itemStack25.getMaxStackSize(); r += q) {
                            Slot slot4 = this.slots.get(r);
                            if (!slot4.hasItem() || !AbstractContainerMenu.canItemQuickReplace(slot4, itemStack25, true) || !slot4.mayPickup(player) || !this.canTakeItemForPickAll(itemStack25, slot4)) continue;
                            ItemStack itemStack6 = slot4.getItem();
                            if (p == 0 && itemStack6.getCount() == itemStack6.getMaxStackSize()) continue;
                            ItemStack itemStack7 = slot4.safeTake(itemStack6.getCount(), itemStack25.getMaxStackSize() - itemStack25.getCount(), player);
                            itemStack25.grow(itemStack7.getCount());
                        }
                    }
                }
            }
        }
    }

    private SlotAccess createCarriedSlotAccess() {
        return new SlotAccess(){

            @Override
            public ItemStack get() {
                return AbstractContainerMenu.this.getCarried();
            }

            @Override
            public boolean set(ItemStack itemStack) {
                AbstractContainerMenu.this.setCarried(itemStack);
                return true;
            }
        };
    }

    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return true;
    }

    public void removed(Player player) {
        ItemStack itemStack;
        if (player instanceof ServerPlayer && !(itemStack = this.getCarried()).isEmpty()) {
            if (!player.isAlive() || ((ServerPlayer)player).hasDisconnected()) {
                player.drop(itemStack, false);
            } else {
                player.getInventory().placeItemBackInInventory(itemStack);
            }
            this.setCarried(ItemStack.EMPTY);
        }
    }

    protected void clearContainer(Player player, Container container) {
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
            for (int i = 0; i < container.getContainerSize(); ++i) {
                player.drop(container.removeItemNoUpdate(i), false);
            }
            return;
        }
        for (int i = 0; i < container.getContainerSize(); ++i) {
            Inventory inventory = player.getInventory();
            if (!(inventory.player instanceof ServerPlayer)) continue;
            inventory.placeItemBackInInventory(container.removeItemNoUpdate(i));
        }
    }

    public void slotsChanged(Container container) {
        this.broadcastChanges();
    }

    public void setItem(int i, int j, ItemStack itemStack) {
        this.getSlot(i).set(itemStack);
        this.stateId = j;
    }

    public void initializeContents(int i, List<ItemStack> list, ItemStack itemStack) {
        for (int j = 0; j < list.size(); ++j) {
            this.getSlot(j).initialize(list.get(j));
        }
        this.carried = itemStack;
        this.stateId = i;
    }

    public void setData(int i, int j) {
        this.dataSlots.get(i).set(j);
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
                if (!itemStack2.isEmpty() && ItemStack.isSameItemSameTags(itemStack, itemStack2)) {
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
        return i == 2 && player.getAbilities().instabuild;
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack itemStack, boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = slot == null || !slot.hasItem();
        if (!bl2 && ItemStack.isSameItemSameTags(itemStack, slot.getItem())) {
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

    public void setCarried(ItemStack itemStack) {
        this.carried = itemStack;
    }

    public ItemStack getCarried() {
        return this.carried;
    }

    public void suppressRemoteUpdates() {
        this.suppressRemoteUpdates = true;
    }

    public void resumeRemoteUpdates() {
        this.suppressRemoteUpdates = false;
    }

    public void transferState(AbstractContainerMenu abstractContainerMenu) {
        Slot slot;
        int i;
        HashBasedTable<Container, Integer, Integer> table = HashBasedTable.create();
        for (i = 0; i < abstractContainerMenu.slots.size(); ++i) {
            slot = abstractContainerMenu.slots.get(i);
            table.put(slot.container, slot.getContainerSlot(), i);
        }
        for (i = 0; i < this.slots.size(); ++i) {
            slot = this.slots.get(i);
            Integer integer = (Integer)table.get(slot.container, slot.getContainerSlot());
            if (integer == null) continue;
            this.lastSlots.set(i, abstractContainerMenu.lastSlots.get(integer));
            this.remoteSlots.set(i, abstractContainerMenu.remoteSlots.get(integer));
        }
    }

    public OptionalInt findSlot(Container container, int i) {
        for (int j = 0; j < this.slots.size(); ++j) {
            Slot slot = this.slots.get(j);
            if (slot.container != container || i != slot.getContainerSlot()) continue;
            return OptionalInt.of(j);
        }
        return OptionalInt.empty();
    }

    public int getStateId() {
        return this.stateId;
    }

    public int incrementStateId() {
        this.stateId = this.stateId + 1 & Short.MAX_VALUE;
        return this.stateId;
    }
}

