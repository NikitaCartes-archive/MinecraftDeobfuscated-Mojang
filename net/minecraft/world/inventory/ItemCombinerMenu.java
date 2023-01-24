/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class ItemCombinerMenu
extends AbstractContainerMenu {
    private static final int INVENTORY_SLOTS_PER_ROW = 9;
    private static final int INVENTORY_SLOTS_PER_COLUMN = 3;
    protected final ContainerLevelAccess access;
    protected final Player player;
    protected final Container inputSlots;
    private final List<Integer> inputSlotIndexes;
    protected final ResultContainer resultSlots = new ResultContainer();
    private final int resultSlotIndex;

    protected abstract boolean mayPickup(Player var1, boolean var2);

    protected abstract void onTake(Player var1, ItemStack var2);

    protected abstract boolean isValidBlock(BlockState var1);

    public ItemCombinerMenu(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i);
        this.access = containerLevelAccess;
        this.player = inventory.player;
        ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition = this.createInputSlotDefinitions();
        this.inputSlots = this.createContainer(itemCombinerMenuSlotDefinition.getNumOfInputSlots());
        this.inputSlotIndexes = itemCombinerMenuSlotDefinition.getInputSlotIndexes();
        this.resultSlotIndex = itemCombinerMenuSlotDefinition.getResultSlotIndex();
        this.createInputSlots(itemCombinerMenuSlotDefinition);
        this.createResultSlot(itemCombinerMenuSlotDefinition);
        this.createInventorySlots(inventory);
    }

    private void createInputSlots(ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
        for (final ItemCombinerMenuSlotDefinition.SlotDefinition slotDefinition : itemCombinerMenuSlotDefinition.getSlots()) {
            this.addSlot(new Slot(this.inputSlots, slotDefinition.slotIndex(), slotDefinition.x(), slotDefinition.y()){

                @Override
                public boolean mayPlace(ItemStack itemStack) {
                    return slotDefinition.mayPlace().test(itemStack);
                }
            });
        }
    }

    private void createResultSlot(ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
        this.addSlot(new Slot(this.resultSlots, itemCombinerMenuSlotDefinition.getResultSlot().slotIndex(), itemCombinerMenuSlotDefinition.getResultSlot().x(), itemCombinerMenuSlotDefinition.getResultSlot().y()){

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return ItemCombinerMenu.this.mayPickup(player, this.hasItem());
            }

            @Override
            public void onTake(Player player, ItemStack itemStack) {
                ItemCombinerMenu.this.onTake(player, itemStack);
            }
        });
    }

    private void createInventorySlots(Inventory inventory) {
        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        }
    }

    public abstract void createResult();

    protected abstract ItemCombinerMenuSlotDefinition createInputSlotDefinitions();

    private SimpleContainer createContainer(int i) {
        return new SimpleContainer(i){

            @Override
            public void setChanged() {
                super.setChanged();
                ItemCombinerMenu.this.slotsChanged(this);
            }
        };
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.inputSlots) {
            this.createResult();
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.inputSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, blockPos) -> {
            if (!this.isValidBlock(level.getBlockState((BlockPos)blockPos))) {
                return false;
            }
            return player.distanceToSqr((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) <= 64.0;
        }, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            int l;
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            int j = this.getInventorySlotStart();
            int k = this.getUseRowEnd();
            if (i == this.getResultSlot()) {
                if (!this.moveItemStackTo(itemStack2, j, k, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemStack2, itemStack);
            } else if (this.inputSlotIndexes.contains(i) ? !this.moveItemStackTo(itemStack2, j, k, false) : (this.canMoveIntoInputSlots(itemStack2) && i >= this.getInventorySlotStart() && i < this.getUseRowEnd() ? !this.moveItemStackTo(itemStack2, l = this.getSlotToQuickMoveTo(itemStack), l + 1, false) : (i >= this.getInventorySlotStart() && i < this.getInventorySlotEnd() ? !this.moveItemStackTo(itemStack2, this.getUseRowStart(), this.getUseRowEnd(), false) : i >= this.getUseRowStart() && i < this.getUseRowEnd() && !this.moveItemStackTo(itemStack2, this.getInventorySlotStart(), this.getInventorySlotEnd(), false)))) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack2);
        }
        return itemStack;
    }

    protected boolean canMoveIntoInputSlots(ItemStack itemStack) {
        return true;
    }

    public int getSlotToQuickMoveTo(ItemStack itemStack) {
        return this.inputSlots.isEmpty() ? 0 : this.inputSlotIndexes.get(0);
    }

    public int getResultSlot() {
        return this.resultSlotIndex;
    }

    private int getInventorySlotStart() {
        return this.getResultSlot() + 1;
    }

    private int getInventorySlotEnd() {
        return this.getInventorySlotStart() + 27;
    }

    private int getUseRowStart() {
        return this.getInventorySlotEnd();
    }

    private int getUseRowEnd() {
        return this.getUseRowStart() + 9;
    }
}

